import React, { useState, useEffect } from 'react';
import { api } from '../api';

const TABS = ['Departments', 'Courses', 'Semesters', 'Teachers', 'Subjects', 'Rooms', 'Sections', 'Allocations'];

export default function DataEntry() {
  const [tab, setTab] = useState('Departments');
  const [departments, setDepartments] = useState([]);
  const [courses, setCourses] = useState([]);
  const [semesters, setSemesters] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [sections, setSections] = useState([]);
  const [allocations, setAllocations] = useState([]);
  const [academicYear, setAcademicYear] = useState('2024-2025');
  const [selectedSemesterForSections, setSelectedSemesterForSections] = useState(null);
  const [selectedSemesterForAllocations, setSelectedSemesterForAllocations] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [allocationSubjects, setAllocationSubjects] = useState([]);

  const load = async () => {
    setError('');
    try {
      const [depts, crs, sem, tch, subj, rms] = await Promise.all([
        api.departments.list(),
        api.courses.list(),
        api.semesters.list(),
        api.teachers.list(),
        api.subjects.list(),
        api.rooms.list(),
      ]);
      setDepartments(depts);
      setCourses(crs);
      setSemesters(sem);
      setTeachers(tch);
      setSubjects(subj);
      setRooms(rms);
      if (selectedSemesterForSections) {
        const sec = await api.classSections.list(selectedSemesterForSections);
        setSections(sec);
      }
      if (selectedSemesterForAllocations) {
        const alloc = await api.allocations.list(selectedSemesterForAllocations, academicYear);
        setAllocations(alloc);
      }
    } catch (e) {
      setError(e.message);
    }
  };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { load(); }, []);
  useEffect(() => {
    if (selectedSemesterForSections) {
       api.classSections.list(selectedSemesterForSections)
          .then(setSections)
          .catch((e) => setError(e.message));    }
  }, [selectedSemesterForSections]);
  useEffect(() => {
    if (selectedSemesterForAllocations) {
        api.allocations.list(selectedSemesterForAllocations, academicYear)
          .then(setAllocations)
          .catch((e) => setError(e.message));    }
  }, [selectedSemesterForAllocations, academicYear]);
  useEffect(() => {
    if (selectedSemesterForAllocations) {
      api.subjects.list(selectedSemesterForAllocations)
      .then(setAllocationSubjects)
      .catch((e) => setError(e.message));
    } else {
      setAllocationSubjects([]);
    }
  }, [selectedSemesterForAllocations]);

  return (
    <div>
      <h1 style={{ marginBottom: '1rem' }}>Data Entry</h1>
      {error && <div className="error-msg">{error}</div>}
      {success && <div className="success-msg">{success}</div>}

      <div className="card" style={{ marginBottom: '1rem' }}>
        {TABS.map((t) => (
          <button
            key={t}
            className={`btn btn-secondary ${tab === t ? 'active' : ''}`}
            style={{ marginRight: '0.5rem', marginBottom: '0.5rem' }}
            onClick={() => { setTab(t); setError(''); setSuccess(''); }}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'Departments' && (
        <EntityCrud
          title="Departments"
          list={departments}
          onReload={load}
          fields={[{ key: 'name', label: 'Name', type: 'text' }]}
          createPayload={(form) => ({ name: form.name })}
          api={api.departments}
          noForeignKeys
        />
      )}
      {tab === 'Courses' && (
        <EntityCrud
          title="Courses"
          list={courses}
          onReload={load}
          fields={[
            { key: 'name', label: 'Name', type: 'text' },
            { key: 'code', label: 'Code', type: 'text' },
            { key: 'departmentId', label: 'Department', type: 'select', options: departments, optionValue: 'id', optionLabel: 'name' },
          ]}
          createPayload={(form) => ({ name: form.name, code: form.code, departmentId: Number(form.departmentId) })}
          api={api.courses}
        />
      )}
      {tab === 'Semesters' && (
        <EntityCrud
          title="Semesters"
          list={semesters}
          onReload={load}
          fields={[
            { key: 'courseId', label: 'Course', type: 'select', options: courses, optionValue: 'id', optionLabel: 'name' },
            { key: 'semesterNumber', label: 'Semester (1-8)', type: 'number' },
          ]}
          createPayload={(form) => ({ courseId: Number(form.courseId), semesterNumber: Number(form.semesterNumber) })}
          api={api.semesters}
        />
      )}
      {tab === 'Teachers' && (
        <EntityCrud
          title="Teachers"
          list={teachers}
          onReload={load}
          fields={[
            { key: 'name', label: 'Name', type: 'text' },
            { key: 'email', label: 'Email', type: 'text' },
            { key: 'departmentId', label: 'Department', type: 'select', options: departments, optionValue: 'id', optionLabel: 'name' },
          ]}
          createPayload={(form) => ({ name: form.name, email: form.email || null, departmentId: Number(form.departmentId) })}
          api={api.teachers}
        />
      )}
  {tab === 'Subjects' && (
  <EntityCrud
    title="Subjects"
    list={subjects}
    onReload={load}
    fields={[
      { key: 'name', label: 'Name', type: 'text' },
      { key: 'code', label: 'Code', type: 'text' },
      { key: 'isLab', label: 'Is Lab', type: 'checkbox' },
      { key: 'periodsPerWeek', label: 'Periods per week', type: 'number' },
      {
        key: 'semesterId',
        label: 'Semester',
        type: 'select',
        options: semesters,
        optionValue: 'id',
        optionLabel: (s) => {
          const course = courses.find((c) => c.id === s.courseId);
          return `${course?.name ?? 'Unknown'} - Sem ${s.semesterNumber}`;
        }
      },
    ]}
    createPayload={(form) => ({
      name: form.name,
      code: form.code,
      isLab: form.isLab === true || form.isLab === 'true',
      periodsPerWeek: Number(form.periodsPerWeek),
      semesterId: Number(form.semesterId),
    })}
    api={api.subjects}
  />
)}
      {tab === 'Rooms' && (
        <EntityCrud
          title="Rooms"
          list={rooms}
          onReload={load}
          fields={[
            { key: 'name', label: 'Name', type: 'text' },
            { key: 'isLab', label: 'Is Lab', type: 'checkbox' },
            { key: 'departmentId', label: 'Department', type: 'select', options: departments, optionValue: 'id', optionLabel: 'name' },
          ]}
          createPayload={(form) => ({
            name: form.name,
            isLab: form.isLab === true || form.isLab === 'true',
            departmentId: Number(form.departmentId),
          })}
          api={api.rooms}
        />
      )}
      {tab === 'Sections' && (
        <div className="card">
          <h2>Class Sections</h2>
          <div className="form-group">
            <label>Semester</label>
            <select
              value={selectedSemesterForSections || ''}
              onChange={(e) => setSelectedSemesterForSections(e.target.value ? Number(e.target.value) : null)}
            >
              <option value="">Select semester</option>
              {semesters.map((s) => (
                <option key={s.id} value={s.id}>
                  {courses.find((c) => c.id === s.courseId)?.name} - Sem {s.semesterNumber}
                </option>
              ))}
            </select>
          </div>
          {selectedSemesterForSections && (() => {
            const selSem = semesters.find((s) => s.id === selectedSemesterForSections);
            const courseForSem = selSem ? courses.find((c) => c.id === selSem.courseId) : null;
            return (
              <EntityCrud
                title=""
                list={sections}
                onReload={() =>
                   api.classSections.list(selectedSemesterForSections)
                     .then(setSections)
                     .catch((e) => setError(e.message))
                    }                
                    fields={[
                  { key: 'sectionName', label: 'Section name (e.g. A, B)', type: 'text' },
                ]}
                createPayload={(form) => ({
                  sectionName: form.sectionName,
                  courseId: courseForSem?.id,
                  semesterId: selectedSemesterForSections,
                })}
                api={api.classSections}
              />
            );
          })()}
        </div>
      )}
      {tab === 'Allocations' && (
        <div className="card">
          <h2>Teacher–Subject–Semester Allocations</h2>
          <div className="form-row">
            <div className="form-group">
              <label>Academic year</label>
              <input
                type="text"
                value={academicYear}
                onChange={(e) => setAcademicYear(e.target.value)}
                placeholder="e.g. 2024-2025"
              />
            </div>
            <div className="form-group">
              <label>Semester</label>
              <select
                value={selectedSemesterForAllocations || ''}
                onChange={(e) => setSelectedSemesterForAllocations(e.target.value ? Number(e.target.value) : null)}
              >
                <option value="">Select semester</option>
                {semesters.map((s) => (
                  <option key={s.id} value={s.id}>
                    {courses.find((c) => c.id === s.courseId)?.name} - Sem {s.semesterNumber}
                  </option>
                ))}
              </select>
            </div>
          </div>
          {selectedSemesterForAllocations && (
            <AllocationCrud
              api={api}
              allocations={allocations}
              teachers={teachers}
              subjects={allocationSubjects}
              rooms={rooms}
              semesterId={selectedSemesterForAllocations}
              academicYear={academicYear}
              onReload={() => api.allocations.list(selectedSemesterForAllocations, academicYear).then(setAllocations)}
            />
          )}
        </div>
      )}
    </div>
  );
}

function EntityCrud({ title, list, onReload, fields, createPayload, api, noForeignKeys }) {
  const [form, setForm] = useState({});
  const [editingId, setEditingId] = useState(null);
  const [err, setErr] = useState('');
  const [ok, setOk] = useState('');

  const resetForm = () => {
    const initial = {};
    fields.forEach((f) => {
      initial[f.key] = f.type === 'checkbox' ? false : '';
    });
    setForm(initial);
    setEditingId(null);
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setErr('');
    setOk('');
    try {
      await api.create(createPayload(form));
      setOk('Created.');
      resetForm();
      onReload();
    } catch (e) {
      setErr(e.message);
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    setErr('');
    setOk('');
    try {
      await api.update(editingId, createPayload(form));
      setOk('Updated.');
      resetForm();
      onReload();
    } catch (e) {
      setErr(e.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete?')) return;
    setErr('');
    try {
      await api.delete(id);
      setOk('Deleted.');
      onReload();
      resetForm();
    } catch (e) {
      setErr(e.message);
    }
  };

  const startEdit = (item) => {
    const values = {};
    fields.forEach((f) => { values[f.key] = item[f.key] ?? (f.type === 'checkbox' ? false : ''); });
    setForm(values);
    setEditingId(item.id);
  };

  return (
    <div className="card">
      {title && <h2>{title}</h2>}
      {err && <div className="error-msg">{err}</div>}
      {ok && <div className="success-msg">{ok}</div>}
      <form onSubmit={editingId ? handleUpdate : handleCreate}>
        {fields.map((f) => (
          <div key={f.key} className="form-group">
            <label>{f.label}</label>
            {f.type === 'select' && (
              <select
                value={form[f.key] ?? ''}
                onChange={(e) => setForm({ ...form, [f.key]: e.target.value })}
              >
                <option value="">Select</option>
                {(f.options || []).map((opt) => (
                  <option key={opt.id} value={opt.id}>
                    {typeof f.optionLabel === 'function' ? f.optionLabel(opt) : opt[f.optionLabel]}
                  </option>
                ))}
              </select>
            )}
            {f.type === 'text' && (
              <input
                type="text"
                value={form[f.key] ?? ''}
                onChange={(e) => setForm({ ...form, [f.key]: e.target.value })}
              />
            )}
            {f.type === 'number' && (
              <input
                type="number"
                value={form[f.key] ?? ''}
                onChange={(e) => setForm({ ...form, [f.key]: e.target.value })}
              />
            )}
            {f.type === 'checkbox' && (
              <input
                type="checkbox"
                checked={form[f.key] === true || form[f.key] === 'true'}
                onChange={(e) => setForm({ ...form, [f.key]: e.target.checked })}
              />
            )}
          </div>
        ))}
        <button type="submit" className="btn">{editingId ? 'Update' : 'Create'}</button>
        {editingId && (
          <button type="button" className="btn btn-secondary" style={{ marginLeft: '0.5rem' }} onClick={resetForm}>
            Cancel
          </button>
        )}
      </form>
      <table style={{ width: '100%', marginTop: '1.5rem', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--border)' }}>
            {fields.map((f) => (
              <th key={f.key} style={{ textAlign: 'left', padding: '0.5rem' }}>{f.label}</th>
            ))}
            <th style={{ padding: '0.5rem' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {list.map((item) => (
            <tr key={item.id} style={{ borderBottom: '1px solid var(--border)' }}>
              {fields.map((f) => (
                <td key={f.key} style={{ padding: '0.5rem' }}>
                  {f.type === 'select' && f.options
                    ? (() => {
                         const opt = f.options.find((o) => o.id === item[f.key]);
                         if (!opt) return item[f.key];
                         return typeof f.optionLabel === 'function' ? f.optionLabel(opt) : opt[f.optionLabel];
                       })()
                     : String(item[f.key] ?? '')}
                </td>
              ))}
              <td style={{ padding: '0.5rem' }}>
                <button type="button" className="btn btn-secondary" style={{ marginRight: '0.5rem' }} onClick={() => startEdit(item)}>Edit</button>
                <button type="button" className="btn btn-danger" onClick={() => handleDelete(item.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function AllocationCrud({ api, allocations, teachers, subjects, rooms, semesterId, academicYear, onReload }) {
  const [teacherId, setTeacherId] = useState('');
  const [subjectId, setSubjectId] = useState('');
  const [roomId, setRoomId] = useState('');
  const [err, setErr] = useState('');
  const [ok, setOk] = useState('');

  const selectedSubject = subjects.find((s) => s.id === Number(subjectId));
  const isLabSubject = selectedSubject && selectedSubject.isLab;
  const labRooms = (rooms || []).filter((r) => r.isLab);

  const handleCreate = async (e) => {
    e.preventDefault();
    setErr('');
    setOk('');
    try {
      const body = { teacherId: Number(teacherId), subjectId: Number(subjectId), semesterId, academicYear };
      if (isLabSubject && roomId) body.roomId = Number(roomId);
      await api.allocations.create(body);
      setOk('Allocation added.');
      setTeacherId('');
      setSubjectId('');
      setRoomId('');
      onReload();
    } catch (e) {
      setErr(e.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Remove this allocation?')) return;
    try {
      await api.allocations.delete(id);
      onReload();
    } catch (e) {
      setErr(e.message);
    }
  };

  return (
    <>
      {err && <div className="error-msg">{err}</div>}
      {ok && <div className="success-msg">{ok}</div>}
      <form onSubmit={handleCreate} className="form-row">
        <div className="form-group">
          <label>Teacher</label>
          <select value={teacherId} onChange={(e) => setTeacherId(e.target.value)}>
            <option value="">Select</option>
            {teachers.map((t) => (
              <option key={t.id} value={t.id}>{t.name}</option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label>Subject</label>
          <select value={subjectId} onChange={(e) => { setSubjectId(e.target.value); setRoomId(''); }}>
            <option value="">Select</option>
            {subjects.map((s) => (
              <option key={s.id} value={s.id}>{s.name} ({s.code}) {s.isLab ? '(Lab)' : ''}</option>
            ))}
          </select>
        </div>
        {isLabSubject && (
          <div className="form-group">
            <label>Lab room (required for lab subjects)</label>
            <select value={roomId} onChange={(e) => setRoomId(e.target.value)}>
              <option value="">Select lab room</option>
              {labRooms.map((r) => (
                <option key={r.id} value={r.id}>{r.name}</option>
              ))}
            </select>
          </div>
        )}
        <div className="form-group">
          <button type="submit" className="btn" disabled={isLabSubject && !roomId}>Add allocation</button>
        </div>
      </form>
      <table style={{ width: '100%', marginTop: '1rem', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--border)' }}>
            <th style={{ textAlign: 'left', padding: '0.5rem' }}>Teacher</th>
            <th style={{ textAlign: 'left', padding: '0.5rem' }}>Subject</th>
            <th style={{ textAlign: 'left', padding: '0.5rem' }}>Lab room</th>
            <th style={{ padding: '0.5rem' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {allocations.map((a) => (
            <tr key={a.id} style={{ borderBottom: '1px solid var(--border)' }}>
              <td style={{ padding: '0.5rem' }}>{teachers.find((t) => t.id === a.teacherId)?.name}</td>
              <td style={{ padding: '0.5rem' }}>{subjects.find((s) => s.id === a.subjectId)?.name}</td>
              <td style={{ padding: '0.5rem' }}>{a.roomId ? (rooms || []).find((r) => r.id === a.roomId)?.name : '—'}</td>
              <td style={{ padding: '0.5rem' }}>
                <button type="button" className="btn btn-danger" onClick={() => handleDelete(a.id)}>Remove</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

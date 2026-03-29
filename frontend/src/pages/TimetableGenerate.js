import React, { useState, useEffect } from 'react';
import { api } from '../api';

const DAY_LABELS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

export default function TimetableGenerate() {
  const [semesters, setSemesters] = useState([]);
  const [courses, setCourses] = useState([]);
  const [semesterId, setSemesterId] = useState('');
  const [academicYear, setAcademicYear] = useState('2024-2025');
  const [versions, setVersions] = useState([]);
  const [selectedVersionId, setSelectedVersionId] = useState(null);
  const [grid, setGrid] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [conflict, setConflict] = useState(null);
  const [modifyOnConflict, setModifyOnConflict] = useState(false);

  useEffect(() => {
    api.courses.list().then(setCourses).catch(e => setError(e.message));
    api.semesters.list().then(setSemesters).catch(e => setError(e.message));
  }, []);

  useEffect(() => {
    if (semesterId) {
      api.timetable.listVersions(semesterId).then(setVersions).catch(e => setError(e.message));
      setSelectedVersionId(null);
      setGrid(null);
    } else {
      setVersions([]);
      setGrid(null);
    }
  }, [semesterId]);

  useEffect(() => {
    if (selectedVersionId) {
      setLoading(true);
      api.timetable.getVersion(selectedVersionId)
        .then(setGrid)
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
    } else {
      setGrid(null);
    }
  }, [selectedVersionId]);

  const handleGenerate = async () => {
    if (!semesterId || !academicYear.trim()) {
      setError('Select semester and enter academic year.');
      return;
    }
    setError('');
    setConflict(null);
    setLoading(true);
    try {
      const result = await api.timetable.generate({
        semesterId: Number(semesterId),
        academicYear: academicYear.trim(),
        modifyExistingOnConflict: modifyOnConflict,
      });
      if (result && result.conflict) {
        setConflict(result);
        setGrid(null);
      } else if (result && result.timetableVersionId) {
        setSelectedVersionId(result.timetableVersionId);
        setGrid(result);
        api.timetable.listVersions(semesterId).then(setVersions);
      } else if (result) {
        setGrid(result);
        setSelectedVersionId(result.timetableVersionId);
        api.timetable.listVersions(semesterId).then(setVersions);
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const semesterOptions = semesters.map((s) => ({
    ...s,
    courseName: courses.find((c) => c.id === s.courseId)?.name || '',
  }));

  return (
    <div>
      <h1 style={{ marginBottom: '1rem' }}>Timetable Generation</h1>
      {error && <div className="error-msg">{error}</div>}
      {conflict && (
        <div className="conflict-banner">
          <strong>Conflict</strong>
          <p>{conflict.message}</p>
          <p>You can try again with &quot;Modify existing on conflict&quot; checked to allow the system to suggest changes.</p>
        </div>
      )}

      <div className="card">
        <h2>Generate Timetable</h2>
        <div className="form-row">
          <div className="form-group">
            <label>Semester</label>
            <select
              value={semesterId}
              onChange={(e) => setSemesterId(e.target.value || '')}
            >
              <option value="">Select semester</option>
              {semesterOptions.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.courseName} – Sem {s.semesterNumber}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Academic year</label>
            <input
              type="text"
              value={academicYear}
              onChange={(e) => setAcademicYear(e.target.value)}
              placeholder="2024-2025"
            />
          </div>
          <div className="form-group">
            <label>
              <input
                type="checkbox"
                checked={modifyOnConflict}
                onChange={(e) => setModifyOnConflict(e.target.checked)}
              />
              {' '}Modify existing on conflict
            </label>
          </div>
          <div className="form-group">
            <button
              type="button"
              className="btn"
              onClick={handleGenerate}
              disabled={loading || !semesterId}
            >
              {loading ? 'Generating…' : 'Generate Timetable'}
            </button>
          </div>
        </div>
      </div>

      {semesterId && versions.length > 0 && (
        <div className="card">
          <h2>Saved versions</h2>
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            {versions.map((v) => (
                <div key={v.id} style={{ display: 'flex', gap: '6px', alignItems: 'center', marginBottom: '6px' }}>
                     <button
                         type="button"
                         className={`btn btn-secondary ${selectedVersionId === v.id ? 'active' : ''}`}
                         onClick={() => setSelectedVersionId(v.id)}
                     >
                        Version {v.versionNumber} {v.status === 'ACTIVE' ? '★' : ''}
                     </button>
                     {v.status !== 'ACTIVE' && (
                         <>
                             <button
                                type="button"
                                className="btn btn-success"
                                onClick={() =>
                                    api.timetable.activateVersion(v.id)
                                       .then(() => api.timetable.listVersions(semesterId).then(setVersions))
                                       .catch(e => setError(e.message))
                               }
                             >
                               Set active
                             </button>
                             <button
                                type="button"
                                className="btn btn-danger"
                                onClick={() => {
                                   if (window.confirm(`Delete Version ${v.versionNumber}?`)) {
                                       api.timetable.deleteVersion(v.id)
                                          .then(() => api.timetable.listVersions(semesterId).then(setVersions))
                                          .catch(e => setError(e.message));
                                 }
                               }}
                             >
                               Delete
                             </button>
                        </>
                      )}
          </div>
     ))}
          </div>
        </div>
      )}

      {loading && !grid && <p>Loading…</p>}
      {grid && <TimetableGrid grid={grid} onSlotUpdate={() => selectedVersionId && api.timetable.getVersion(selectedVersionId).then(setGrid)} />}
    </div>
  );
}

// Assignable period indices only (no break, no lunch)
const MON_THU_PERIODS = [
  { index: 0, label: 'P1', time: '9:00–10:00'   },
  { index: 2, label: 'P2', time: '10:15–11:15'  },
  { index: 3, label: 'P3', time: '11:15–12:15'  },
  { index: 5, label: 'P4', time: '1:15–2:15'    },
  { index: 6, label: 'P5', time: '2:15–3:15'    },
  { index: 7, label: 'P6', time: '3:15–4:15'    },
];

const FRIDAY_PERIODS = [
  { index: 0, label: 'P1', time: '9:00–9:50'    },
  { index: 1, label: 'P2', time: '9:50–10:40'   },
  { index: 3, label: 'P3', time: '10:50–11:40'  },
  { index: 4, label: 'P4', time: '11:40–12:30'  },
  { index: 6, label: 'P5', time: '2:00–3:00'    },
  { index: 7, label: 'P6', time: '3:00–4:00'    },
];

function getPeriodsForDay(dayIndex) {
  return dayIndex === 4 ? FRIDAY_PERIODS : MON_THU_PERIODS;
}

function TimetableGrid({ grid, onSlotUpdate }) {
  const [swapFrom, setSwapFrom] = useState(null);
  const [updateError, setUpdateError] = useState('');

  const sectionIds = grid.data ? Object.keys(grid.data).map(Number) : [];
  const dayLabels = grid.dayLabels || DAY_LABELS;

  const handleLock = async (slot) => {
    if (!slot || !slot.id) return;
    setUpdateError('');
    try {
      await api.timetable.updateSlot({ slotId: slot.id, isLocked: !slot.isLocked });
      onSlotUpdate();
    } catch (e) { setUpdateError(e.message); }
  };

 

  const handleSwapClick = (slot) => {
    if (!slot || !slot.id) return;
    if (!swapFrom) { setSwapFrom(slot); return; }
    if (swapFrom.id === slot.id) { setSwapFrom(null); return; }
    setUpdateError('');
    api.timetable.swapSlots({ slotId1: swapFrom.id, slotId2: slot.id })
      .then(() => { setSwapFrom(null); onSlotUpdate(); })
      .catch((e) => setUpdateError(e.message));
  };

  if (!grid.data || sectionIds.length === 0) {
    return <div className="card">No timetable data to display.</div>;
  }

  // Header uses Mon-Thu periods (same 6 labels for all days)
  const headerPeriods = MON_THU_PERIODS;

  return (
    <div className="card">
      <h2>Timetable – Version {grid.versionNumber}</h2>
      {updateError && <div className="error-msg">{updateError}</div>}
      {swapFrom && <p>Click another slot to swap with the selected one.</p>}
      {sectionIds.map((sectionId) => {
        const rows = grid.data[sectionId] || [];
        const firstCell = rows[0]?.find((c) => c?.classSectionName) || rows[0]?.[0];
        const sectionName = firstCell?.classSectionName || `Section ${sectionId}`;

        return (
          <div key={sectionId} style={{ marginBottom: '2rem' }}>
            <h3 style={{ marginBottom: '0.5rem' }}>{sectionName}</h3>
            <div style={{ overflowX: 'auto' }}>
              <table className="timetable-table">
                <thead>
                  <tr>
                    <th>Day</th>
                    {headerPeriods.map((p) => (
                      <th key={p.index} style={{ minWidth: '90px', fontSize: '0.8rem' }}>
                        <div>{p.label}</div>
                        <div style={{ fontWeight: 400, fontSize: '0.7rem' }}>{p.time}</div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row, dayIndex) => {
                    const periods = getPeriodsForDay(dayIndex);
                    return (
                      <tr key={dayIndex}>
                        <td style={{ fontWeight: 600, whiteSpace: 'nowrap' }}>
                          {dayLabels[dayIndex] || `Day ${dayIndex + 1}`}
                        </td>
                        {periods.map((p) => {
                          const cell = row ? row[p.index] : null;
                          return (
                            <td key={p.index} className="timetable-cell">
                              {cell ? (
                                <div className="slot-content">
                                  <div className="slot-subject">{cell.subjectName || '—'}</div>
                                  <div className="slot-teacher">{cell.teacherName || ''}</div>
                                  <div className="slot-room">{cell.roomName || ''}</div>
                                  <div className="slot-actions">
                                    <button type="button" className="slot-btn"
                                      title={cell.isLocked ? 'Unlock' : 'Lock'}
                                      onClick={() => handleLock(cell)}>
                                      {cell.isLocked ? '🔒' : '🔓'}
                                    </button>
                                    
                                    <button type="button"
                                      className={`slot-btn ${swapFrom?.id === cell.id ? 'active' : ''}`}
                                      title="Swap"
                                      onClick={() => handleSwapClick(cell)}>
                                      ⇄
                                    </button>
                                  </div>
                                </div>
                              ) : (
                                <span className="slot-empty">—</span>
                              )}
                            </td>
                          );
                        })}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        );
      })}
    </div>
  );
}
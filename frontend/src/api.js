import { API_BASE } from './App';

async function request(path, options = {}) {
  const url = `${API_BASE}${path}`;
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  departments: {
    list: () => request('/departments'),
    get: (id) => request(`/departments/${id}`),
    create: (body) => request('/departments', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) => request(`/departments/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id) => request(`/departments/${id}`, { method: 'DELETE' }),
  },
  teachers: {
    list: (departmentId) => request(departmentId ? `/teachers?departmentId=${departmentId}` : '/teachers'),
    get: (id) => request(`/teachers/${id}`),
    create: (body) => request('/teachers', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) => request(`/teachers/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id) => request(`/teachers/${id}`, { method: 'DELETE' }),
  },
  subjects: {
    list: () => request('/subjects'),
    get: (id) => request(`/subjects/${id}`),
    create: (body) => request('/subjects', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) => request(`/subjects/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id) => request(`/subjects/${id}`, { method: 'DELETE' }),
  },
  courses: {
    list: () => request('/courses'),
    get: (id) => request(`/courses/${id}`),
    create: (body) => request('/courses', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) => request(`/courses/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id) => request(`/courses/${id}`, { method: 'DELETE' }),
  },
  semesters: {
    list: (courseId) => request(courseId ? `/semesters?courseId=${courseId}` : '/semesters'),
    get: (id) => request(`/semesters/${id}`),
    create: (body) => request('/semesters', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) => request(`/semesters/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id) => request(`/semesters/${id}`, { method: 'DELETE' }),
  },
  rooms: {
    list: () => request('/rooms'),
    get: (id) => request(`/rooms/${id}`),
    create: (body) => request('/rooms', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) => request(`/rooms/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id) => request(`/rooms/${id}`, { method: 'DELETE' }),
  },
  classSections: {
    list: (semesterId) => request(semesterId ? `/class-sections?semesterId=${semesterId}` : '/class-sections'),
    get: (id) => request(`/class-sections/${id}`),
    create: (body) => request('/class-sections', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) => request(`/class-sections/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id) => request(`/class-sections/${id}`, { method: 'DELETE' }),
  },
  allocations: {
    list: (semesterId, academicYear) =>
      request(`/allocations?semesterId=${semesterId}&academicYear=${encodeURIComponent(academicYear)}`),
    create: (body) => request('/allocations', { method: 'POST', body: JSON.stringify(body) }),
    delete: (id) => request(`/allocations/${id}`, { method: 'DELETE' }),
  },
  timetable: {
    generate: (body) => request('/timetable/generate', { method: 'POST', body: JSON.stringify(body) }),
    getVersion: (versionId) => request(`/timetable/version/${versionId}`),
    listVersions: (semesterId) => request(`/timetable/semester/${semesterId}/versions`),
    updateSlot: (body) => request('/timetable/slot', { method: 'PATCH', body: JSON.stringify(body) }),
    swapSlots: (body) => request('/timetable/slot/swap', { method: 'POST', body: JSON.stringify(body) }),
    activateVersion: (versionId) =>
    request(`/timetable/version/${versionId}/activate`, { method: 'PATCH' }),
    deleteVersion: (versionId) =>
    request(`/timetable/version/${versionId}`, { method: 'DELETE' }),
  },
};

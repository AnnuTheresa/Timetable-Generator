import React from 'react';
import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import DataEntry from './pages/DataEntry';
import TimetableGenerate from './pages/TimetableGenerate';
import './App.css';

const API_BASE = process.env.REACT_APP_API_URL || '/api';

export { API_BASE };

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <nav className="nav">
          <NavLink to="/" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Data Entry</NavLink>
          <NavLink to="/timetable" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Timetable</NavLink>
        </nav>
        <main className="main">
          <Routes>
            <Route path="/" element={<DataEntry />} />
            <Route path="/timetable" element={<TimetableGenerate />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;

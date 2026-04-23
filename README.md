Automatic Timetable Generator

This is a simple timetable generation project with a React frontend and a Spring Boot backend. It is mainly built for quick usage where you can enter data (departments, teachers, subjects, etc.) and generate a timetable based on that.

Tech Stack

Frontend: React
Backend: Spring Boot
Database: PostgreSQL

Features

Data entry for departments, teachers, subjects, courses, semesters, rooms, and class sections
Allocation management
Timetable generation
Basic editing (swap/update slots)


Make sure the following are installed:

Java (JDK 17 or compatible)
Maven
Node.js (with npm)
PostgreSQL

Database Setup (PostgreSQL)
Create a database (example name: timetable_db).

Update backend configuration (application.yml) with your credentials:

spring.datasource.url=jdbc:postgresql://localhost:5432/timetable_db
spring.datasource.username=your_username
spring.datasource.password=your_password
Ensure PostgreSQL is running on port 5432.

How to Run
1. Start Backend
Go to the backend folder

Run:
    mvn spring-boot:run

2. Start Frontend
Go to the frontend folder

Run:
    npm install
    npm start
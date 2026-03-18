# University Timetable Generation (CSP)

Full-stack application that generates weekly class timetables using a **Constraint Satisfaction Problem (CSP)** approach with backtracking, **MRV (Minimum Remaining Values)**, and **forward checking**.

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.2, JPA/Hibernate
- **Database:** PostgreSQL (or MySQL with profile `mysql`)
- **Build:** Maven
- **Frontend:** React 18 (HTML, CSS, JavaScript)

## Architecture

- **Layered:** Controllers → Services → Repositories → Entities
- **CSP engine** is in package `com.timetable.csp` and is **UI-independent** (no Spring in solver core)
- DTOs for API; normalized DB schema with audit fields (`createdAt`, `updatedAt`)

## Quick Start

### 1. Database

Create a database and set credentials in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/timetable_db
    username: postgres
    password: postgres
```

For MySQL, use profile `mysql` and set `spring.datasource.url`, `username`, `password` accordingly.

### 2. Backend

```bash
mvn spring-boot:run
```

API base: `http://localhost:8080/api`

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

Set `REACT_APP_API_URL=http://localhost:8080/api` if the API runs elsewhere. Default dev proxy is `http://localhost:8080`.

### 4. Initial Data

1. Create a **Department**.
2. Create **Courses** (e.g. two courses under the department).
3. Create **Semesters** (1–6 for each course).
4. Add **Teachers**, **Subjects** (theory and lab), **Rooms** (theory and lab).
5. Create **Class sections** per semester.
6. Set **Teacher–Subject–Semester allocations** for the academic year.
7. Go to **Timetable**, select semester and academic year, click **Generate Timetable**.

## API Overview

| Area | Endpoints |
|------|-----------|
| Departments | `GET/POST/PUT/DELETE /api/departments` |
| Teachers | `GET/POST/PUT/DELETE /api/teachers` |
| Subjects | `GET/POST/PUT/DELETE /api/subjects` |
| Courses | `GET/POST/PUT/DELETE /api/courses` |
| Semesters | `GET/POST/PUT/DELETE /api/semesters` |
| Rooms | `GET/POST/PUT/DELETE /api/rooms` |
| Class sections | `GET/POST/PUT/DELETE /api/class-sections` |
| Allocations | `GET/POST/DELETE /api/allocations?semesterId=&academicYear=` |
| Timetable | `POST /api/timetable/generate`, `GET /api/timetable/version/{id}`, `GET /api/timetable/semester/{id}/versions`, `PATCH /api/timetable/slot`, `POST /api/timetable/slot/swap` |

## Time Framework

- **Monday–Thursday:** 6 periods (P1–P6) with break and lunch (fixed, non-assignable).
- **Friday:** 6 periods with different timings (see `TimeFramework`).
- **Labs:** One block = 3 consecutive periods, either forenoon (P1–P3) or afternoon (P4–P6).

## Hard Constraints (CSP)

- No teacher double-booking (same time).
- No room/lab double-booking.
- One subject per class per slot.
- Required periods per subject satisfied; total periods within weekly capacity.
- Lab sessions as 3 consecutive periods; no clash with existing timetables.

## Soft Constraints

- Implemented via a scoring mechanism in the solver (e.g. preferred slots, even distribution).

## Editing and Regeneration

- **Lock slot:** PATCH slot with `isLocked: true` so it is unchanged on regeneration.
- **Not required:** PATCH slot with `notRequired: true` to exclude from requirements.
- **Swap:** POST `/api/timetable/slot/swap` with `slotId1` and `slotId2` (same version).

Conflict handling: if generation fails due to existing timetables, the API returns `{ conflict: true, message: "..." }`. The UI can offer to retry with “modify existing on conflict” (handled in business logic as needed).

## Project Structure

```
TimetableCSP/
├── pom.xml
├── src/main/java/com/timetable/
│   ├── TimetableCspApplication.java
│   ├── config/          # TimeFramework, CORS
│   ├── csp/             # CSP context, assignment, solver (no Spring)
│   ├── controller/      # REST APIs
│   ├── dto/
│   ├── entity/         # JPA entities
│   ├── repository/     # JPA repositories
│   └── service/        # Business logic, timetable generation
├── src/main/resources/application.yml
└── frontend/            # React UI
    ├── public/
    └── src/
        ├── api.js
        ├── App.js
        └── pages/
            ├── DataEntry.js
            └── TimetableGenerate.js
```

## License

MIT (or as you prefer).

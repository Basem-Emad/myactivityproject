# Activity Tracking System — Team Task Distribution

## 1. Project Overview

The Activity Tracking System is a web-based application for logging daily work activities, monitoring working hours, generating monthly summaries, and providing dashboards and reports.

The project is developed as part of a **one-month Summer Internship Program**.

The team consists of four contributors. Each contributor owns a complete business module across both backend and frontend where applicable.

---

# 2. Team Distribution

| Contributor | Branch | Main Ownership | Backend | Frontend |
|---|---|---|---|---|
| **Ahmed Yehia** | `feature/user-authentication` | Authentication & User Management | Users, Roles, Authentication, JWT, Security | Login, User Management |
| **Basem Emad** | `feature/master-data` | Master Data & Frontend Foundation | Activity Types, Activity Subjects | React/Vite Foundation, Master Data UI |
| **Daniel Akmal** | `feature/activity-entry` | Daily Activity Entry | Activity CRUD, Validation, Calculations, Filtering | Activity Entry & History |
| **Mostafa Haitham** | `feature/dashboard-reporting` | Dashboard & Reporting | Aggregation, Reports, Excel Export | Dashboard, Charts, Reports |

---

# 3. Ahmed Yehia — Authentication & User Management

## Branch

```text
feature/user-authentication
```

## Main Responsibility

Ahmed owns everything related to users, roles, authentication, authorization, and JWT security.

## Backend Tasks

Implement:

- User entity
- Role entity / enum
- Authentication
- JWT generation
- JWT validation
- Spring Security configuration
- Password hashing
- Role-based authorization
- User CRUD
- Current-user endpoint

### Suggested APIs

```http
POST   /api/auth/login
POST   /api/auth/register

GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

## Frontend Tasks

Create:

- Login page
- User management page
- User list
- Create user
- Edit user
- Delete user
- Role assignment
- Protected routes
- JWT handling

### Authentication Flow

```text
Login
  ↓
Receive JWT
  ↓
Store Token
  ↓
Attach Token to API Requests
  ↓
Protected Routes
  ↓
Role-Based Authorization
```

## Testing

Focus on:

- Successful login
- Invalid credentials
- Password hashing
- JWT validation
- Unauthorized requests
- Role authorization
- User CRUD

---

# 4. Basem Emad — Master Data & Frontend Foundation

## Branch

```text
feature/master-data
```

## Main Responsibility

Basem owns the initial React/Vite frontend foundation and the master-data module.

---

## Phase 1 — Frontend Foundation

The frontend application must be created **once only**.

Create:

```text
frontend/
├── src/
│   ├── components/
│   ├── pages/
│   ├── layouts/
│   ├── services/
│   ├── hooks/
│   ├── utils/
│   └── App.jsx
```

Set up:

- React
- Vite
- React Router
- Axios
- UI framework if agreed by the team
- Basic application layout
- Navigation
- API client
- Environment configuration
- Common error handling
- Shared components

After this foundation is merged into `main`, all contributors must use the same frontend application.

---

## Phase 2 — Master Data

### Backend

Implement:

```text
ActivityType
ActivitySubject
```

### Suggested APIs

```http
GET    /api/activity-types
POST   /api/activity-types
PUT    /api/activity-types/{id}
DELETE /api/activity-types/{id}

GET    /api/activity-subjects
POST   /api/activity-subjects
PUT    /api/activity-subjects/{id}
DELETE /api/activity-subjects/{id}
```

### Business Rules

Implement validation for:

- Required names
- Duplicate prevention
- Active/inactive status
- Invalid input

## Frontend

Create:

- Activity Types list
- Create Activity Type
- Edit Activity Type
- Delete Activity Type
- Activity Subjects list
- Create Activity Subject
- Edit Activity Subject
- Delete Activity Subject
- Validation messages
- API error handling

## Testing

Test:

- CRUD operations
- Duplicate activity type
- Duplicate activity subject
- Invalid data
- Validation rules

---

# 5. Daniel Akmal — Activity Entry

## Branch

```text
feature/activity-entry
```

## Main Responsibility

Daniel owns the core daily activity module and its business rules.

## Backend

Create the `Activity` entity.

### Suggested Fields

```text
id
user
activityType
activitySubject
date
startTime
endTime
duration
description
```

### APIs

```http
GET    /api/activities
GET    /api/activities/{id}

POST   /api/activities
PUT    /api/activities/{id}
DELETE /api/activities/{id}
```

### Filtering

Support filters such as:

```http
GET /api/activities?date=2026-08-11

GET /api/activities?from=2026-08-01&to=2026-08-31

GET /api/activities?activityType=1

GET /api/activities?user=5
```

---

## Business Logic

### Duration Calculation

Example:

```text
09:00 → 10:30
= 1.5 hours
```

The duration should be calculated by the backend rather than trusted from the frontend.

---

## Overlap Validation

Example:

```text
Activity 1
09:00 ───────── 11:00

Activity 2
10:30 ───────── 12:00
```

Result:

```text
REJECT
```

Overlapping activities must not be allowed for the same user.

Adjacent activities are allowed:

```text
09:00 ───────── 11:00
11:00 ───────── 12:00
```

Result:

```text
ALLOW
```

---

## Other Validation

Validate:

- End time must be greater than start time
- Required activity type
- Required activity subject
- Valid date
- Valid user
- No overlapping activities
- Required fields
- Appropriate duration

## Frontend

Create:

- Activity list
- Add activity
- Edit activity
- Delete activity
- Activity details
- Activity filters
- Date filter
- Date-range filter
- Activity type filter
- Activity subject filter
- User filter

## Testing

This module contains important business logic and should have strong unit tests.

Test:

- Duration calculation
- Overlap detection
- Adjacent activities
- Invalid start/end times
- Filtering
- CRUD operations
- Validation rules

---

# 6. Mostafa Haitham — Dashboard & Reporting

## Branch

```text
feature/dashboard-reporting
```

## Main Responsibility

Mostafa owns reporting, aggregation, dashboard APIs, charts, monthly summaries, and Excel export.

## Backend

Implement reporting services and APIs.

### Monthly Summary

```http
GET /api/reports/monthly?month=2026-08
```

Example response:

```json
{
  "totalDays": 20,
  "totalHours": 152.5
}
```

### Hours by Activity Type

```http
GET /api/reports/hours-by-type?month=2026-08
```

Example:

```json
[
  {
    "activityType": "Development",
    "hours": 80
  },
  {
    "activityType": "Meeting",
    "hours": 25
  }
]
```

### Hours by Activity Subject

```http
GET /api/reports/hours-by-subject?month=2026-08
```

### Monthly Details

```http
GET /api/reports/monthly-details?month=2026-08
```

---

## Excel Export

Implement:

```http
GET /api/reports/export/excel?month=2026-08
```

Suggested file:

```text
August-2026-Activity-Report.xlsx
```

Suggested sheets:

```text
Summary
Activities
By Activity Type
By Subject
```

## Frontend

### Dashboard

Display:

```text
Total Working Days
Total Working Hours
Average Hours / Day
```

### Charts

Recommended charts:

- Hours by Activity Type
- Hours by Activity Subject
- Daily Working Hours

Possible chart types:

```text
Pie Chart
Bar Chart
Line Chart
```

## Reports

Create:

- Monthly report
- Month selector
- Summary section
- Activity details
- Export Excel button

## Testing

Test:

- Monthly totals
- Total working days
- Total hours
- Aggregation by type
- Aggregation by subject
- Excel generation
- Empty-month handling

---

# 7. Team Dependencies

The modules are connected and should not be developed as completely isolated systems.

```text
                 ┌──────────────────────┐
                 │   Backend Foundation │
                 └──────────┬───────────┘
                            │
            ┌───────────────┼────────────────┐
            ↓               ↓                ↓
       Authentication   Master Data      Activity
            │               │                │
            └───────────────┴────────────────┘
                            ↓
                       Reporting
                            ↓
                         Dashboard
```

## Ahmed → Daniel

Daniel depends on:

- User
- Authentication
- Authorization

Activities belong to users.

## Basem → Daniel

Daniel depends on:

- Activity Type
- Activity Subject

These are required when creating activities.

## Daniel → Mostafa

Mostafa depends on activity data for:

- Total days
- Total hours
- Hours by activity type
- Hours by activity subject
- Monthly reports

Therefore, the activity API contract should be agreed early.

---

# 8. Week-by-Week Plan

## Week 1 — Foundation

### Ahmed

- User entity
- Role
- Database structure
- Security design
- JWT design

### Basem

- React + Vite
- React Router
- Axios
- Layout
- UI foundation
- Activity Type
- Activity Subject

### Daniel

- Activity entity
- Repository
- Service
- DTOs
- API contract
- Database relationships

### Mostafa

- Dashboard requirements
- Report API design
- Aggregation requirements
- Excel library investigation

### Team

Agree on:

- Java version
- Spring Boot version
- Package naming
- API naming
- DTO conventions
- Error response format
- Database naming
- Authentication flow
- Git workflow

---

# 9. Week 2 — Core Features

## Ahmed

- Login
- JWT
- User CRUD
- Role authorization
- Login UI

## Basem

- Master Data APIs
- Master Data UI
- Validation

## Daniel

- Activity CRUD
- Duration calculation
- Overlap validation
- Filtering
- Activity UI

## Mostafa

- Report services
- Monthly calculations
- Aggregation endpoints

---

# 10. Week 3 — Integration

## Ahmed

- Protected APIs
- Permission testing
- Frontend authentication integration

## Basem

- Master data integration
- Shared UI improvements
- Frontend cleanup

## Daniel

Integrate:

```text
Activity
   +
User
   +
Activity Type
   +
Activity Subject
```

## Mostafa

- Dashboard UI
- Charts
- Monthly report
- Backend/frontend integration

---

# 11. Week 4 — Finalization

## Ahmed

- Security testing
- Authorization testing
- Bug fixing

## Basem

- UI consistency
- Validation
- Frontend bug fixing

## Daniel

- Business-rule testing
- Activity bug fixing
- Integration testing

## Mostafa

- Excel export
- Dashboard polishing
- Report testing

## Everyone

- Final integration
- Bug fixing
- Testing
- Swagger documentation
- README updates
- Docker Compose
- Final demo preparation

---

# 12. Code Review Distribution

Each contributor should review another contributor's work.

| Developer | Owns | Reviews |
|---|---|---|
| Ahmed | Authentication & Users | Daniel |
| Basem | Master Data & Frontend Foundation | Ahmed |
| Daniel | Activity Entry | Mostafa |
| Mostafa | Dashboard & Reporting | Basem |

The goal is to ensure that every contributor understands at least one module outside their own area.

---

# 13. Development Rules

## Git

Before starting work:

```bash
git checkout main
git pull origin main
```

Create a feature branch:

```bash
git checkout -b feature/<feature-name>
```

Example:

```bash
git checkout -b feature/activity-entry
```

Commit focused changes:

```bash
git add .
git commit -m "feat: add activity creation endpoint"
```

Push:

```bash
git push origin feature/activity-entry
```

Create a Pull Request:

```text
feature/* → main
```

---

# 14. Commit Convention

Use simple conventional commits.

```text
feat: add activity creation endpoint

fix: prevent overlapping activities

test: add activity service tests

docs: update project documentation

refactor: simplify dashboard service

chore: configure postgres
```

Keep commits:

- Small
- Focused
- Descriptive

Avoid large commits containing unrelated changes.

---

# 15. Collaboration Rules

- Always pull the latest `main` before starting new work.
- Work primarily inside the assigned feature area.
- Discuss changes to shared files before modifying them.
- Do not create duplicate backend applications.
- Do not create duplicate frontend applications.
- Agree API request/response contracts before frontend integration.
- Use Pull Requests for feature integration.
- Review another contributor's code before merge.
- Keep Swagger/OpenAPI documentation updated.
- Write tests for important business logic.
- Resolve merge conflicts carefully.
- Never overwrite another contributor's work to solve a merge conflict.
- Keep shared components reusable.
- Keep business logic inside backend services rather than the frontend.

---

# 16. Sensitive Files

Do not commit:

```text
.env
node_modules/
target/
.idea/
*.log
```

Never commit:

- Passwords
- Database credentials
- JWT secrets
- API keys
- Personal credentials

Use environment variables or local configuration.

---

# 17. Recommended Final Repository

```text
activity-tracking-system/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/activitytracking/
│   │   │           ├── auth/
│   │   │           ├── user/
│   │   │           ├── masterdata/
│   │   │           ├── activity/
│   │   │           └── reporting/
│   │   │
│   │   └── test/
│   │
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── layouts/
│   │   ├── pages/
│   │   │   ├── auth/
│   │   │   ├── users/
│   │   │   ├── master-data/
│   │   │   ├── activities/
│   │   │   └── dashboard/
│   │   ├── services/
│   │   ├── hooks/
│   │   └── utils/
│   └── package.json
│
├── docs/
│   ├── HLD/
│   ├── LLD/
│   └── requirements/
│
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

# 18. Definition of Done

A feature is complete when:

- Backend functionality works correctly.
- Required frontend functionality is integrated.
- Validation is implemented.
- Error handling is implemented.
- APIs can be tested using Swagger/Postman.
- Important business logic has automated tests.
- Code has been reviewed.
- Changes are merged successfully.
- Documentation is updated where required.
- The feature works with the integrated application.

---

# 19. Final MVP

The final application should provide:

- User authentication
- User and role management
- Activity Type management
- Activity Subject management
- Daily activity logging
- Automatic working-hour calculation
- Activity overlap validation
- Activity filtering
- Monthly Total Days
- Monthly Total Hours
- Hours by Activity Type
- Hours by Activity Subject
- Dashboard charts
- Monthly reports
- Excel export
- Swagger/OpenAPI documentation
- Automated tests for important business logic
- PostgreSQL integration
- Docker Compose setup

---

# 20. Final Team Principle

Each contributor owns a **business module end-to-end** rather than working only on backend or frontend.

```text
Ahmed
Authentication ──────────────→ Backend + Frontend

Basem
Master Data ────────────────→ Backend + Frontend

Daniel
Activity Entry ─────────────→ Backend + Frontend

Mostafa
Dashboard & Reporting ─────→ Backend + Frontend
```

This approach gives the interns practical experience with:

- Backend development
- Frontend development
- REST APIs
- Database design
- Authentication
- Business logic
- Testing
- Git
- Pull Requests
- Code reviews
- Integration
- Reporting
- Deployment preparation

The goal is not only to finish the MVP, but to make the project resemble a real software-team workflow.

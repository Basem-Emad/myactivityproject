# Activity Tracking System

A web-based activity tracking and reporting system for logging daily work activities, monitoring working hours, and generating monthly summaries and dashboards.

This project is developed as part of a **one-month Summer Internship Program**.

---

## Project Documentation

Before starting development, review the project documentation available under `docs/`.

```text
docs/
├── HLD/
├── LLD/
└── requirements/
```

The documentation contains:

* Project requirements and scope
* High-Level Design (HLD)
* Low-Level Design (LLD)
* Functional requirements
* Business rules
* Required screens
* Acceptance criteria
* Work distribution

---

## Expected Project Structure

The development team is responsible for creating the backend and frontend applications.

The final repository should follow approximately this structure:

```text
activity-tracking-system/
├── backend/
│   └── Spring Boot application
│
├── frontend/
│   └── React + Vite application
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

Minor structural changes are acceptable if agreed by the team.

---

## Technology Stack

### Backend

* Java 17+
* Spring Boot
* Spring Web
* Spring Data JPA
* Bean Validation
* Spring Security / JWT
* PostgreSQL
* Maven
* Swagger / OpenAPI

### Frontend

**Recommended: React + Vite**

The frontend should consume the Spring Boot REST APIs.

Next.js or another frontend technology may be used if agreed by the team, but React + Vite is recommended for keeping the one-month project scope simple.

---

## Team & Work Distribution

Each contributor owns a functional area across both backend and frontend where applicable.

| Contributor     | Branch                        | Responsibility                                                                       |
| --------------- | ----------------------------- | ------------------------------------------------------------------------------------ |
| Ahmed Yehia     | `feature/user-authentication` | Users, roles, authentication, JWT, Login UI and User Management                      |
| Basem Emad      | `feature/master-data`         | Activity Types, Activity Subjects, React/Vite frontend foundation and master-data UI |
| Daniel Akmal    | `feature/activity-entry`      | Activity CRUD, time calculation, validation, filtering and Activity UI               |
| Mostafa Haitham | `feature/dashboard-reporting` | Dashboard, monthly summaries, charts, reporting and Excel export                     |

Detailed requirements for each module are available in the project documentation.

---

## Initial Setup Responsibility

The repository intentionally does not contain pre-built backend or frontend applications.

Project initialization is considered part of the internship work.

Before feature development begins, the team should agree on:

* Java and Spring Boot versions
* Backend package naming
* PostgreSQL configuration
* Frontend structure
* API conventions
* Common error-response format
* Git workflow

Only **one initial backend structure** and **one initial frontend structure** should be created.

Do not create multiple independent Spring Boot or React applications for the same project.

### Backend Foundation

The team should create one Spring Boot application under:

```text
backend/
```

After the initial backend structure is merged into `main`, all backend contributors should build on that structure.

### Frontend Foundation

The frontend foundation should be created once under:

```text
frontend/
```

React + Vite is recommended.

After the frontend foundation is merged into `main`, all contributors should use the same frontend application.

---

## Git Workflow

### 1. Get the latest main branch

Before starting:

```bash
git checkout main
git pull origin main
```

### 2. Create your feature branch

```bash
git checkout -b feature/<feature-name>
```

Example:

```bash
git checkout -b feature/activity-entry
```

### 3. Work on your assigned feature

Keep commits small and focused.

### 4. Commit your changes

Example:

```bash
git add .
git commit -m "feat: add activity creation endpoint"
```

### 5. Push your branch

```bash
git push origin feature/activity-entry
```

### 6. Create a Pull Request

Create a Pull Request:

```text
feature/* → main
```

The Pull Request should describe:

* What was implemented
* Backend changes
* Frontend changes
* Testing performed
* Any known limitations

### 7. Code Review

At least one other team member should review the Pull Request before merge.

Resolve review comments before merging.

---

## Branches

Main development branches:

```text
main

feature/user-authentication
feature/master-data
feature/activity-entry
feature/dashboard-reporting
```

Additional short-lived branches may be created when required, for example:

```text
fix/activity-overlap-validation
fix/dashboard-calculation
docs/update-api-documentation
```

Do not push feature implementation directly to `main`.

---

## Commit Convention

Use simple conventional commit messages:

```text
feat: add activity creation endpoint

fix: prevent overlapping activities

test: add activity service tests

docs: update project documentation

refactor: simplify dashboard service

chore: configure postgres
```

---

## Collaboration Rules

* Always pull the latest `main` before starting new work.
* Work primarily inside your assigned feature area.
* Discuss changes to shared files before modifying them.
* Do not create duplicate backend or frontend applications.
* Agree API request/response contracts before frontend integration.
* Use Pull Requests for feature integration.
* Review another contributor's code before merge.
* Keep Swagger/OpenAPI documentation updated.
* Write tests for important business logic.
* Resolve merge conflicts carefully rather than overwriting another contributor's work.

---

## Sensitive Files

Do not commit:

```text
.env
node_modules/
target/
.idea/
*.log
```

Do not commit:

* Passwords
* Database credentials
* JWT secrets
* API keys
* Personal credentials

Use environment variables or local configuration for sensitive values.

---

## Main MVP Features

The final application should provide:

* User authentication
* User and role management
* Activity Type management
* Activity Subject management
* Daily activity logging
* Automatic working-hour calculation
* Activity overlap validation
* Activity filtering
* Monthly Total Days
* Monthly Total Hours
* Hours by Activity Type
* Hours by Activity Subject
* Dashboard charts
* Monthly report
* Excel export

Refer to the requirements document for detailed acceptance criteria.

---

## Internship Duration

The project must be completed within the **one-month internship period**.

A suggested delivery plan is:

**Week 1**

* Project initialization
* Database setup
* Backend/frontend foundations
* User/Auth and Master Data

**Week 2**

* Activity Entry
* Business validation
* Activity frontend screens

**Week 3**

* Dashboard
* Reporting
* Frontend/backend integration

**Week 4**

* Excel export
* Testing
* Bug fixing
* Documentation
* Final integration
* Final demo

---

## Definition of Done

A feature is considered complete when:

* Backend functionality works correctly.
* Required frontend functionality is integrated.
* Validation and error handling are implemented.
* APIs can be tested through Swagger/Postman.
* Important business logic has tests.
* Code has been reviewed.
* Changes are merged successfully.
* Documentation is updated where required.

---

## Final Deliverables

By the end of the internship, the repository should contain:

```text
backend/
frontend/
docs/
docker-compose.yml
README.md
.gitignore
```

And the team should deliver:

* Working Spring Boot REST API
* Working frontend application
* PostgreSQL database integration
* Dashboard and reporting
* Excel export
* Swagger/OpenAPI documentation
* Tests for important business logic
* Setup/run instructions
* Final integrated demonstration

---

## Backend Setup Guide (Master Data Module)

This section covers how to run the backend locally, specific to the Master Data module (Activity Types & Activity Subjects) developed by Basem Emad.

### Prerequisites

* JDK 17+
* Docker Desktop (running)
* Maven (or use the included `mvnw` wrapper)

### 1. Start the Database

From the project root:

```bash
docker compose up -d
```

This starts a PostgreSQL 16 container with the following default credentials (override via environment variables for production):

* Database: `activitytracking_db`
* Username: `activitytracking_user`
* Password: `activitytracking_pass`

To stop the database (data is preserved):

```bash
docker compose down
```

### 2. Configure Environment Variables (Optional)

The backend reads credentials from environment variables with local defaults:

```bash
DB_USER=activitytracking_user
DB_PASSWORD=activitytracking_pass
```

If not set, the defaults above are used automatically (see `backend/src/main/resources/application.properties`).

### 3. Run the Backend

From the `backend/` directory:

```bash
./mvnw spring-boot:run
```

Or run `ActivityTrackingApplication.java` directly from your IDE.

The backend starts on `http://localhost:8080`. Flyway automatically applies database migrations on startup.

### 4. Explore the API

* Swagger UI: `http://localhost:8080/swagger-ui.html`
* OpenAPI spec: `http://localhost:8080/v3/api-docs`

### 5. Run Tests

From the `backend/` directory:

```bash
./mvnw test
```

### Master Data Endpoints

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/activity-types` | Create an Activity Type |
| GET | `/api/activity-types` | List all Activity Types |
| GET | `/api/activity-types/{id}` | Get an Activity Type by ID |
| PUT | `/api/activity-types/{id}` | Update an Activity Type |
| DELETE | `/api/activity-types/{id}` | Deactivate an Activity Type |
| POST | `/api/activity-subjects` | Create an Activity Subject |
| GET | `/api/activity-subjects` | List all Activity Subjects |
| GET | `/api/activity-subjects/{id}` | Get an Activity Subject by ID |
| PUT | `/api/activity-subjects/{id}` | Update an Activity Subject |
| DELETE | `/api/activity-subjects/{id}` | Deactivate an Activity Subject |

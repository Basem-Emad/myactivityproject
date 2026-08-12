# Activity Tracking System — Frontend

Frontend application for the Activity Tracking System internship project.

The application is built with React and Vite and consumes the Spring Boot REST API provided by the backend.

For the complete project scope, business rules, and team responsibilities, see the repository root `README.md` and the project documentation under `docs/`.

## Tech Stack

- React
- TypeScript
- Vite
- React Router
- Axios
- ESLint

## Getting Started

From the `frontend` directory:

```bash
npm install
npm run dev
```

The development server will normally run at:

```text
http://localhost:5173
```

## Available Scripts

Start the development server:

```bash
npm run dev
```

Run ESLint:

```bash
npm run lint
```

Create a production build:

```bash
npm run build
```

Preview the production build locally:

```bash
npm run preview
```

## Environment Configuration

Create a local `.env` file inside `frontend/`.

Example:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

Use `.env.example` as the reference for required frontend environment variables.

Do not commit `.env` files containing local or sensitive configuration.

Frontend environment variables are client-side configuration only. Do not place database passwords, JWT secrets, API secrets, or other private credentials in the frontend.

## Project Structure

```text
src/
├── assets/
├── components/
├── hooks/
├── layouts/
├── pages/
│   ├── activities/
│   ├── auth/
│   ├── dashboard/
│   ├── master-data/
│   ├── reports/
│   └── users/
├── services/
├── utils/
├── App.tsx
├── index.css
└── main.tsx
```

### Directory Responsibilities

`components/`
Shared and reusable UI components.

`layouts/`
Shared application layouts such as the authenticated application shell.

`pages/`
Route-level application screens grouped by feature.

`services/`
API access and shared HTTP services.

`hooks/`
Reusable React hooks.

`utils/`
Shared formatting and utility functions.

## Routing

The frontend uses React Router.

Current application routes include:

```text
/login
/dashboard
/activities
/activities/new
/activities/:id/edit
/users
/activity-types
/activity-subjects
/reports/monthly
```

The Login page is outside the authenticated application shell.

Authenticated pages will share the main application layout containing the sidebar, top header, navigation, and page content area.

Route authorization will be integrated with the authentication module once the shared authentication contract is available.

## API Client

Shared API requests should use the Axios client in:

```text
src/services/api.ts
```

Do not create separate Axios configurations inside individual pages.

The base API URL comes from:

```text
VITE_API_BASE_URL
```

Authentication headers and common API error handling will be added to the shared API layer as the authentication integration is completed.

## Design

Frontend design and implementation rules are documented in:

```text
docs/DESIGN.md
```

The design guide contains:

- application layout
- navigation rules
- design tokens
- typography and spacing
- reusable component guidance
- loading, empty, and error states
- accessibility requirements
- approved frontend behavior

The approved Figma design is also linked from the design guide.

Project requirements, HLD/LLD, and acceptance criteria remain authoritative if there is any conflict with the visual design.

## Development Guidelines

Keep shared UI patterns reusable instead of duplicating them inside individual pages.

Keep API access in the shared service layer.

Keep business rules and reporting calculations in the backend rather than reproducing them in React.

Use TypeScript for frontend source files.

Use PascalCase for React component names and camelCase for props and variables.

Run both linting and the production build before opening a Pull Request:

```bash
npm run lint
npm run build
```

Do not commit:

```text
node_modules/
dist/
.env
```

## Git Workflow

Frontend work follows the repository Git workflow.

Work from a feature branch, keep commits focused, push the branch, and open a Pull Request into `main`.

Do not push feature implementation directly to `main`.

## Documentation

For more information, see:

```text
../README.md
../docs/
docs/DESIGN.md
```

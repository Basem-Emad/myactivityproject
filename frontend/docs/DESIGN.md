# Activity Tracking System — Frontend Design Guide

This document is the shared design and implementation guide for the Activity Tracking System frontend. It keeps the React application visually consistent, predictable to use, and aligned with the internship requirements.

It is intentionally practical. If a rule does not help us build or review the UI, it probably does not belong here.

## Design reference

**Figma:** [Activity Tracking System — UI Design](https://www.figma.com/design/LO9VZdPgECgANZDJg9FdQu/UntitleActivity-Tracking-System-%E2%80%94-UI-Designd?node-id=0-1&t=P0i0PVXpZdWz3WvO-1)

Use the Figma file together with this guide when implementing frontend screens and shared components.

### Source of truth

When something conflicts, use this order:

1. Repository README, project requirements, HLD/LLD, and acceptance criteria
2. Agreed backend/API contracts
3. Approved Figma screens and this design guide

The design should explain the requirements, not expand them. Do not add product features because they are common in other dashboards.

---

## Frontend baseline

The project uses one shared frontend application.

- React + Vite
- TypeScript
- React Router
- Axios
- Shared layout, navigation, API client, error handling, and reusable components
- A UI framework may be added only if the team agrees on it

Do not create a second React application or a competing shared-component structure.

Recommended source layout:

```text
src/
├── components/
├── pages/
├── layouts/
├── services/
├── hooks/
├── utils/
├── App.tsx
└── main.tsx
```

---

## Design principles

### Keep it clear

This is an internal work-tracking application. Prioritize readable data, obvious actions, and predictable page structure over decoration.

### Keep patterns consistent

The same kind of action should look and behave the same everywhere. Reuse buttons, tables, dialogs, empty states, loading states, and error patterns instead of recreating them per page.

### Always show system state

Async screens and actions must communicate what is happening. Use loading, empty, success, and error states where they make sense.

### Design for keyboard and screen-reader users

Accessibility is part of the component design, not a final cleanup task.

### Protect the MVP

Do not introduce features outside the README, project requirements, or agreed team scope.

---

## Application shell

Authenticated screens share one application shell.

| Area | Rule |
| --- | --- |
| Sidebar | Fixed `240px` width on desktop |
| Top header | `64px` height |
| Main content | Fluid width, max `1440px`, `24px` page padding |
| Page scroll | Vertical scrolling is allowed |
| Horizontal scroll | Do not allow page-level horizontal scrolling |
| Target desktop | Must work cleanly at normal laptop sizes such as `1366 × 768` |
| Mobile | Advanced mobile behavior is not required for the MVP |

The top header contains the authenticated user's **name**, **role**, and **Logout** action.

Do not add breadcrumbs, global search, notifications, Profile, or Settings unless they become an agreed requirement.

---

## Navigation

Navigation must reflect the authenticated user's role.

| Role | Navigation |
| --- | --- |
| Employee | Dashboard, My Activities, Add Activity, Monthly Report |
| Admin | Employee items + Users, Activity Types, Activity Subjects |
| Manager | Dashboard, Monthly Report, and authorized team activities/summaries |

A team or employee selector may be added for managers only when the API and authorization contract support it.

Activity approval is optional and is not part of the required MVP.

### Do not add

- Global search
- System logs
- Team Performance / Team Stats as a separate module
- Resource Planning
- Personal Stats
- Profile or Settings
- Persistent notifications
- Advanced approval workflows

---

## MVP screen coverage

| Screen | Required UI |
| --- | --- |
| Login | Email, password, validation, authentication error, successful redirect |
| Monthly Dashboard | Month filter, Total Hours, Total Days, grouped totals, simple charts |
| My Activities | Activity list, date/type/subject filters, edit/delete actions, loading/empty/error states |
| Add / Edit Activity | Date, From, To, Activity Type, Activity Subject, Task Description, calculated duration, validation errors |
| Activity Types | List, create, edit, deactivate/delete |
| Activity Subjects | List, create, edit, deactivate/delete |
| Users | Admin list/create/edit/delete users and role assignment |
| Monthly Report | Monthly summary, grouped data, activity details, Excel export |

Do not create a separate public home page for the MVP.

---

## Design tokens

### Colours

| Token | Value | Use |
| --- | --- | --- |
| Primary | `#2563EB` | Navigation, links, primary actions, chart emphasis |
| Primary Hover | `#1D4ED8` | Primary hover state |
| Success | `#16A34A` | Successful operations and valid status |
| Warning | `#D97706` | Warning feedback |
| Danger | `#DC2626` | Errors and destructive actions |
| Accent | `#C2410C` | Optional decorative emphasis only |
| Background | `#F9FAFB` | App background |
| Surface | `#FFFFFF` | Cards, tables, dialogs, panels |
| Primary Text | `#111827` | Headings and main content |
| Secondary Text | `#4B5563` | Supporting text |
| Disabled Text | `#9CA3AF` | Disabled content |
| Border | `#E5E7EB` | Dividers, card borders, table rules, skeleton boundaries |
| Control Border | `#6B7280` | Visible input/control boundaries |
| Subtle Background | `#F3F4F6` | Table headers, skeletons, subtle states |

Use semantic colours for their intended meaning. Do not use colour as the only way to communicate status.

Every interactive control needs a visible keyboard focus state.

### Typography

- Font: `Inter, ui-sans-serif, system-ui, sans-serif`
- Page heading: `24px / 32px / 600`
- Section heading: `20px / 28px / 600`
- Metric value: `24–30px / 700`
- Large body: `16px / 24px`
- Default body: `14px / 20px`
- Labels and captions: `12px / 16px / 500`
- Table text: `14px / 20px`

### Spacing and shape

- Spacing scale: `4, 8, 12, 16, 24, 32, 48, 64px`
- Small control radius: `4px`
- Inputs, buttons, cards, dialogs: `8px`
- Small shadow: `0 1px 2px rgba(0, 0, 0, 0.05)`
- Medium shadow: `0 4px 6px -1px rgba(0, 0, 0, 0.1)`

---

## Shared components

Prefer reusable components and variants over page-specific copies.

Core shared pieces:

- `AppShell`
- `Sidebar`
- `TopHeader`
- `Button`
- `DataTable`
- `LoadingSkeleton`
- `EmptyState`
- `ErrorAlert`
- `ConfirmationDialog`
- `ToastNotification` for temporary feedback only
- `MonthPicker`
- `SummaryMetricCard`
- `ChartCard`
- `ExportExcelButton`
- `MonthlyActivityDetailsTable`

`Button` should support at least:

- primary
- secondary
- danger
- loading
- disabled

Use PascalCase for component names and camelCase for props.

---

## Forms and controls

- Standard control height: `40px`
- Icon-only action target: `48 × 48px`
- Inputs must have visible labels
- Placeholder text is not a replacement for a label
- Validation errors use a Danger treatment plus readable helper text
- Disabled controls must still look disabled
- When an action is disabled, make the reason understandable where relevant
- Keyboard focus uses a visible Primary focus ring
- Do not clip focus rings inside cards, tables, or dialogs

Month values use `YYYY-MM` in the data layer and a readable month/year label in the UI.

---

## Loading, empty, and error states

Pages that load remote data must handle all three states deliberately.

### Loading

- Keep the page structure stable
- Use skeletons instead of a full-page spinner
- Do not display stale values from a previous successful request
- Use `aria-busy="true"` on the loading region
- Include a screen-reader loading message when needed

### Empty

- Explain what is missing in plain language
- Provide the next useful action when one exists
- Do not render fake chart axes or placeholder data as if results exist

### Error

- Explain that the data could not be loaded
- Provide Retry when retrying makes sense
- Do not expose stack traces, database details, or internal server messages
- Use `role="alert"` or an equivalent assertive announcement for important errors

---

## Tables

Use the same table language across Users, Activities, Master Data, and Reports.

- Header background: `#F3F4F6`
- Row background: white
- Row hover: `#F9FAFB`
- Use subtle row dividers
- Right-align hours and duration columns
- Keep headers visible while rows are loading
- Replace loading rows with skeletons
- Show a contextual empty state when no rows exist
- On load failure, retain the table context and show `Data unavailable`
- Wide tables may scroll inside their own labelled container instead of forcing page-level horizontal scrolling

Monthly Activity Details must include:

- Date
- Start time
- End time
- Activity Type
- Activity Subject
- Task Description
- Formatted duration

---

## Screen-specific notes

### Login

The login screen contains only what the MVP needs:

- Email
- Password
- Client/server validation feedback
- Invalid-credentials error
- Redirect after successful authentication

Do not add Forgot Password, Remember Me, social login, support links, or registration unless the requirements change.

### User Management

User Management is Admin-only.

Approved flow:

- List users
- Create user
- Edit user
- Delete user
- Assign role

Create User uses:

- Name
- Email
- Password
- Role

Edit User uses:

- Name
- Email
- Role

Do not add password reset, account profile fields, or other user-management features unless they are added to the agreed contract.

Destructive deletion uses a confirmation dialog.

### Activity Types and Activity Subjects

The master-data UI must support:

- list
- create
- edit
- deactivate/delete
- required-name validation
- duplicate prevention
- API error handling

Active/inactive status exists in the master-data requirements. The exact delete-versus-deactivate behavior must follow the backend contract rather than being invented in the frontend.

### My Activities

The list must support:

- date filtering
- Activity Type filtering
- Activity Subject filtering
- edit
- delete
- loading, empty, and error states

Additional user/team filtering is only shown where authorization and the API support it.

### Add / Edit Activity

Required fields:

- Date
- From
- To
- Activity Type
- Activity Subject
- Task Description

The UI may display calculated duration, but the backend remains authoritative for duration.

Validation must surface:

- end time not later than start time
- required fields
- overlapping activity
- invalid server/API input

Adjacent activities are allowed. Overlapping activities for the same user are not.

---

## Monthly data rules

The frontend displays backend-provided reporting values. It must not recreate reporting business logic independently.

- **Total Days:** unique dates in the selected month with at least one activity
- **Total Hours:** sum of backend-calculated activity durations
- **Average Hours / Day:** Total Hours divided by Total Days; return zero when Total Days is zero
- Hours by Activity Type must reconcile with Total Hours
- Hours by Activity Subject must reconcile with Total Hours
- Daily Hours Trend must reconcile with Total Hours
- Dashboard and Monthly Report should use the same monthly-summary source
- Prefer integer duration minutes in the data layer
- Format durations for users as `24h 30m`, not `24.5h`

### Reference dataset

Use this only as test/demo data when validating the UI:

- Total Hours: `160h 30m`
- Total Days: `20`
- Average Hours / Day: `8h 2m`
- Activity Types:
  - Project `60h 0m`
  - Product `32h 0m`
  - POC `20h 0m`
  - Dev Plan `16h 0m`
  - Meeting `24h 30m`
  - Day Off `8h 0m`
- Activity Subjects:
  - BM Microfocus `65h 0m`
  - License Module `45h 0m`
  - Microservices `35h 0m`
  - Training `15h 30m`

Each grouped total and the daily trend should reconcile to `160h 30m`.

---

## Monthly Dashboard

The dashboard contains:

- Month Picker
- Total Hours
- Total Days
- Average Hours / Day
- Hours by Activity Type
- Hours by Activity Subject
- Daily Hours Trend

Use simple horizontal bars for grouped totals and a line chart for the daily trend.

### Dashboard states

| State | Behaviour |
| --- | --- |
| Populated | Show backend-provided metrics and chart data |
| Loading | Skeleton metrics/charts, disable Month Picker, no stale totals |
| Empty | Show `0h 0m`, `0`, `0h 0m`; message `No activity data for [month]`; provide Add Activity |
| Error | Show `—` in metrics, inline error + Retry, and `Data unavailable` in chart regions |

Do not use `Empty`, `Loading`, or `Error` in the visible page heading. Those labels are only for design/state names.

---

## Monthly Report

The report contains:

- Month Picker
- Export Excel
- Total Hours
- Total Days
- Average Hours / Day
- Hours by Activity Type table
- Hours by Activity Subject table
- Monthly Activity Details table

Dashboard and Report must show consistent values for the same month.

### Report states

| State | Behaviour |
| --- | --- |
| Populated | Show summary, grouped tables, activity details, enable export |
| Loading | Skeleton summary/tables, disable Month Picker and Export Excel |
| Empty | Show zero metrics, `No report data for [month]`, Add Activity, disable export |
| Error | Show `—` metrics, error + Retry, `Data unavailable`, disable data-dependent actions |

Never mix successful totals with unavailable report data from a failed request.

---

## Excel export

Only `.xlsx` export is part of the MVP.

Do not add PDF, CSV, email, or scheduled export.

### Export states

| State | Behaviour |
| --- | --- |
| Default | Enabled when monthly data exists |
| Exporting | Disable button, show spinner + `Exporting…`, prevent duplicate requests |
| Success | Trigger browser download and show a polite temporary success message |
| Failure | Show an assertive error with Retry |
| No data | Disable export and expose `No data to export` |

Suggested filename:

```text
Month-YYYY-Activity-Report.xlsx
```

Example:

```text
August-2026-Activity-Report.xlsx
```

Suggested worksheets:

- `Summary`
- `Activities`
- `By Activity Type`
- `By Subject`

---

## Charts

Keep charts simple and readable.

- Use horizontal bars and line charts for the MVP
- Do not use pie or donut charts
- Use labels of at least `12px`
- Tooltips show the category/date and formatted duration
- Do not rely on colour alone
- Every chart needs a textual or tabular equivalent
- Keep labels inside their own layout space so they are not clipped by card boundaries
- Keep at least `12px` between chart edges and the first/last plot line
- Put X-axis labels in their own row below the chart
- Responsive charts must not visually stretch stroke widths
- Use a clear disclosure such as `View daily values` for accessible tabular equivalents

---

## Accessibility

Target WCAG AA.

At minimum:

- Body text: `4.5:1` contrast
- Large text and meaningful icons: `3:1`
- Control boundaries, graphical objects, and focus indicators: `3:1`
- All actions must be keyboard reachable and operable
- Icon-only buttons need an accessible name and visible tooltip
- Loading regions use `aria-busy`
- Success messages use polite live announcements
- Important errors use assertive announcements
- Focus moves logically after dialogs, Retry, navigation, and export actions
- Error messages never expose internal server details

At `1366 × 768`, the app must not require horizontal page scrolling.

At narrow widths, layout regions should reflow. Wide data tables may scroll inside their own container.

---

## Reporting API contract

The current project documentation proposes:

```text
GET /api/reports/monthly?month=YYYY-MM
GET /api/reports/hours-by-type?month=YYYY-MM
GET /api/reports/hours-by-subject?month=YYYY-MM
GET /api/reports/monthly-details?month=YYYY-MM
GET /api/reports/export/excel?month=YYYY-MM
```

These are proposed contracts, not permission to duplicate endpoints.

If the merged backend defines equivalent routes or combines reporting data into a shared DTO, use the agreed backend contract.

---

## Implementation conventions

- Keep one shared React/Vite application
- Use TypeScript and matching `.ts` / `.tsx` extensions
- Keep API access in `src/services`
- Keep reusable formatting logic in `src/utils`
- Keep page components in `src/pages`
- Keep shared visual components in `src/components`
- Keep shared layouts in `src/layouts`
- Do not hard-code reporting totals into components
- Keep backend business rules in backend services
- Keep Swagger/OpenAPI and project documentation current
- Add tests for important business logic and integration behavior

---

## Out of scope

Unless the project requirements are updated, do not implement:

- Advanced approval workflows
- Persistent notification system
- Attachments
- Advanced analytics
- Scheduled reports
- PDF export
- CSV export
- Global search
- System logs
- Resource planning
- Personal statistics
- Profile
- Settings

---

## When something is unclear

Check the project requirements first, then the agreed API contract, then the approved Figma design.

If it is still unclear, discuss it with the team before introducing a new pattern or feature.

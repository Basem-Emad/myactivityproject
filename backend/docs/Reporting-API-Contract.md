# Reporting API Contract

This document defines the backend API contract for the Dashboard and Monthly Report features. It covers monthly summaries, grouped reporting data, daily trends, activity details, and Excel export.

Base path: `/api/reports`

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/reports/monthly?month=YYYY-MM` | Monthly summary |
| GET | `/api/reports/hours-by-type?month=YYYY-MM` | Duration grouped by Activity Type |
| GET | `/api/reports/hours-by-subject?month=YYYY-MM` | Duration grouped by Activity Subject |
| GET | `/api/reports/daily-trend?month=YYYY-MM` | Daily duration trend |
| GET | `/api/reports/monthly-details?month=YYYY-MM` | Full activity list for the month |
| GET | `/api/reports/export/excel?month=YYYY-MM` | Excel workbook download |

---

## Common Rules

### Month Parameter

- Required query parameter on all endpoints.
- Format: `YYYY-MM` (e.g. `2026-08`).
- Parsed as `java.time.YearMonth` in the backend.
- Invalid or blank value → HTTP 400 using the project's `ApiErrorResponse` convention.
- Future months are valid (may return empty data).

### Duration Representation

API responses represent durations exclusively as integer minutes (`long` in Java). Display formatting such as `8h 30m` is handled by the frontend.

### Data Consistency

For any populated month, the following invariants hold:

```
sum(hours-by-type[].totalDurationMinutes)   == monthly.totalDurationMinutes
sum(hours-by-subject[].totalDurationMinutes) == monthly.totalDurationMinutes
sum(daily-trend[].totalDurationMinutes)      == monthly.totalDurationMinutes
```

Dashboard and Monthly Report consume the same backend calculations.

### Empty Month Semantics

A valid month with no activities returns **normal responses** (not 404):

- `/monthly` → summary with all values `0`
- `/hours-by-type` → `[]`
- `/hours-by-subject` → `[]`
- `/daily-trend` → `[]`
- `/monthly-details` → object with empty `activities` array

---

## Endpoint Details

### GET /api/reports/monthly

**Response 200:**

```json
{
  "month": "2026-08",
  "totalDays": 20,
  "totalDurationMinutes": 9630,
  "averageDurationMinutes": 482
}
```

**Business rules:**

- `totalDays` = number of distinct activity dates in the month.
- `totalDurationMinutes` = sum of all activity durations (backend-calculated).
- `averageDurationMinutes` = `totalDurationMinutes / totalDays`, rounded to the nearest whole minute.
- If `totalDays` is 0, `averageDurationMinutes` is 0.
- The frontend consumes these values directly without recalculating.

---

### GET /api/reports/hours-by-type

**Response 200:**

```json
[
  {
    "activityType": "Project",
    "totalDurationMinutes": 3600
  }
]
```

- Activity Type IDs are not part of the current reporting contract.
- Sum across entries equals `monthly.totalDurationMinutes`.

---

### GET /api/reports/hours-by-subject

**Response 200:**

```json
[
  {
    "activitySubject": "BM Microfocus",
    "totalDurationMinutes": 3900
  }
]
```

- Activity Subject IDs are not part of the current reporting contract.
- Sum across entries equals `monthly.totalDurationMinutes`.

---

### GET /api/reports/daily-trend

**Response 200:**

```json
[
  {
    "date": "2026-08-01",
    "totalDurationMinutes": 480
  }
]
```

- Date format: ISO `YYYY-MM-DD` (Java DTO uses `LocalDate`).
- Only dates with activity data are included.
- Sorted ascending by date.
- Sum across entries equals `monthly.totalDurationMinutes`.
- Empty month returns `[]`.

---

### GET /api/reports/monthly-details

**Response 200:**

```json
{
  "month": "2026-08",
  "activities": [
    {
      "id": 1,
      "date": "2026-08-01",
      "startTime": "09:00",
      "endTime": "11:30",
      "activityType": "Project",
      "activitySubject": "BM Microfocus",
      "taskDescription": "Implemented reporting API",
      "durationMinutes": 150
    }
  ]
}
```

- `date`: ISO `YYYY-MM-DD` (Java DTO uses `LocalDate`).
- `startTime` / `endTime`: ISO time (Java DTO uses `LocalTime`).
- `durationMinutes`: `long`.
- Sorted by `date` ascending, then `startTime` ascending.

> **Note:** The exact Activity entity field names are not finalized. This contract defines the reporting response shape only and does not dictate the Activity entity structure.

---

### GET /api/reports/export/excel

**When data exists — Response 200:**

```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="August-2026-Activity-Report.xlsx"
```

Filename pattern: `Month-YYYY-Activity-Report.xlsx`

**Workbook sheets:**

1. Summary
2. Activities
3. By Activity Type
4. By Subject

Formatted duration values (e.g. `"8h 30m"`) are acceptable inside the workbook since it is a human-readable export.

**When no activity data exists — Response 204 No Content:**

- The frontend disables the export button when the month is empty.
- A direct request to this endpoint for an empty month returns 204.
- No empty workbook is generated.

---

## Error Handling

| Condition | Response |
|-----------|----------|
| Invalid or missing `month` parameter | HTTP 400 with `ApiErrorResponse` |

Uses the project's existing `ApiErrorResponse` convention. No new generic response wrapper is introduced.

---

## Dependency Boundaries

The Reporting module does **not** own or create:

- Activity entity or its database migrations
- User entity
- Authentication / security implementation

These are owned by their respective modules. The query/repository integration strategy will be decided once the Activity module implementation is available.

### Data Access Scope

Reporting results will eventually be scoped using the authenticated user and role provided by the User/Auth module.

- Employees view their own reporting data.
- Any manager/admin reporting scope will follow the final authorization rules implemented by the User/Auth module.
- Reporting scope is derived from the authenticated user context rather than a client-supplied user identifier.
- Authentication and authorization integration is deferred until the User/Auth module is available.

# Reporting API Contract

This document defines the backend API contract for the Dashboard and Monthly Report features.

**Project requirements reference:**
- `docs/Activity_Tracking_Project.pdf` — Sections 10, 11, and 21.4
- Functional Requirements: FR-10, FR-11, FR-12
- Business Rules: Total Days = count of unique dates; Total Hours = sum of all activity durations

---

## Overview

The Dashboard screen and the Monthly Report screen both consume these endpoints. There is one
unified set of reporting endpoints under `/api/reports`. The frontend uses the same data for both
screens — the Dashboard shows charts and summary cards, and the Monthly Report shows tables and
provides the Excel export.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/reports/monthly?month=YYYY-MM` | Monthly summary totals |
| GET | `/api/reports/hours-by-type?month=YYYY-MM` | Duration grouped by Activity Type |
| GET | `/api/reports/hours-by-subject?month=YYYY-MM` | Duration grouped by Activity Subject |
| GET | `/api/reports/daily-trend?month=YYYY-MM` | Per-day duration trend |
| GET | `/api/reports/monthly-details?month=YYYY-MM` | Full activity list for the month |
| GET | `/api/reports/export/excel?month=YYYY-MM` | Download Excel workbook |

---

## Common Rules

### Month Parameter

Every endpoint requires a `month` query parameter in `YYYY-MM` format (e.g. `2026-08`).

- Must be exactly 7 characters: four-digit year, a hyphen, and a two-digit month
- Parsed as `java.time.YearMonth` internally using `ReportingMonthParser`
- Invalid or missing value returns HTTP 400 with `ApiErrorResponse`
- Future months are valid and return empty data if no activities exist

### Date and Time Formats

**Dates** are serialized as `YYYY-MM-DD` (e.g. `"2026-08-01"`).

**Times** are serialized as `HH:mm:ss` (e.g. `"09:00:00"`, `"17:30:00"`). The frontend displays
only hours and minutes since seconds are always zero.

### Duration Format

All durations in JSON responses are represented as **integer minutes**.

For example, 2 hours and 30 minutes is `150` (not `"2:30"` or `"2h 30m"`).

The frontend is responsible for formatting durations for display (e.g. `"2h 30m"`). This approach
was chosen because:
- The `ActivityEntry` entity stores `durationMinutes` as an `Integer`
- Integer minutes are efficient for JSON transport and aggregation
- Chart libraries require numeric values
- Duration formatting is a UI concern

The Excel export uses human-readable formats like `"2h 30m"` since it is a document for reading,
not for data processing.

### User Scope

**Current behavior:** All endpoints return data across all users without filtering. The temporary
`userId = 1L` constant used in `ActivityEntryController` is reflected here — reporting queries
the full `activity_entries` table without a user filter.

**Future behavior:** Once authentication is integrated, reporting will automatically scope to the
logged-in user via the Spring Security context. Employees will see only their own data.
Admin and Manager access will follow the authorization rules defined in the User and Authentication
module.

The frontend must not send a `userId` parameter. User scoping is server-side only.

### Data Consistency

For any month with activity data, the following must hold:

```
sum(hours-by-type[].totalDurationMinutes)    == monthly.totalDurationMinutes
sum(hours-by-subject[].totalDurationMinutes) == monthly.totalDurationMinutes
sum(daily-trend[].totalDurationMinutes)      == monthly.totalDurationMinutes
```

The backend is the single source of truth for all totals. The frontend must not recalculate or
derive these values independently.

### Empty Month Behavior

A valid month with no activities returns normal successful responses, not 404:

| Endpoint | Empty response |
|----------|----------------|
| `/monthly` | `{ "month": "YYYY-MM", "totalDays": 0, "totalDurationMinutes": 0, "averageDurationMinutes": 0 }` |
| `/hours-by-type` | `[]` |
| `/hours-by-subject` | `[]` |
| `/daily-trend` | `[]` |
| `/monthly-details` | `{ "month": "YYYY-MM", "activities": [] }` |
| `/export/excel` | HTTP 204 No Content |

---

## Endpoint Details

### GET /api/reports/monthly

Returns summary totals for the selected month.

Used by: Dashboard summary cards, Monthly Report summary section.

**Example response:**

```json
{
  "month": "2026-08",
  "totalDays": 20,
  "totalDurationMinutes": 9630,
  "averageDurationMinutes": 481
}
```

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `month` | `String` | The requested month, echoed back in `YYYY-MM` format |
| `totalDays` | `int` | Number of distinct calendar dates that have at least one activity |
| `totalDurationMinutes` | `long` | Sum of all `durationMinutes` values for activities in the month |
| `averageDurationMinutes` | `long` | Average duration per day: `totalDurationMinutes / totalDays` using integer division (truncates fractional part); returns `0` when `totalDays` is `0` |

**Mapped from:** `MonthlySummaryResponse`

---

### GET /api/reports/hours-by-type

Returns durations grouped by Activity Type for the selected month.

Used by: Dashboard "Hours by Activity Type" chart, Monthly Report grouped table.

**Example response:**

```json
[
  { "activityType": "Project",  "totalDurationMinutes": 3600 },
  { "activityType": "Product",  "totalDurationMinutes": 2220 },
  { "activityType": "Dev Plan", "totalDurationMinutes": 960 },
  { "activityType": "Meeting",  "totalDurationMinutes": 1470 },
  { "activityType": "POC",      "totalDurationMinutes": 1200 },
  { "activityType": "Day Off",  "totalDurationMinutes": 180 }
]
```

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `activityType` | `String` | The name of the Activity Type as stored in `activity_types.name` |
| `totalDurationMinutes` | `long` | Sum of `durationMinutes` for all activities of this type in the month |

Only Activity Types that have at least one activity in the selected month are included.
The sum of all `totalDurationMinutes` values must equal `monthly.totalDurationMinutes`.

**Mapped from:** `List<HoursByTypeResponse>`

---

### GET /api/reports/hours-by-subject

Returns durations grouped by Activity Subject for the selected month.

Used by: Dashboard "Hours by Activity Subject" chart, Monthly Report grouped table.

**Example response:**

```json
[
  { "activitySubject": "BM Microfocus",  "totalDurationMinutes": 3900 },
  { "activitySubject": "License Module", "totalDurationMinutes": 2700 },
  { "activitySubject": "Microservices",  "totalDurationMinutes": 1920 },
  { "activitySubject": "Raya Handover",  "totalDurationMinutes": 930 },
  { "activitySubject": "Training",       "totalDurationMinutes": 180 }
]
```

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `activitySubject` | `String` | The name of the Activity Subject as stored in `activity_subjects.name` |
| `totalDurationMinutes` | `long` | Sum of `durationMinutes` for all activities of this subject in the month |

Only Activity Subjects that have at least one activity in the selected month are included.
The sum of all `totalDurationMinutes` values must equal `monthly.totalDurationMinutes`.

**Mapped from:** `List<HoursBySubjectResponse>`

---

### GET /api/reports/daily-trend

Returns per-day totals for the selected month, used for the daily trend line chart.

Used by: Dashboard "Daily Trend" chart.

**Example response:**

```json
[
  { "date": "2026-08-01", "totalDurationMinutes": 480 },
  { "date": "2026-08-04", "totalDurationMinutes": 510 },
  { "date": "2026-08-05", "totalDurationMinutes": 390 },
  { "date": "2026-08-06", "totalDurationMinutes": 480 }
]
```

**Fields:**

| Field | Java Type | JSON Type | Description |
|-------|-----------|-----------|-------------|
| `date` | `LocalDate` | `String` | Activity date serialized as `YYYY-MM-DD` |
| `totalDurationMinutes` | `long` | `Number` | Sum of all `durationMinutes` values for activities on this date |

Only dates that have at least one activity are included. Results are sorted by date ascending.
The sum of all `totalDurationMinutes` values must equal `monthly.totalDurationMinutes`.

**Mapped from:** `List<DailyTrendResponse>`

---

### GET /api/reports/monthly-details

Returns the full list of activities for the selected month, sorted by date then start time.

Used by: Monthly Report activity details table, Excel export source data.

**Example response:**

```json
{
  "month": "2026-08",
  "activities": [
    {
      "id": 1,
      "date": "2026-08-01",
      "startTime": "09:00:00",
      "endTime": "11:30:00",
      "activityType": "Project",
      "activitySubject": "BM Microfocus",
      "taskDescription": "Implemented the reporting service layer",
      "durationMinutes": 150
    },
    {
      "id": 2,
      "date": "2026-08-01",
      "startTime": "12:00:00",
      "endTime": "14:00:00",
      "activityType": "Meeting",
      "activitySubject": "Raya Handover",
      "taskDescription": "Sprint planning and backlog grooming",
      "durationMinutes": 120
    }
  ]
}
```

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `month` | `String` | The requested month in `YYYY-MM` format |
| `activities` | `Array` | Ordered list of activity entries; empty array if no activities |

**Activity entry fields:**

| Field | Java Type | JSON Type | Description |
|-------|-----------|-----------|-------------|
| `id` | `Long` | `Number` | Activity entry ID from the `activity_entries` table |
| `date` | `LocalDate` | `String` | Activity date serialized as `YYYY-MM-DD` |
| `startTime` | `LocalTime` | `String` | Start time serialized as `HH:mm:ss` |
| `endTime` | `LocalTime` | `String` | End time serialized as `HH:mm:ss` |
| `activityType` | `String` | `String` | Activity Type name from `activity_types.name` |
| `activitySubject` | `String` | `String` | Activity Subject name from `activity_subjects.name` |
| `taskDescription` | `String` | `String` | Free-text task description |
| `durationMinutes` | `long` | `Number` | Duration in integer minutes, calculated by the backend |

Results are sorted by `date` ascending, then `startTime` ascending. This ordering is enforced by the repository query and must be preserved in the response.

**Mapped from:** `MonthlyDetailsResponse` containing `List<ActivityDetailResponse>`

---

### GET /api/reports/export/excel

Downloads an Excel workbook (`.xlsx`) containing the full monthly report.

Used by: Monthly Report "Export to Excel" button.

**Response when data exists — HTTP 200:**

```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="August-2026-Activity-Report.xlsx"
```

**Filename format:** `{MonthName}-{YYYY}-Activity-Report.xlsx`

Examples:
- `August-2026-Activity-Report.xlsx`
- `January-2027-Activity-Report.xlsx`

**Workbook sheet structure:**

| Sheet | Contents |
|-------|----------|
| `Summary` | Month, Total Days, Total Duration, Average Duration Per Day |
| `Activities` | Full activity list: Date, Start Time, End Time, Activity Type, Activity Subject, Task Description, Duration |
| `By Activity Type` | Activity Type name and total duration |
| `By Subject` | Activity Subject name and total duration |

Durations in the workbook are formatted as `"Xh Ym"` (e.g. `"8h 30m"`) for readability.

**Response when no data exists — HTTP 204 No Content:**

If the selected month has no activities, the endpoint returns `204 No Content` with no body and
no file. The frontend must disable the Export button whenever `totalDays` is `0`.

**Generated by:** `ExcelReportGenerator` using Apache POI (`poi-ooxml`)

---

## Error Handling

All errors use the shared `ApiErrorResponse` format:

```json
{
  "timestamp": "2026-08-20T23:15:30.123456",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid month format. Expected YYYY-MM (e.g. 2026-08).",
  "path": "/api/reports/monthly"
}
```

| Scenario | HTTP Status |
|----------|-------------|
| Missing or invalid `month` parameter | 400 Bad Request |
| Unexpected server error | 500 Internal Server Error |

The `details` array is included in validation error responses when multiple field errors are
present (consistent with the global error handling pattern used by the Activity Entry module).

---

## Implementation Notes

### Response Classes

| Response class | Used by |
|----------------|---------|
| `MonthlySummaryResponse` | `/monthly` |
| `HoursByTypeResponse` | `/hours-by-type` |
| `HoursBySubjectResponse` | `/hours-by-subject` |
| `DailyTrendResponse` | `/daily-trend` |
| `MonthlyDetailsResponse` + `ActivityDetailResponse` | `/monthly-details` |
| `ExcelReportGenerator` | `/export/excel` |

All response classes are in `com.raya.activitytracking.reporting.dto.response`.

### Month Parsing

All endpoint methods receive `month` as a plain `String` query parameter and call
`ReportingMonthParser.parse(month)` immediately. This throws `IllegalArgumentException` on
invalid input, which `GlobalExceptionHandler` converts to HTTP 400.

### Data Source

All reporting data comes from the `activity_entries` table joined to `activity_types` and
`activity_subjects`. The fields used are:

| Column | Used for |
|--------|----------|
| `date` | Monthly range filtering, day counting, trend grouping |
| `start_time` | Activity detail output |
| `end_time` | Activity detail output |
| `duration_minutes` | All duration aggregations |
| `activity_type_id` → `activity_types.name` | Type grouping and output |
| `activity_subject_id` → `activity_subjects.name` | Subject grouping and output |
| `task_description` | Activity detail output |
| `id` | Activity detail output |
| `user_id` | Reserved for future user scoping after auth module is merged |

### Repository Queries

These three queries are required on `ActivityEntryRepository`, which is defined in Daniel's
`feature/activity-entry` branch and will be available after merge:

**1. Full activity list for a date range (already defined by Daniel):**
```java
List<ActivityEntry> findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
        Long userId, LocalDate startDate, LocalDate endDate);
```
For the current pre-auth phase, pass the temporary constant `userId = 1L`.

**2. Duration grouped by Activity Type:**
```java
@Query("""
        SELECT e.activityType.name, SUM(e.durationMinutes)
        FROM ActivityEntry e
        WHERE e.date BETWEEN :start AND :end
        GROUP BY e.activityType.name
        ORDER BY e.activityType.name
        """)
List<Object[]> sumDurationByTypeBetween(
        @Param("start") LocalDate start,
        @Param("end") LocalDate end);
```

**3. Duration grouped by Activity Subject:**
```java
@Query("""
        SELECT e.activitySubject.name, SUM(e.durationMinutes)
        FROM ActivityEntry e
        WHERE e.date BETWEEN :start AND :end
        GROUP BY e.activitySubject.name
        ORDER BY e.activitySubject.name
        """)
List<Object[]> sumDurationBySubjectBetween(
        @Param("start") LocalDate start,
        @Param("end") LocalDate end);
```

Queries 2 and 3 are added to `ActivityEntryRepository` as part of the reporting module
implementation once the activity entry branch is merged into the branch.

### Service Logic Summary

`ReportServiceImpl` derives all values from the same date-range query result:

- **Total Days** — `stream().map(ActivityEntry::getDate).distinct().count()`
- **Total Duration** — `stream().mapToLong(ActivityEntry::getDurationMinutes).sum()`
- **Average Duration** — `totalDurationMinutes / totalDays`; returns `0` when `totalDays` is `0`
- **By Type** — uses `sumDurationByTypeBetween` query
- **By Subject** — uses `sumDurationBySubjectBetween` query
- **Daily Trend** — groups the full list by date in Java: `Collectors.groupingBy` → `summingLong`
- **Monthly Details** — maps the full list to `ActivityDetailResponse` objects

### CORS

The reporting controller is served under `/api/**`, which is already covered by the existing
`WebConfig` CORS configuration (`allowedOrigins("http://localhost:5173")`).

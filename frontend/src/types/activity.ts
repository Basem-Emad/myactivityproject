export interface ActivityEntry {
    id: number;
    userId: number;
    date: string;          // "2026-08-13" — ISO date string from the API
    startTime: string;     // "09:00:00"
    endTime: string;       // "12:30:00"
    durationMinutes: number; // 210
    durationFormatted: string;  // "3h 30m"
    activityTypeId: number;
    activityTypeName: string;
    activitySubjectId: number;
    activitySubjectName: string;
    taskDescription: string;
}

export interface ActivityEntryRequest {
    date: string;
    startTime: string;
    endTime: string;
    activityTypeId: number;
    activitySubjectId: number;
    taskDescription: string;
}

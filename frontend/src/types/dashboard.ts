export interface MonthlySummary {
    month: string;
    totalDays: number;
    totalDurationMinutes: number;
    averageDurationMinutes: number;
}

export interface HoursByType {
    activityType: string;
    totalDurationMinutes: number;
}

export interface HoursBySubject {
    activitySubject: string;
    totalDurationMinutes: number;
}

export interface DailyTrend {
    date: string;
    totalDurationMinutes: number;
}

export interface ActivityDetail {
    id: number;
    date: string;
    startTime: string;
    endTime: string;
    activityType: string;
    activitySubject: string;
    taskDescription: string;
    durationMinutes: number;
}

export interface MonthlyDetails {
    month: string;
    activities: ActivityDetail[];
}

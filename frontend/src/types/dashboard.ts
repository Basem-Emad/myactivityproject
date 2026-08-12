export interface HoursByType {
    activityType: string;
    hoursMinutes: string;
}

export interface HoursBySubject {
    activitySubject: string;
    hoursMinutes: string;
}

export interface DailyTrendPoint {
    day: number;
    hours: number;
}

export interface MonthlySummary {
    totalDays: number;
    totalHoursMinutes: string;
    byActivityType: HoursByType[];
    byActivitySubject: HoursBySubject[];
    dailyTrend: DailyTrendPoint[];
}

// Mock data matching the dashboard design.
// Replace with real API calls when backend integration is ready.

export type DashboardState = "loaded" | "loading" | "empty" | "error";

export interface HoursByType {
  activityType: string;
  totalMinutes: number;
  label: string; // formatted "60h 0m"
}

export interface HoursBySubject {
  activitySubject: string;
  totalMinutes: number;
  label: string;
}

export interface DailyTrendPoint {
  day: number;
  totalMinutes: number;
  label: string; // formatted "8h 0m"
}

export interface MonthlySummary {
  month: string;
  totalHours: number;
  totalMinutes: number;
  totalHoursLabel: string;   // "160h 30m"
  totalDays: number;
  avgHours: number;
  avgMinutes: number;
  avgHoursLabel: string;     // "8h 2m"
  byActivityType: HoursByType[];
  byActivitySubject: HoursBySubject[];
  dailyTrend: DailyTrendPoint[];
}

export const mockMonthlySummary: MonthlySummary = {
  month: "2026-08",
  totalHours: 160,
  totalMinutes: 30,
  totalHoursLabel: "160h 30m",
  totalDays: 20,
  avgHours: 8,
  avgMinutes: 2,
  avgHoursLabel: "8h 2m",

  byActivityType: [
    { activityType: "Project",  totalMinutes: 3600, label: "60h 0m" },
    { activityType: "Product",  totalMinutes: 1920, label: "32h 0m" },
    { activityType: "Meeting",  totalMinutes: 1470, label: "24h 30m" },
    { activityType: "POC",      totalMinutes: 1200, label: "20h 0m" },
    { activityType: "Dev Plan", totalMinutes:  960, label: "16h 0m" },
    { activityType: "Day Off",  totalMinutes:  480, label: "8h 0m" },
  ],

  byActivitySubject: [
    { activitySubject: "BM Microfocus",  totalMinutes: 3900, label: "65h 0m" },
    { activitySubject: "License Module", totalMinutes: 2700, label: "45h 0m" },
    { activitySubject: "Microservices",  totalMinutes: 2100, label: "35h 0m" },
    { activitySubject: "Training",       totalMinutes:  930, label: "15h 30m" },
  ],

  // All 31 days of August 2026 (Mon-Fri = 8h, Sat-Sun = 0h, last working day = 8h 30m)
  // August 2026: Sat 1, Sun 2, Mon 3-Fri 7, Sat 8-Sun 9, Mon 10-Fri 14, Sat 15-Sun 16, Mon 17-Fri 21, Sat 22-Sun 23, Mon 24-Fri 28, Sat 29-Sun 30, Mon 31
  dailyTrend: [
    { day:  1, totalMinutes:   0, label: "0h 0m" },   // Sat
    { day:  2, totalMinutes:   0, label: "0h 0m" },   // Sun
    { day:  3, totalMinutes: 480, label: "8h 0m" },   // Mon
    { day:  4, totalMinutes: 480, label: "8h 0m" },
    { day:  5, totalMinutes: 480, label: "8h 0m" },
    { day:  6, totalMinutes: 480, label: "8h 0m" },
    { day:  7, totalMinutes: 480, label: "8h 0m" },   // Fri
    { day:  8, totalMinutes:   0, label: "0h 0m" },   // Sat
    { day:  9, totalMinutes:   0, label: "0h 0m" },   // Sun
    { day: 10, totalMinutes: 480, label: "8h 0m" },   // Mon
    { day: 11, totalMinutes: 480, label: "8h 0m" },
    { day: 12, totalMinutes: 480, label: "8h 0m" },
    { day: 13, totalMinutes: 480, label: "8h 0m" },
    { day: 14, totalMinutes: 480, label: "8h 0m" },   // Fri
    { day: 15, totalMinutes:   0, label: "0h 0m" },   // Sat
    { day: 16, totalMinutes:   0, label: "0h 0m" },   // Sun
    { day: 17, totalMinutes: 480, label: "8h 0m" },   // Mon
    { day: 18, totalMinutes: 480, label: "8h 0m" },
    { day: 19, totalMinutes: 480, label: "8h 0m" },
    { day: 20, totalMinutes: 480, label: "8h 0m" },
    { day: 21, totalMinutes: 480, label: "8h 0m" },   // Fri
    { day: 22, totalMinutes:   0, label: "0h 0m" },   // Sat
    { day: 23, totalMinutes:   0, label: "0h 0m" },   // Sun
    { day: 24, totalMinutes: 480, label: "8h 0m" },   // Mon
    { day: 25, totalMinutes: 480, label: "8h 0m" },
    { day: 26, totalMinutes: 480, label: "8h 0m" },
    { day: 27, totalMinutes: 480, label: "8h 0m" },
    { day: 28, totalMinutes: 510, label: "8h 30m" },  // Fri - last day extra 30m
    { day: 29, totalMinutes:   0, label: "0h 0m" },   // Sat
    { day: 30, totalMinutes:   0, label: "0h 0m" },   // Sun
    { day: 31, totalMinutes:   0, label: "0h 0m" },   // Mon (but not working in this dataset)
  ],
};

// Mock data for Monthly Report page
// Replace with real API calls when backend integration is ready

export interface ActivityDetail {
  id: number;
  date: string; // "2026-08-03"
  activityType: string;
  activitySubject: string;
  startTime: string; // "09:00"
  endTime: string; // "17:00"
  durationMinutes: number;
  description: string;
}

export interface HoursByType {
  activityType: string;
  totalMinutes: number;
  label: string;
}

export interface HoursBySubject {
  activitySubject: string;
  totalMinutes: number;
  label: string;
}

export interface MonthlyReportData {
  month: string; // "2026-08"
  totalDays: number;
  totalMinutes: number;
  totalHoursLabel: string;
  avgMinutes: number;
  avgHoursLabel: string;
  hoursByType: HoursByType[];
  hoursBySubject: HoursBySubject[];
  activities: ActivityDetail[];
}

export const mockMonthlyReportData: MonthlyReportData = {
  month: "2026-08",
  totalDays: 20,
  totalMinutes: 9630, // 160h 30m
  totalHoursLabel: "160h 30m",
  avgMinutes: 482, // 8h 2m
  avgHoursLabel: "8h 2m",

  hoursByType: [
    { activityType: "Project", totalMinutes: 3600, label: "60h 0m" },
    { activityType: "Product", totalMinutes: 1920, label: "32h 0m" },
    { activityType: "POC", totalMinutes: 1200, label: "20h 0m" },
    { activityType: "Dev Plan", totalMinutes: 960, label: "16h 0m" },
    { activityType: "Meeting", totalMinutes: 1470, label: "24h 30m" },
    { activityType: "Day Off", totalMinutes: 480, label: "8h 0m" },
  ],

  hoursBySubject: [
    { activitySubject: "BM Microfocus", totalMinutes: 3900, label: "65h 0m" },
    { activitySubject: "License Module", totalMinutes: 2700, label: "45h 0m" },
    { activitySubject: "Microservices", totalMinutes: 2100, label: "35h 0m" },
    { activitySubject: "Training", totalMinutes: 930, label: "15h 30m" },
  ],

  activities: [
    {
      id: 1,
      date: "2026-08-03",
      activityType: "Project",
      activitySubject: "BM Microfocus",
      startTime: "09:00",
      endTime: "12:00",
      durationMinutes: 180,
      description: "Initial setup",
    },
    {
      id: 2,
      date: "2026-08-03",
      activityType: "Product",
      activitySubject: "License Module",
      startTime: "13:00",
      endTime: "18:00",
      durationMinutes: 300,
      description: "Feature development",
    },
    {
      id: 3,
      date: "2026-08-04",
      activityType: "Project",
      activitySubject: "BM Microfocus",
      startTime: "09:00",
      endTime: "17:00",
      durationMinutes: 480,
      description: "API Integration",
    },
    {
      id: 4,
      date: "2026-08-05",
      activityType: "Meeting",
      activitySubject: "Microservices",
      startTime: "09:00",
      endTime: "12:00",
      durationMinutes: 180,
      description: "Sprint Planning",
    },
    {
      id: 5,
      date: "2026-08-05",
      activityType: "POC",
      activitySubject: "Microservices",
      startTime: "13:00",
      endTime: "18:00",
      durationMinutes: 300,
      description: "Auth prototype",
    },
    {
      id: 6,
      date: "2026-08-06",
      activityType: "Dev Plan",
      activitySubject: "Training",
      startTime: "09:00",
      endTime: "17:00",
      durationMinutes: 480,
      description: "Security workshop",
    },
  ],
};

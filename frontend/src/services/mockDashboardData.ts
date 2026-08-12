import type { MonthlySummary } from "../types/dashboard";

// TODO: replace with real API once Mostafa's backend is ready

export const mockMonthlySummary: MonthlySummary = {
    totalDays: 20,
    totalHoursMinutes: "80:30",
    byActivityType: [
        { activityType: "Project", hoursMinutes: "25:00" },
        { activityType: "Product", hoursMinutes: "37:00" },
        { activityType: "POC", hoursMinutes: "0:00" },
        { activityType: "Pre-Project", hoursMinutes: "0:00" },
        { activityType: "Meetings", hoursMinutes: "0:00" },
        { activityType: "Day Off", hoursMinutes: "0:00" },
        { activityType: "Dev Plan", hoursMinutes: "16:00" },
    ],
    byActivitySubject: [
        { activitySubject: "BM Microfocus", hoursMinutes: "26:00" },
        { activitySubject: "License Module", hoursMinutes: "37:00" },
        { activitySubject: "Microservices", hoursMinutes: "16:00" },
        { activitySubject: "Raya Handover", hoursMinutes: "1:30" },
        { activitySubject: "Other", hoursMinutes: "0:00" },
    ],
    dailyTrend: [
        { day: 1, hours: 8 }, { day: 5, hours: 6 }, { day: 10, hours: 9 },
        { day: 15, hours: 5 }, { day: 20, hours: 7 }, { day: 25, hours: 8 },
        { day: 30, hours: 4 },
    ],
};

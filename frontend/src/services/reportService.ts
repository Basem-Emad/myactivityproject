import api from "./api";
import type {
    MonthlySummary,
    HoursByType,
    HoursBySubject,
    DailyTrend,
    MonthlyDetails,
} from "../types/dashboard";

export async function getMonthlySummary(month: string): Promise<MonthlySummary> {
    const response = await api.get<MonthlySummary>("/reports/monthly", {
        params: { month },
    });
    return response.data;
}

export async function getHoursByType(month: string): Promise<HoursByType[]> {
    const response = await api.get<HoursByType[]>("/reports/hours-by-type", {
        params: { month },
    });
    return response.data;
}

export async function getHoursBySubject(month: string): Promise<HoursBySubject[]> {
    const response = await api.get<HoursBySubject[]>("/reports/hours-by-subject", {
        params: { month },
    });
    return response.data;
}

export async function getDailyTrend(month: string): Promise<DailyTrend[]> {
    const response = await api.get<DailyTrend[]>("/reports/daily-trend", {
        params: { month },
    });
    return response.data;
}

export async function getMonthlyDetails(month: string): Promise<MonthlyDetails> {
    const response = await api.get<MonthlyDetails>("/reports/monthly-details", {
        params: { month },
    });
    return response.data;
}

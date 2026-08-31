import api from "./api";
import type { ActivityEntry, ActivityEntryRequest } from "../types/activity";

export async function getActivities(): Promise<ActivityEntry[]> {
    const response = await api.get<ActivityEntry[]>("/activities");
    return response.data;
}

export async function getActivityById(id: number): Promise<ActivityEntry> {
    const response = await api.get<ActivityEntry>(`/activities/${id}`);
    return response.data;
}

export async function getActivitiesByDateRange(
    startDate: string,
    endDate: string
): Promise<ActivityEntry[]> {
    const response = await api.get<ActivityEntry[]>("/activities/filter", {
        params: { startDate, endDate },
    });
    return response.data;
}

export async function createActivity(
    data: ActivityEntryRequest
): Promise<ActivityEntry> {
    const response = await api.post<ActivityEntry>("/activities", data);
    return response.data;
}

export async function updateActivity(
    id: number,
    data: ActivityEntryRequest
): Promise<ActivityEntry> {
    const response = await api.put<ActivityEntry>(`/activities/${id}`, data);
    return response.data;
}

export async function deleteActivity(id: number): Promise<void> {
    await api.delete(`/activities/${id}`);
}

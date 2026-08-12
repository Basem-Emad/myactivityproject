import api from "./api";
import type { ActivityType, ActivityTypeRequest } from "../types/masterData";

export async function getActivityTypes(): Promise<ActivityType[]> {
    const response = await api.get<ActivityType[]>("/activity-types");
    return response.data;
}

export async function createActivityType(
    data: ActivityTypeRequest
): Promise<ActivityType> {
    const response = await api.post<ActivityType>("/activity-types", data);
    return response.data;
}

export async function updateActivityType(
    id: number,
    data: ActivityTypeRequest
): Promise<ActivityType> {
    const response = await api.put<ActivityType>(`/activity-types/${id}`, data);
    return response.data;
}

export async function deactivateActivityType(id: number): Promise<void> {
    await api.delete(`/activity-types/${id}`);
}

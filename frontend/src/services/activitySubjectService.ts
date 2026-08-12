import api from "./api";
import type { ActivitySubject, ActivitySubjectRequest } from "../types/masterData";

export async function getActivitySubjects(): Promise<ActivitySubject[]> {
    const response = await api.get<ActivitySubject[]>("/activity-subjects");
    return response.data;
}

export async function createActivitySubject(
    data: ActivitySubjectRequest
): Promise<ActivitySubject> {
    const response = await api.post<ActivitySubject>("/activity-subjects", data);
    return response.data;
}

export async function updateActivitySubject(
    id: number,
    data: ActivitySubjectRequest
): Promise<ActivitySubject> {
    const response = await api.put<ActivitySubject>(`/activity-subjects/${id}`, data);
    return response.data;
}

export async function deactivateActivitySubject(id: number): Promise<void> {
    await api.delete(`/activity-subjects/${id}`);
}

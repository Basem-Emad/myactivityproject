export interface ActivityType {
    id: number;
    name: string;
    description: string | null;
    active: boolean;
}

export interface ActivityTypeRequest {
    name: string;
    description?: string;
}

export type SubjectType =
    | "PROJECT"
    | "PRODUCT"
    | "POC"
    | "COURSE"
    | "MEETING"
    | "OTHER";

export interface ActivitySubject {
    id: number;
    name: string;
    subjectType: SubjectType;
    description: string | null;
    active: boolean;
}

export interface ActivitySubjectRequest {
    name: string;
    subjectType: SubjectType;
    description?: string;
}

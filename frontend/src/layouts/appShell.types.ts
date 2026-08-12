export type UserRole = "EMPLOYEE" | "MANAGER" | "ADMIN";

export interface CurrentUser {
  name: string;
  role: UserRole;
}
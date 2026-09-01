import api from "./api";

export interface LoginPayload {
  username: string;
  password: string;
}

export interface UserRoleInfo {
  id: number;
  name: string;
}

export interface AuthUserInfo {
  id: number;
  userName: string;
  email: string;
  role?: UserRoleInfo;
}

/**
 * Sends login credentials to the backend and returns the raw JWT token string.
 */
export async function loginUser(payload: LoginPayload): Promise<string> {
  const response = await api.post<string>("/v1/auth/login", payload);
  return response.data;
}

/**
 * Fetches the currently authenticated user's profile.
 */
export async function getCurrentUser(): Promise<AuthUserInfo> {
  const response = await api.get<AuthUserInfo>("/v1/auth/me");
  return response.data;
}

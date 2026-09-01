import { createContext } from "react";
import type { AuthUserInfo, LoginPayload } from "../services/authService";
import type { CurrentUser } from "../layouts/appShell.types";

export interface AuthContextType {
  token: string | null;
  user: AuthUserInfo | null;
  currentUser: CurrentUser | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (credentials: LoginPayload) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

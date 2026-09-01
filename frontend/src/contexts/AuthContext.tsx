import React, { useEffect, useState } from "react";
import { getCurrentUser, loginUser, type AuthUserInfo, type LoginPayload } from "../services/authService";
import type { CurrentUser, UserRole } from "../layouts/appShell.types";
import { AuthContext } from "./authContextDef";

function normalizeRole(roleName?: string): UserRole {
  if (!roleName) return "EMPLOYEE";
  const upper = roleName.toUpperCase();
  if (upper.includes("ADMIN")) return "ADMIN";
  if (upper.includes("MANAGER")) return "MANAGER";
  return "EMPLOYEE";
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("token"));
  const [user, setUser] = useState<AuthUserInfo | null>(() => {
    const cached = localStorage.getItem("user");
    return cached ? JSON.parse(cached) : null;
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadUser() {
      const storedToken = localStorage.getItem("token");
      if (storedToken) {
        try {
          const profile = await getCurrentUser();
          setUser(profile);
          localStorage.setItem("user", JSON.stringify(profile));
        } catch {
          // Token expired or invalid
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          setToken(null);
          setUser(null);
        }
      }
      setLoading(false);
    }
    loadUser();
  }, [token]);

  async function login(credentials: LoginPayload) {
    const rawToken = await loginUser(credentials);
    localStorage.setItem("token", rawToken);
    setToken(rawToken);

    try {
      const profile = await getCurrentUser();
      setUser(profile);
      localStorage.setItem("user", JSON.stringify(profile));
    } catch {
      // Fallback if profile fetch fails
      const fallbackUser: AuthUserInfo = {
        id: 1,
        userName: credentials.username,
        email: "",
      };
      setUser(fallbackUser);
      localStorage.setItem("user", JSON.stringify(fallbackUser));
    }
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  }

  const currentUser: CurrentUser | null = user
    ? {
        name: user.userName,
        role: normalizeRole(user.role?.name),
      }
    : null;

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        currentUser,
        isAuthenticated: !!token,
        loading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

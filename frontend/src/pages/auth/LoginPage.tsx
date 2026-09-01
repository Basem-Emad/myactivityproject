import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router";
import { useAuth } from "../../hooks/useAuth";
import Button from "../../components/ui/Button";
import ErrorAlert from "../../components/ui/ErrorAlert";
import { getApiErrorMessage } from "../../utils/apiError";
import "./LoginPage.css";

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated } = useAuth();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Redirect if already authenticated
  React.useEffect(() => {
    if (isAuthenticated) {
      navigate("/dashboard", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!username.trim() || !password.trim()) {
      setError("Please enter both username and password.");
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await login({ username: username.trim(), password });
      
      const destination = (location.state as { from?: { pathname: string } })?.from?.pathname || "/dashboard";
      navigate(destination, { replace: true });
    } catch (err: unknown) {
      setError(getApiErrorMessage(err) || "Invalid username or password. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-header">
          <div className="login-header__icon">
            <span className="material-symbols-outlined">schedule</span>
          </div>
          <h1>Activity Tracker</h1>
          <p>Sign in to your account to continue</p>
        </div>

        {error && <ErrorAlert message={error} />}

        <form className="login-form" onSubmit={handleSubmit}>
          <div className="login-form__field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
              autoFocus
              required
            />
          </div>

          <div className="login-form__field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              required
            />
          </div>

          <div className="login-form__submit">
            <Button
              type="submit"
              variant="primary"
              disabled={loading}
              style={{ width: "100%" }}
            >
              {loading ? "Signing in..." : "Sign In"}
            </Button>
          </div>
        </form>

        <div className="login-footer">
          <p>Raya Internship Activity Tracking System &bull; 2026</p>
        </div>
      </div>
    </div>
  );
}

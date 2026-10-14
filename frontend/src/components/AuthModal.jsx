import { useState } from "react";
import { authAPI } from "../api/api";

function AuthModal({ onClose, onLogin }) {
  const [isLogin, setIsLogin] = useState(true);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setLoading(true);

    try {
      if (isLogin) {
        const response = await authAPI.login(email, password);
        const data = response?.data ?? response ?? {};
        const user = {
          name: data.name || email.split('@')[0],
          email: data.email || email,
          role: data.role || 'USER',
        };

        localStorage.setItem("vibesync_token", data.token || "");
        localStorage.setItem("vibesync_user", JSON.stringify(user));

        onLogin(user);
        onClose();
      } else {
        const response = await authAPI.register(name, email, password);
        const data = response?.data ?? response ?? {};

        if (data.token) {
          const user = {
            name: data.name || name,
            email: data.email || email,
            role: data.role || 'USER',
          };
          localStorage.setItem("vibesync_token", data.token || "");
          localStorage.setItem("vibesync_user", JSON.stringify(user));
          onLogin(user);
          onClose();
          return;
        }

        setMessage("Account created successfully. Please login.");
        setIsLogin(true);
        setPassword("");
      }
    } catch (error) {
      console.error(error);
      setMessage(
        error.response?.data?.message ||
          (error.code === "ERR_NETWORK"
            ? "Cannot reach the VibeSync backend. Start the backend and check its API URL and database connection."
            : "Something went wrong. Please try again.")
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-overlay">
      <div className="auth-modal">
        <button className="auth-close" onClick={onClose}>
          ×
        </button>

        <h2>{isLogin ? "Welcome back" : "Create your account"}</h2>

        <p className="auth-subtitle">
          {isLogin
            ? "Login to continue your VibeSync experience."
            : "Join VibeSync and make your music personal."}
        </p>

        <form onSubmit={handleSubmit}>
          {!isLogin && (
            <input
              type="text"
              placeholder="Your name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          )}

          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {message && <p className="auth-message">{message}</p>}

          <button type="submit" disabled={loading}>
            {loading
              ? "Please wait..."
              : isLogin
              ? "Login"
              : "Create Account"}
          </button>
        </form>

        <p className="auth-switch">
          {isLogin ? "Don't have an account?" : "Already have an account?"}

          <button
            type="button"
            onClick={() => {
              setIsLogin(!isLogin);
              setMessage("");
            }}
          >
            {isLogin ? "Sign Up" : "Login"}
          </button>
        </p>
      </div>
    </div>
  );
}

export default AuthModal;
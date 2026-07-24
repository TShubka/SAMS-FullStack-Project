import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import authService from '../services/authService';
import { ROLES, STORAGE_KEYS } from '../utils/constants';

/**
 * Authentication state for the whole app. Owner: Member 1.
 *
 * The Context API is used here instead of prop drilling: the navigation bar, the
 * route guards and half the pages all need to know who is logged in, and they sit
 * at very different depths of the tree.
 *
 * Everything this exposes is convenience for the UI. It is NOT security - the
 * backend re-checks the token and the role on every single request. Hiding a menu
 * item stops an honest user from clicking it; it does not stop anyone determined.
 */
export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore the session on a page refresh, otherwise every reload would log out.
  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEYS.USER);
    if (stored) {
      try {
        setUser(JSON.parse(stored));
      } catch {
        localStorage.removeItem(STORAGE_KEYS.USER);
        localStorage.removeItem(STORAGE_KEYS.TOKEN);
      }
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (credentials) => {
    const data = await authService.login(credentials);

    const session = {
      id: data.userId,
      username: data.username,
      email: data.email,
      roles: data.roles || [],
    };

    localStorage.setItem(STORAGE_KEYS.TOKEN, data.token);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(session));
    setUser(session);

    return session;
  }, []);

  const register = useCallback((payload) => authService.register(payload), []);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    setUser(null);
  }, []);

  const hasRole = useCallback(
    (...roles) => roles.some((role) => user?.roles?.includes(role)),
    [user],
  );

  const value = useMemo(
    () => ({
      user,
      loading,
      isAuthenticated: Boolean(user),
      isAdmin: user?.roles?.includes(ROLES.ADMIN) ?? false,
      isTeacher: user?.roles?.includes(ROLES.TEACHER) ?? false,
      isStudent: user?.roles?.includes(ROLES.STUDENT) ?? false,
      hasRole,
      login,
      register,
      logout,
    }),
    [user, loading, hasRole, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

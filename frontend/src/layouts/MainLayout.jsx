import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import useAuth from '../hooks/useAuth';
import { ROLES } from '../utils/constants';

/**
 * Shell for every authenticated page: sidebar, top bar, content outlet.
 * Owner: Member 1.
 *
 * Role-based navigation is built here. Each link declares which roles may see it,
 * and the list is filtered against the roles held in AuthContext - so an admin, a
 * teacher and a student each get a different menu from the same component.
 */
export default function MainLayout() {
  const { user, isAdmin, isTeacher, isStudent, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const links = [
    { to: '/dashboard', label: 'Dashboard', show: true },

    { to: '/departments', label: 'Departments', show: isAdmin },
    { to: '/students', label: 'Students', show: isAdmin || isTeacher },
    { to: '/teachers', label: 'Teachers', show: isAdmin },
    { to: '/courses', label: 'Courses', show: isAdmin || isTeacher },
    { to: '/enrollments', label: 'Enrollments', show: isAdmin },

    // Teacher attendance/marks entry, admin reports and user management are built
    // in the next phases; their links are added here as those pages land, so the
    // nav never points at a route that would 404.
    { to: '/reports', label: 'Reports', show: isAdmin || isTeacher },

    { to: '/my-attendance', label: 'My Attendance', show: isStudent },
    { to: '/my-grades', label: 'My Grades', show: isStudent },
    { to: '/transcript', label: 'My Transcript', show: isStudent },

    { to: '/profile', label: 'Profile', show: true },
  ].filter((link) => link.show);

  const roleLabel = isAdmin
    ? 'Administrator'
    : isTeacher
      ? 'Teacher'
      : isStudent
        ? 'Student'
        : 'User';

  return (
    <div className="layout">
      <aside className={`sidebar ${menuOpen ? 'open' : ''}`}>
        <div className="brand">
          <strong>SAMS</strong>
          <small>Group 6</small>
        </div>

        <nav>
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
              onClick={() => setMenuOpen(false)}
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main">
        <header className="topbar">
          <button
            type="button"
            className="menu-toggle"
            onClick={() => setMenuOpen((open) => !open)}
            aria-label="Toggle navigation"
          >
            &#9776;
          </button>

          <div className="topbar-user">
            <span className="username">{user?.username}</span>
            <span className="role-badge">{roleLabel}</span>
            <button type="button" className="btn btn-secondary" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </header>

        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export { ROLES };

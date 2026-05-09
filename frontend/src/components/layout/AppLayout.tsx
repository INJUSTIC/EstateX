import { Outlet, Navigate } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { useAuthStore } from '../../store/auth';
import { Zap } from 'lucide-react';

export function AppLayout() {
  const { userId } = useAuthStore();
  if (!userId) return <Navigate to="/login" replace />;

  return (
    <div className="app-layout">
      {/* Topbar */}
      <header className="topbar">
        <span className="logo">
          <Zap size={18} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 4 }} />
          EstateX
        </span>
        <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Property Marketplace</span>
      </header>

      {/* Sidebar */}
      <Sidebar />

      {/* Page content */}
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}

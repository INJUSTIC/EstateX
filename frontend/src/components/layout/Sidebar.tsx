import { NavLink, useNavigate } from 'react-router-dom';
import { Home, Heart, MessageCircle, User, LogOut, PlusCircle, Building2 } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { chatApi } from '../../api/chat';
import { useAuthStore } from '../../store/auth';

const links = [
  { to: '/', icon: Home, label: 'Browse' },
  { to: '/my-listings', icon: Building2, label: 'My Listings' },
  { to: '/favourites', icon: Heart, label: 'Favourites' },
  { to: '/inbox', icon: MessageCircle, label: 'Inbox' },
  { to: '/profile', icon: User, label: 'Profile' },
];

export function Sidebar() {
  const { displayName, clearUser } = useAuthStore();
  const navigate = useNavigate();

  const { data: conversations = [] } = useQuery({
    queryKey: ['conversations'],
    queryFn: chatApi.getConversations,
    refetchInterval: 10_000,
  });
  const totalUnread = conversations.reduce((sum, c) => sum + (c.unreadCount ?? 0), 0);

  return (
    <aside className="sidebar">
      <div style={{ padding: '4px 12px 16px', borderBottom: '1px solid var(--border)', marginBottom: 8 }}>
        <p style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 2 }}>Signed in as</p>
        <p style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-primary)' }}>{displayName ?? 'User'}</p>
      </div>

      {links.map(({ to, icon: Icon, label }) => (
        <NavLink key={to} to={to} end={to === '/'}>
          {({ isActive }) => (
            <div className={`nav-item ${isActive ? 'active' : ''}`}>
              <Icon size={17} />
              {label}
              {label === 'Inbox' && totalUnread > 0 && (
                <span className="badge" style={{ marginLeft: 'auto' }}>{totalUnread}</span>
              )}
            </div>
          )}
        </NavLink>
      ))}

      <div style={{ marginTop: 'auto', paddingTop: 16, borderTop: '1px solid var(--border)' }}>
        <button className="btn btn-primary w-full" onClick={() => navigate('/listings/new')}>
          <PlusCircle size={16} /> New Listing
        </button>
        <button className="nav-item mt-2 w-full" onClick={() => { clearUser(); navigate('/login'); }}
          style={{ marginTop: 10 }}>
          <LogOut size={17} /> Sign out
        </button>
      </div>
    </aside>
  );
}

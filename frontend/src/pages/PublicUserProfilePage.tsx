import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { userApi } from '../api/users';
import { User, Phone, Mail, Building2, ChevronLeft } from 'lucide-react';

export function PublicUserProfilePage() {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();

  const { data: user, isLoading } = useQuery({
    queryKey: ['user', userId],
    queryFn: () => userApi.getPublicProfile(userId!),
    enabled: !!userId,
  });

  if (isLoading) return <div className="loading-center"><div className="spinner" /></div>;
  if (!user) return <div className="empty-state"><p>User not found.</p></div>;

  return (
    <div className="fade-in" style={{ maxWidth: 560, margin: '0 auto' }}>
      <button className="btn btn-ghost btn-sm mb-4" onClick={() => navigate(-1)}>
        <ChevronLeft size={15} /> Back
      </button>

      <div className="section-header">
        <div>
          <h1 className="section-title">User Profile</h1>
          <p className="section-subtitle">Public profile information</p>
        </div>
      </div>

      <div className="card" style={{ display: 'flex', alignItems: 'center', gap: 20, marginBottom: 20 }}>
        <div className="avatar" style={{ width: 64, height: 64, fontSize: 26 }}>
          {user.displayName[0].toUpperCase()}
        </div>
        <div>
          <p style={{ fontSize: 20, fontWeight: 700 }}>{user.displayName}</p>
          <p style={{ fontSize: 13, color: 'var(--text-secondary)', marginTop: 2 }}>{user.email}</p>
          <div className="flex items-center gap-2 mt-2">
            <span className="listing-card__badge" style={{
              background: user.active ? 'rgba(34,197,94,0.12)' : 'rgba(239,68,68,0.12)',
              color: user.active ? 'var(--success)' : 'var(--danger)',
            }}>
              {user.active ? 'Active' : 'Inactive'}
            </span>
            <span className="listing-card__badge">
              <Building2 size={10} /> {user.activeListingsCount} listings
            </span>
          </div>
        </div>
      </div>

      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <p style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          Contact Info
        </p>

        <div>
          <label className="input-label">
            <Mail size={11} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 4 }} />
            Email
          </label>
          <input className="input" value={user.email} disabled style={{ opacity: 0.8, cursor: 'not-allowed' }} />
        </div>

        {user.phone && (
          <div>
            <label className="input-label">
              <Phone size={11} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 4 }} />
              Phone
            </label>
            <input className="input" value={user.phone} disabled style={{ opacity: 0.8, cursor: 'not-allowed' }} />
          </div>
        )}

        <div>
          <label className="input-label">
            <User size={11} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 4 }} />
            Member since
          </label>
          <input className="input" value={new Date(user.createdAt).toLocaleDateString('en-GB')} disabled style={{ opacity: 0.8, cursor: 'not-allowed' }} />
        </div>
      </div>
    </div>
  );
}

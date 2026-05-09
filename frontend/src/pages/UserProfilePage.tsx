import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '../api/users';
import { useAuthStore } from '../store/auth';
import { User, Phone, Mail, Building2, CheckCircle } from 'lucide-react';

export function UserProfilePage() {
  const { userId, setUser } = useAuthStore();
  const qc = useQueryClient();
  const [editing, setEditing] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [phone, setPhone] = useState('');
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState('');

  const { data: user, isLoading } = useQuery({
    queryKey: ['me', userId],
    queryFn: userApi.getMe,
    enabled: !!userId,
  });

  useEffect(() => {
    if (user) {
      setDisplayName(user.displayName);
      setPhone(user.phone ?? '');
    }
  }, [user]);

  const update = useMutation({
    mutationFn: () => userApi.updateMe({ displayName, phone: phone || undefined }),
    onSuccess: (updated) => {
      setUser(updated.id, updated.displayName);
      qc.invalidateQueries({ queryKey: ['me', userId] });
      setEditing(false);
      setSaved(true);
      setError('');
      setTimeout(() => setSaved(false), 3000);
    },
    onError: () => setError('Failed to update profile. Please try again.'),
  });

  if (isLoading) return <div className="loading-center"><div className="spinner" /></div>;
  if (!user) return <div className="empty-state"><p>Could not load profile.</p></div>;

  return (
    <div className="fade-in" style={{ maxWidth: 560, margin: '0 auto' }}>
      <div className="section-header">
        <div>
          <h1 className="section-title">My Profile</h1>
          <p className="section-subtitle">Manage your account information</p>
        </div>
      </div>

      {/* Avatar + name */}
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

      {/* Edit form */}
      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <div className="flex items-center justify-between">
          <p style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Account Details
          </p>
          {!editing && (
            <button className="btn btn-ghost btn-sm" onClick={() => setEditing(true)}>
              Edit
            </button>
          )}
        </div>

        <div>
          <label className="input-label">
            <Mail size={11} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 4 }} />
            Email
          </label>
          <input className="input" value={user.email} disabled
            style={{ opacity: 0.6, cursor: 'not-allowed' }} />
        </div>

        <div>
          <label className="input-label">
            <User size={11} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 4 }} />
            Display Name
          </label>
          <input className="input" value={displayName} disabled={!editing}
            style={!editing ? { opacity: 0.8 } : undefined}
            onChange={(e) => setDisplayName(e.target.value)} />
        </div>

        <div>
          <label className="input-label">
            <Phone size={11} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 4 }} />
            Phone
          </label>
          <input className="input" value={phone} disabled={!editing}
            placeholder={editing ? '+48 000 000 000' : 'Not set'}
            style={!editing ? { opacity: 0.8 } : undefined}
            onChange={(e) => setPhone(e.target.value)} />
        </div>

        {error && (
          <p style={{ fontSize: 13, color: 'var(--danger)', background: 'rgba(239,68,68,0.1)',
            padding: '8px 12px', borderRadius: 8 }}>{error}</p>
        )}

        {saved && (
          <p className="flex items-center gap-2" style={{ fontSize: 13, color: 'var(--success)',
            background: 'rgba(34,197,94,0.1)', padding: '8px 12px', borderRadius: 8 }}>
            <CheckCircle size={14} /> Profile updated successfully.
          </p>
        )}

        {editing && (
          <div className="flex gap-3">
            <button className="btn btn-primary" disabled={update.isPending}
              onClick={() => update.mutate()}>
              {update.isPending ? 'Saving…' : 'Save Changes'}
            </button>
            <button className="btn btn-ghost" onClick={() => {
              setEditing(false);
              setDisplayName(user.displayName);
              setPhone(user.phone ?? '');
              setError('');
            }}>
              Cancel
            </button>
          </div>
        )}
      </div>

      {/* Stats */}
      <div className="card" style={{ marginTop: 20 }}>
        <p style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 12 }}>
          Statistics
        </p>
        <div className="flex gap-6">
          <div>
            <p style={{ fontSize: 28, fontWeight: 800, color: 'var(--accent-hover)' }}>
              {user.activeListingsCount}
            </p>
            <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Active Listings</p>
          </div>
          <div>
            <p style={{ fontSize: 28, fontWeight: 800, color: 'var(--text-primary)' }}>
              {new Date(user.createdAt).toLocaleDateString('en-GB', { month: 'short', year: 'numeric' })}
            </p>
            <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Member Since</p>
          </div>
        </div>
      </div>
    </div>
  );
}

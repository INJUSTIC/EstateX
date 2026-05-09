import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { userApi } from '../api/users';
import { useAuthStore } from '../store/auth';
import { Zap } from 'lucide-react';

export function LoginPage() {
  const navigate = useNavigate();
  const setUser = useAuthStore((s) => s.setUser);
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (mode === 'register') {
        const user = await userApi.register(email, displayName);
        setUser(user.id, user.displayName);
      } else {
        // Login: fetch existing user by email
        const user = await userApi.login(email);
        setUser(user.id, user.displayName);
      }
      navigate('/');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Something went wrong. Please try again.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'var(--bg-base)',
      padding: 24,
    }}>
      <div style={{ width: '100%', maxWidth: 400 }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: 36 }}>
          <div className="logo" style={{ fontSize: 28, display: 'inline-block' }}>
            <Zap size={22} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 6 }} />
            EstateX
          </div>
          <p style={{ color: 'var(--text-secondary)', marginTop: 8, fontSize: 14 }}>
            Modern property marketplace
          </p>
        </div>

        {/* Card */}
        <div className="card" style={{ padding: 32, borderRadius: 'var(--radius-xl)' }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, marginBottom: 24 }}>
            {mode === 'login' ? 'Sign in' : 'Create account'}
          </h2>

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div>
              <label className="input-label">Email</label>
              <input className="input" type="email" required placeholder="you@example.com"
                value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>

            {mode === 'register' && (
              <div>
                <label className="input-label">Display name</label>
                <input className="input" required placeholder="John Doe"
                  value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
              </div>
            )}

            {error && (
              <p style={{ fontSize: 13, color: 'var(--danger)', background: 'rgba(239,68,68,0.1)',
                padding: '8px 12px', borderRadius: 8, margin: 0 }}>{error}</p>
            )}

            <button className="btn btn-primary w-full" type="submit" disabled={loading}
              style={{ marginTop: 4 }}>
              {loading ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
            </button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 20, fontSize: 13, color: 'var(--text-secondary)' }}>
            {mode === 'login' ? "Don't have an account? " : 'Already have an account? '}
            <button onClick={() => setMode(mode === 'login' ? 'register' : 'login')}
              style={{ background: 'none', border: 'none', color: 'var(--accent-hover)',
                fontWeight: 600, cursor: 'pointer', fontSize: 13 }}>
              {mode === 'login' ? 'Register' : 'Sign in'}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}

import { describe, it, expect } from 'vitest';
import { useAuthStore } from './auth';

describe('useAuthStore', () => {
  it('should start with null user', () => {
    const state = useAuthStore.getState();
    expect(state.userId).toBeNull();
    expect(state.displayName).toBeNull();
  });

  it('should set user and persist to localStorage', () => {
    useAuthStore.getState().setUser('user-1', 'Alice');
    const state = useAuthStore.getState();
    expect(state.userId).toBe('user-1');
    expect(state.displayName).toBe('Alice');
    expect(localStorage.getItem('userId')).toBe('user-1');
  });

  it('should clear user and remove from localStorage', () => {
    useAuthStore.getState().setUser('user-1', 'Alice');
    useAuthStore.getState().clearUser();
    const state = useAuthStore.getState();
    expect(state.userId).toBeNull();
    expect(state.displayName).toBeNull();
    expect(localStorage.getItem('userId')).toBeNull();
  });

  it('should persist state under the "auth" localStorage key', () => {
    useAuthStore.getState().setUser('user-1', 'Alice');
    expect(localStorage.getItem('auth')).not.toBeNull();
  });
});

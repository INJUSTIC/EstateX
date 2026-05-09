import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  userId: string | null;
  displayName: string | null;
  setUser: (userId: string, displayName: string) => void;
  clearUser: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      userId: null,
      displayName: null,
      setUser: (userId, displayName) => {
        localStorage.setItem('userId', userId);
        set({ userId, displayName });
      },
      clearUser: () => {
        localStorage.removeItem('userId');
        set({ userId: null, displayName: null });
      },
    }),
    { name: 'auth' }
  )
);

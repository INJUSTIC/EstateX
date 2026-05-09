import { api } from '../lib/axios';
import type { User } from '../types';

export const userApi = {
  getMe: (): Promise<User> =>
    api.get('/api/users/me').then((r) => r.data),

  updateMe: (data: { displayName: string; phone?: string }): Promise<User> =>
    api.put('/api/users/me', data).then((r) => r.data),

  getPublicProfile: (userId: string): Promise<User> =>
    api.get(`/api/users/${userId}/profile`).then((r) => r.data),

  register: (email: string, displayName: string): Promise<User> =>
    api.post('/api/auth/register', { email, displayName }).then((r) => r.data),

  login: (email: string): Promise<User> =>
    api.post('/api/auth/login', { email }).then((r) => r.data),
};

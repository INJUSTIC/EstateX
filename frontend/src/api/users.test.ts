import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { server } from '../test/mocks/server';
import { userApi } from './users';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('userApi', () => {
  it('should register a new user', async () => {
    const user = await userApi.register('alice@example.com', 'Alice');
    expect(user.id).toBe('user-1');
    expect(user.email).toBe('alice@example.com');
    expect(user.displayName).toBe('Alice');
    expect(user.active).toBe(true);
  });

  it('should login an existing user', async () => {
    const user = await userApi.login('alice@example.com');
    expect(user.id).toBe('user-1');
    expect(user.email).toBe('alice@example.com');
  });

  it('should fetch current user profile', async () => {
    localStorage.setItem('userId', 'user-1');
    const user = await userApi.getMe();
    expect(user.displayName).toBe('Alice');
    expect(user.activeListingsCount).toBe(1);
  });

  it('should update user profile', async () => {
    localStorage.setItem('userId', 'user-1');
    const user = await userApi.updateMe({ displayName: 'Updated Alice', phone: '+48111222333' });
    expect(user.displayName).toBe('Updated Alice');
    expect(user.phone).toBe('+48111222333');
  });

  it('should fetch public profile', async () => {
    const user = await userApi.getPublicProfile('user-1');
    expect(user.id).toBe('user-1');
    expect(user.displayName).toBe('Alice');
  });
});

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { server } from '../test/mocks/server';
import { renderWithProviders } from '../test/test-utils';
import { UserProfilePage } from './UserProfilePage';
import { useAuthStore } from '../store/auth';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
  server.resetHandlers();
  useAuthStore.getState().clearUser();
});
afterAll(() => server.close());

describe('UserProfilePage', () => {
  it('should render profile heading', async () => {
    useAuthStore.getState().setUser('user-1', 'Alice');
    renderWithProviders(<UserProfilePage />);

    await waitFor(() => {
      expect(screen.getByText('My Profile')).toBeInTheDocument();
    });
  });

  it('should load and display user data', async () => {
    useAuthStore.getState().setUser('user-1', 'Alice');
    renderWithProviders(<UserProfilePage />);

    await waitFor(() => {
      const nameInput = screen.getByDisplayValue('Alice');
      expect(nameInput).toBeInTheDocument();
    });
  });
});

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { server } from '../test/mocks/server';
import { renderWithProviders } from '../test/test-utils';
import { FavouritesPage } from './FavouritesPage';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('FavouritesPage', () => {
  it('should render favourites heading', () => {
    localStorage.setItem('userId', 'user-1');
    renderWithProviders(<FavouritesPage />);
    expect(screen.getByText('My Favourites')).toBeInTheDocument();
  });

  it('should show saved properties count after loading', async () => {
    localStorage.setItem('userId', 'user-1');
    renderWithProviders(<FavouritesPage />);

    await waitFor(() => {
      expect(screen.getByText('1 saved properties')).toBeInTheDocument();
    });
  });

  it('should render favourite listing cards', async () => {
    localStorage.setItem('userId', 'user-1');
    renderWithProviders(<FavouritesPage />);

    await waitFor(() => {
      expect(screen.getByText('Sunny Apartment')).toBeInTheDocument();
    });
  });
});

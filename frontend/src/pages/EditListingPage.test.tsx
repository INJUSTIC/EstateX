import { describe, it, expect, beforeAll, afterAll, afterEach, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from '../test/mocks/server';
import { mockListingBackend } from '../test/mocks/handlers';
import { renderWithProviders } from '../test/test-utils';
import { EditListingPage } from './EditListingPage';
import { useAuthStore } from '../store/auth';

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>();
  return { ...actual, useParams: () => ({ id: 'listing-1' }) };
});

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
  server.resetHandlers();
  useAuthStore.getState().clearUser();
});
afterAll(() => server.close());

describe('EditListingPage', () => {
  it('should render edit listing form with existing data', async () => {
    // given
    useAuthStore.getState().setUser('owner-1', 'Bob');

    // when
    renderWithProviders(<EditListingPage />);

    // then
    await waitFor(() => {
      expect(screen.getByText('Edit Listing')).toBeInTheDocument();
    });
  });

  it('should show error when submitting with no photos', async () => {
    // given
    useAuthStore.getState().setUser('owner-1', 'Bob');
    server.use(
      http.get('http://localhost:8080/api/listings/:id', () =>
        HttpResponse.json({ ...mockListingBackend, photos: [] }),
      ),
    );
    const user = userEvent.setup();
    renderWithProviders(<EditListingPage />);
    await waitFor(() => expect(screen.getByText('Save Changes')).toBeInTheDocument());

    // when
    await user.click(screen.getByText('Save Changes'));

    // then
    await waitFor(() => {
      expect(screen.getByText('At least one photo is required.')).toBeInTheDocument();
    });
  });

  it('should update photo count when new file is selected', async () => {
    // given — listing starts with 1 photo
    useAuthStore.getState().setUser('owner-1', 'Bob');
    const user = userEvent.setup();
    renderWithProviders(<EditListingPage />);
    await waitFor(() => expect(screen.getByText(/Photos \(1\/20\)/i)).toBeInTheDocument());

    // when — upload one more file
    const file = new File(['pixel'], 'photo.jpg', { type: 'image/jpeg' });
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    await user.upload(fileInput, file);

    // then — count should update to 2 (1 existing + 1 pending)
    await waitFor(() => {
      expect(screen.getByText(/Photos \(2\/20\)/i)).toBeInTheDocument();
    });
  });
});


import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { server } from '../test/mocks/server';
import { renderWithProviders } from '../test/test-utils';
import { CreateListingPage } from './CreateListingPage';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('CreateListingPage', () => {
  it('should render new listing form', () => {
    renderWithProviders(<CreateListingPage />);
    expect(screen.getByText('New Listing')).toBeInTheDocument();
    expect(screen.getByText('Fill in the property details below')).toBeInTheDocument();
  });

  it('should render all form sections', () => {
    renderWithProviders(<CreateListingPage />);
    expect(screen.getByText('Basic Info')).toBeInTheDocument();
    expect(screen.getByText('Financials & Dimensions')).toBeInTheDocument();
    expect(screen.getByText('Location')).toBeInTheDocument();
  });

  it('should render all required form fields', () => {
    renderWithProviders(<CreateListingPage />);
    expect(screen.getByPlaceholderText(/sunny 2-room/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/describe the property/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/e.g. 450000/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/e.g. 65/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/e.g. Warsaw/i)).toBeInTheDocument();
  });

  it('should render create and cancel buttons', () => {
    renderWithProviders(<CreateListingPage />);
    expect(screen.getByText('Create Listing')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  it('should show error when submitting without photos', async () => {
    // given
    localStorage.setItem('userId', 'user-1');
    const user = userEvent.setup();
    renderWithProviders(<CreateListingPage />);

    // Fill all required fields but add NO photos
    await user.type(screen.getByPlaceholderText(/sunny 2-room/i), 'Test Apartment');
    await user.type(screen.getByPlaceholderText(/e.g. 450000/i), '300000');
    await user.type(screen.getByPlaceholderText(/e.g. 65/i), '50');
    await user.type(screen.getByPlaceholderText(/marszałkowska/i), 'ul. Test 1');
    await user.type(screen.getByPlaceholderText(/e.g. Warsaw/i), 'Krakow');
    await user.type(screen.getByPlaceholderText(/00-001/i), '30-001');

    // when
    await user.click(screen.getByText('Create Listing'));

    // then
    await waitFor(() => {
      expect(screen.getByText('At least one photo is required.')).toBeInTheDocument();
    });
  });

  it('should submit form and create listing', async () => {
    localStorage.setItem('userId', 'user-1');
    const user = userEvent.setup();
    renderWithProviders(<CreateListingPage />);

    await user.type(screen.getByPlaceholderText(/sunny 2-room/i), 'Test Apartment');
    await user.type(screen.getByPlaceholderText(/e.g. 450000/i), '300000');
    await user.type(screen.getByPlaceholderText(/e.g. 65/i), '50');
    await user.type(screen.getByPlaceholderText(/e.g. 3/i), '2');
    await user.type(screen.getByPlaceholderText(/marszałkowska/i), 'ul. Test 1');
    await user.type(screen.getByPlaceholderText(/e.g. Warsaw/i), 'Krakow');
    await user.type(screen.getByPlaceholderText(/mazowieckie/i), 'Malopolskie');
    await user.type(screen.getByPlaceholderText(/00-001/i), '30-001');

    await user.click(screen.getByText('Create Listing'));

    await waitFor(() => {
      expect(screen.queryByText('Creating…')).not.toBeInTheDocument();
    });
  });
});

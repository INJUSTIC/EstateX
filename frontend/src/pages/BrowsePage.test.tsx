import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { server } from '../test/mocks/server';
import { renderWithProviders } from '../test/test-utils';
import { BrowsePage } from './BrowsePage';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('BrowsePage', () => {
  it('should render Browse Listings heading', () => {
    renderWithProviders(<BrowsePage />);
    expect(screen.getByText('Browse Listings')).toBeInTheDocument();
  });

  it('should show loading spinner initially', () => {
    renderWithProviders(<BrowsePage />);
    expect(screen.getByText('Loading…')).toBeInTheDocument();
  });

  it('should render listing cards after loading', async () => {
    renderWithProviders(<BrowsePage />);

    await waitFor(() => {
      expect(screen.getByText('Sunny Apartment')).toBeInTheDocument();
    });
    expect(screen.getByText('1 properties')).toBeInTheDocument();
  });

  it('should render filter inputs', () => {
    renderWithProviders(<BrowsePage />);
    expect(screen.getByPlaceholderText('City…')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Min price')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Max price')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Min rooms')).toBeInTheDocument();
  });

  it('should render property type and transaction type selects', () => {
    renderWithProviders(<BrowsePage />);
    expect(screen.getByText('All types')).toBeInTheDocument();
    expect(screen.getByText('All properties')).toBeInTheDocument();
  });
});

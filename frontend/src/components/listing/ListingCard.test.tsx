import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../../test/test-utils';
import { ListingCard } from './ListingCard';
import { mockListing } from '../../test/mocks/handlers';

describe('ListingCard', () => {
  it('should render listing title', () => {
    renderWithProviders(<ListingCard listing={mockListing} />);
    expect(screen.getByText('Sunny Apartment')).toBeInTheDocument();
  });

  it('should render price in PLN', () => {
    renderWithProviders(<ListingCard listing={mockListing} />);
    expect(screen.getByText(/450.*PLN/)).toBeInTheDocument();
  });

  it('should render city', () => {
    renderWithProviders(<ListingCard listing={mockListing} />);
    expect(screen.getByText(/Warsaw/)).toBeInTheDocument();
  });

  it('should render area and rooms', () => {
    renderWithProviders(<ListingCard listing={mockListing} />);
    expect(screen.getByText(/65/)).toBeInTheDocument();
    expect(screen.getByText(/3/)).toBeInTheDocument();
  });

  it('should render view count', () => {
    renderWithProviders(<ListingCard listing={mockListing} />);
    expect(screen.getByText(/42/)).toBeInTheDocument();
  });

  it('should render cover photo if available', () => {
    renderWithProviders(<ListingCard listing={mockListing} />);
    const img = screen.getByRole('img');
    expect(img).toHaveAttribute('src', '/files/photo.jpg');
  });
});

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { server } from '../test/mocks/server';
import { renderWithProviders } from '../test/test-utils';
import { LoginPage } from './LoginPage';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('LoginPage', () => {
  it('should render sign in form by default', () => {
    renderWithProviders(<LoginPage />);
    expect(screen.getByRole('heading', { name: /sign in/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText('you@example.com')).toBeInTheDocument();
  });

  it('should switch to register mode', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);

    await user.click(screen.getByRole('button', { name: /register/i }));
    expect(screen.getByRole('heading', { name: /create account/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText('John Doe')).toBeInTheDocument();
  });

  it('should register a new user and store userId', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);

    await user.click(screen.getByText('Register'));
    await user.type(screen.getByPlaceholderText('you@example.com'), 'alice@example.com');
    await user.type(screen.getByPlaceholderText('John Doe'), 'Alice');
    await user.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(localStorage.getItem('userId')).toBe('user-1');
    });
  });

  it('should login existing user and store userId', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);

    await user.type(screen.getByPlaceholderText('you@example.com'), 'alice@example.com');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(localStorage.getItem('userId')).toBe('user-1');
    });
  });

  it('should show EstateX branding', () => {
    renderWithProviders(<LoginPage />);
    expect(screen.getByText('EstateX')).toBeInTheDocument();
    expect(screen.getByText('Modern property marketplace')).toBeInTheDocument();
  });
});

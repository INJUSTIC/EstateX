import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { server } from '../test/mocks/server';
import { favouriteApi } from './favourites';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('favouriteApi', () => {
  it('should fetch favourites list', async () => {
    localStorage.setItem('userId', 'user-1');
    const favs = await favouriteApi.getAll();
    expect(favs).toHaveLength(1);
    expect(favs[0].listingId).toBe('listing-1');
    expect(favs[0].userId).toBe('user-1');
  });

  it('should save a favourite', async () => {
    localStorage.setItem('userId', 'user-1');
    const fav = await favouriteApi.save('listing-1');
    expect(fav.id).toBe('fav-1');
    expect(fav.listingId).toBe('listing-1');
    expect(fav.savedAt).toBeTruthy();
  });

  it('should remove a favourite', async () => {
    localStorage.setItem('userId', 'user-1');
    await expect(favouriteApi.remove('listing-1')).resolves.toBeUndefined();
  });
});

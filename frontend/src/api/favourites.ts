import { api } from '../lib/axios';
import type { Favourite } from '../types';

export const favouriteApi = {
  getAll: (): Promise<Favourite[]> =>
    api.get('/api/favourites').then((r) => r.data),

  save: (listingId: string): Promise<Favourite> =>
    api.post(`/api/favourites/${listingId}`).then((r) => r.data),

  remove: (listingId: string): Promise<void> =>
    api.delete(`/api/favourites/${listingId}`).then(() => undefined),
};

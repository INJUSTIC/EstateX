import { api } from '../lib/axios';
import type { Listing, ListingPage, ListingSearchCriteria } from '../types';

// Backend uses different field names — map them to frontend types
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function transformListing(raw: any): Listing {
  return {
    ...raw,
    area: raw.areaSqMeters ?? raw.area ?? 0,
    rooms: raw.numberOfRooms ?? raw.rooms ?? 0,
    photos: (raw.photos ?? []).map((p: any) => ({
      ...p,
      isCover: p.cover ?? p.isCover ?? false,
    })),
  };
}

// Shape sent to the backend (uses backend field names)
function toBackendBody(data: Partial<Listing>) {
  return {
    ...data,
    areaSqMeters: data.area,
    numberOfRooms: data.rooms,
    area: undefined,
    rooms: undefined,
  };
}

export const listingApi = {
  search: (criteria: ListingSearchCriteria): Promise<ListingPage> =>
    api.get('/api/listings', { params: criteria }).then((r) => ({
      ...r.data,
      content: (r.data.items ?? []).map(transformListing),
      number: r.data.page,
    })),

  getById: (id: string): Promise<Listing> =>
    api.get(`/api/listings/${id}`).then((r) => transformListing(r.data)),

  create: (data: Partial<Listing>): Promise<Listing> =>
    api.post('/api/listings', toBackendBody(data)).then((r) => transformListing(r.data)),

  update: (id: string, data: Partial<Listing>): Promise<Listing> =>
    api.put(`/api/listings/${id}`, toBackendBody(data)).then((r) => transformListing(r.data)),

  changeStatus: (id: string, status: string): Promise<Listing> =>
    api.patch(`/api/listings/${id}/status`, { status }).then((r) => transformListing(r.data)),

  delete: (id: string): Promise<void> =>
    api.delete(`/api/listings/${id}`).then(() => undefined),

  getMyListings: (): Promise<Listing[]> =>
    api.get('/api/listings/my').then((r) => (r.data ?? []).map(transformListing)),

  uploadPhoto: (listingId: string, file: File): Promise<Listing> => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/api/listings/${listingId}/photos`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((r) => transformListing(r.data));
  },

  deletePhoto: (listingId: string, photoId: string): Promise<void> =>
    api.delete(`/api/listings/${listingId}/photos/${photoId}`).then(() => undefined),

  setCoverPhoto: (listingId: string, photoId: string): Promise<Listing> =>
    api.patch(`/api/listings/${listingId}/photos/${photoId}/cover`).then((r) => transformListing(r.data)),
};

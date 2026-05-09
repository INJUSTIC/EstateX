import { http, HttpResponse } from 'msw';
import type { User, Listing, ListingPage, Favourite, Conversation, Message } from '../../types';

// ── Fixture data ─────────────────────────────────────────────────────────────

export const mockUser: User = {
  id: 'user-1',
  email: 'alice@example.com',
  displayName: 'Alice',
  phone: null,
  createdAt: '2025-01-15T12:00:00',
  active: true,
  activeListingsCount: 1,
};

export const mockListing: Listing = {
  id: 'listing-1',
  title: 'Sunny Apartment',
  description: 'A lovely apartment in the city centre',
  street: 'ul. Marszalkowska 1',
  city: 'Warsaw',
  voivodeship: 'Mazowieckie',
  postalCode: '00-001',
  country: 'Poland',
  latitude: 52.23,
  longitude: 21.01,
  propertyType: 'APARTMENT',
  transactionType: 'SALE',
  price: 450000,
  area: 65,
  rooms: 3,
  status: 'ACTIVE',
  ownerId: 'owner-1',
  ownerName: 'Bob',
  photos: [{ id: 'photo-1', url: '/files/photo.jpg', isCover: true, position: 0 }],
  viewCount: 42,
  createdAt: '2025-06-01T10:00:00',
  updatedAt: '2025-06-01T10:00:00',
};

// Backend wire format — uses areaSqMeters, numberOfRooms, cover
export const mockListingBackend = {
  id: 'listing-1',
  title: 'Sunny Apartment',
  description: 'A lovely apartment in the city centre',
  street: 'ul. Marszalkowska 1',
  city: 'Warsaw',
  voivodeship: 'Mazowieckie',
  postalCode: '00-001',
  country: 'Poland',
  latitude: 52.23,
  longitude: 21.01,
  propertyType: 'APARTMENT',
  transactionType: 'SALE',
  price: 450000,
  areaSqMeters: 65,
  numberOfRooms: 3,
  status: 'ACTIVE',
  ownerId: 'owner-1',
  ownerName: 'Bob',
  photos: [{ id: 'photo-1', url: '/files/photo.jpg', cover: true }],
  viewCount: 42,
  createdAt: '2025-06-01T10:00:00',
  updatedAt: '2025-06-01T10:00:00',
};

export const mockListingPage: ListingPage = {
  content: [mockListing],
  totalElements: 1,
  totalPages: 1,
  number: 0,
};

// Raw backend wire format — what the API actually returns before client-side mapping
export const mockListingPageRaw = {
  items: [mockListingBackend],
  totalElements: 1,
  totalPages: 1,
  page: 0,
};

export const mockFavourite: Favourite = {
  id: 'fav-1',
  userId: 'user-1',
  listingId: 'listing-1',
  savedAt: '2025-06-02T08:00:00',
};

export const mockConversation: Conversation = {
  id: 'conv-1',
  listingId: 'listing-1',
  listingTitle: 'Sunny Apartment',
  initiatorId: 'user-1',
  ownerId: 'owner-1',
  startedAt: '2025-06-03T09:00:00',
  unreadCount: 2,
};

// Backend wire format — uses listingOwnerId
const mockConversationBackend = {
  id: 'conv-1',
  listingId: 'listing-1',
  listingTitle: 'Sunny Apartment',
  initiatorId: 'user-1',
  listingOwnerId: 'owner-1',
  startedAt: '2025-06-03T09:00:00',
  unreadCount: 2,
  listingStatus: 'ACTIVE',
  initiatorName: 'Alice',
  ownerName: 'Bob',
};

export const mockMessage: Message = {
  id: 'msg-1',
  conversationId: 'conv-1',
  senderId: 'user-1',
  content: 'Hi, is this still available?',
  attachmentUrl: null,
  sentAt: '2025-06-03T09:05:00',
  read: false,
};

// ── Handlers ─────────────────────────────────────────────────────────────────

export const handlers = [
  // Auth
  http.post('http://localhost:8080/api/auth/register', async ({ request }) => {
    const body = await request.json() as { email?: string; displayName?: string };
    return HttpResponse.json({ ...mockUser, email: body.email, displayName: body.displayName }, { status: 200 });
  }),

  http.post('http://localhost:8080/api/auth/login', async ({ request }) => {
    const body = await request.json() as { email?: string };
    return HttpResponse.json({ ...mockUser, email: body.email }, { status: 200 });
  }),

  // Users
  http.get('http://localhost:8080/api/users/me', () => {
    return HttpResponse.json(mockUser);
  }),

  http.put('http://localhost:8080/api/users/me', async () => {
    return HttpResponse.json({ ...mockUser, displayName: 'Updated Alice', phone: '+48111222333' });
  }),

  http.get('http://localhost:8080/api/users/:userId/profile', () => {
    return HttpResponse.json(mockUser);
  }),

  // Listings
  http.get('http://localhost:8080/api/listings', ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') ?? '0');
    return HttpResponse.json({ ...mockListingPageRaw, page });
  }),

  http.get('http://localhost:8080/api/listings/my', () => {
    return HttpResponse.json([mockListingBackend]);
  }),

  http.get('http://localhost:8080/api/listings/my/analytics', () => {
    return HttpResponse.json([{ listingId: 'listing-1', title: 'Sunny Apartment', viewCount: 42 }]);
  }),

  http.get('http://localhost:8080/api/listings/:id', () => {
    return HttpResponse.json(mockListingBackend);
  }),

  http.post('http://localhost:8080/api/listings', async () => {
    return HttpResponse.json({ ...mockListingBackend, id: 'listing-new' });
  }),

  http.put('http://localhost:8080/api/listings/:id', async ({ request }) => {
    const body = await request.json() as Partial<Listing>;
    return HttpResponse.json({ ...mockListing, ...body });
  }),

  http.delete('http://localhost:8080/api/listings/:id', () => {
    return new HttpResponse(null, { status: 204 });
  }),

  http.patch('http://localhost:8080/api/listings/:id/status', async ({ request }) => {
    const body = await request.json() as { status: string };
    return HttpResponse.json({ ...mockListingBackend, status: body.status });
  }),

  http.post('http://localhost:8080/api/listings/:id/photos', async ({ request }) => {
    const ct = request.headers.get('content-type') ?? '';
    if (!ct.includes('multipart/form-data')) return new HttpResponse('Bad Content-Type', { status: 400 });
    const data = await request.formData();
    if (!data.has('file')) return new HttpResponse('Missing file', { status: 400 });
    return HttpResponse.json(mockListingBackend);
  }),

  http.delete('http://localhost:8080/api/listings/:id/photos/:photoId', () => {
    return new HttpResponse(null, { status: 204 });
  }),

  http.patch('http://localhost:8080/api/listings/:id/photos/:photoId/cover', () => {
    return HttpResponse.json(mockListingBackend);
  }),

  // Favourites
  http.get('http://localhost:8080/api/favourites', () => {
    return HttpResponse.json([mockFavourite]);
  }),

  http.post('http://localhost:8080/api/favourites/:listingId', ({ params }) => {
    return HttpResponse.json({ ...mockFavourite, listingId: params.listingId });
  }),

  http.delete('http://localhost:8080/api/favourites/:listingId', () => {
    return new HttpResponse(null, { status: 204 });
  }),

  http.get('http://localhost:8080/api/favourites/:listingId/status', () => {
    return HttpResponse.json(true);
  }),

  // Conversations
  http.get('http://localhost:8080/api/conversations', () => {
    return HttpResponse.json([mockConversationBackend]);
  }),

  http.post('http://localhost:8080/api/conversations', async ({ request }) => {
    const body = await request.json() as { listingId?: string };
    return HttpResponse.json({ ...mockConversationBackend, listingId: body.listingId });
  }),

  http.get('http://localhost:8080/api/conversations/:id/messages', () => {
    return HttpResponse.json({
      items: [mockMessage],
      totalElements: 1,
      totalPages: 1,
      page: 0,
    });
  }),

  http.post('http://localhost:8080/api/conversations/:id/messages', async ({ request }) => {
    const body = await request.json() as { content?: string };
    return HttpResponse.json({
      ...mockMessage,
      id: 'msg-new',
      content: body.content,
      sentAt: new Date().toISOString(),
    });
  }),
];

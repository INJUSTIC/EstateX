// ── Enums ──────────────────────────────────────────────────────────────────
export type PropertyType = 'APARTMENT' | 'HOUSE' | 'STUDIO' | 'COMMERCIAL' | 'LAND';
export type TransactionType = 'RENT' | 'SALE';
export type ListingStatus = 'ACTIVE' | 'ARCHIVED' | 'RENTED';

// ── User ───────────────────────────────────────────────────────────────────
export interface User {
  id: string;
  email: string;
  displayName: string;
  phone: string | null;
  createdAt: string;
  active: boolean;
  activeListingsCount: number;
}

// ── Listing ────────────────────────────────────────────────────────────────
export interface Photo {
  id: string;
  url: string;
  isCover: boolean;
  position: number;
}

export interface Listing {
  id: string;
  title: string;
  description: string;
  street: string;
  city: string;
  voivodeship: string;
  postalCode: string;
  country: string;
  latitude: number | null;
  longitude: number | null;
  propertyType: PropertyType;
  transactionType: TransactionType;
  price: number;
  area: number;
  rooms: number;
  status: ListingStatus;
  ownerId: string;
  ownerName: string;
  photos: Photo[];
  viewCount: number;
  createdAt: string;
  updatedAt: string;
  availableFrom?: string;
}

export interface ListingPage {
  content: Listing[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface ListingSearchCriteria {
  keyword?: string;
  city?: string;
  voivodeship?: string;
  propertyType?: PropertyType;
  transactionType?: TransactionType;
  minPrice?: number;
  maxPrice?: number;
  minArea?: number;
  maxArea?: number;
  minRooms?: number;
  sortBy?: 'CREATED_AT' | 'PRICE' | 'AREA';
  sortDirection?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
  availableEarliest?: string;
  availableLatest?: string;
}

// ── Favourite ──────────────────────────────────────────────────────────────
export interface Favourite {
  id: string;
  userId: string;
  listingId: string;
  savedAt: string;
  listing?: Listing;
}

// ── Chat ───────────────────────────────────────────────────────────────────
export interface Conversation {
  id: string;
  listingId: string | null;
  listingTitle: string;
  initiatorId: string;
  ownerId: string;
  startedAt: string;
  unreadCount?: number;
  listingStatus?: ListingStatus | null;
  initiatorName?: string;
  ownerName?: string;
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  attachmentUrl: string | null;
  sentAt: string;
  read: boolean;
}

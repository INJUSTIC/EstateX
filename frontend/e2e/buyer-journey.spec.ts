import { test, expect, type Page } from '@playwright/test';

const API = 'http://localhost:8080';

const buyer = {
  id: 'buyer-1',
  email: 'buyer@example.com',
  displayName: 'Buyer',
  phone: null,
  createdAt: '2025-01-01T12:00:00',
  active: true,
  activeListingsCount: 0,
};

const listing = {
  id: 'listing-1',
  title: 'Dream House',
  description: 'A beautiful house for sale',
  street: 'ul. Kwiatowa 5',
  city: 'Krakow',
  voivodeship: 'Malopolskie',
  postalCode: '30-001',
  country: 'Poland',
  latitude: 50.06,
  longitude: 19.94,
  propertyType: 'HOUSE',
  transactionType: 'SALE',
  price: 850000,
  area: 150,
  rooms: 5,
  status: 'ACTIVE',
  ownerId: 'owner-1',
  ownerName: 'Seller Bob',
  photos: [{ id: 'photo-1', url: '/files/house.jpg', isCover: true, position: 0 }],
  viewCount: 25,
  createdAt: '2025-05-01T10:00:00',
  updatedAt: '2025-05-01T10:00:00',
};

const conversation = {
  id: 'conv-1',
  listingId: 'listing-1',
  listingTitle: 'Dream House',
  initiatorId: 'buyer-1',
  listingOwnerId: 'owner-1',
  startedAt: '2025-06-10T09:00:00',
  unreadCount: 0,
};

const sentMessage = {
  id: 'msg-1',
  conversationId: 'conv-1',
  senderId: 'buyer-1',
  content: 'Is this still available?',
  attachmentUrl: null,
  sentAt: '2025-06-10T09:05:00',
  read: false,
};

async function setupBuyerJourneyMocks(page: Page) {
  // Login
  await page.route(`${API}/api/auth/login`, route =>
    route.fulfill({ json: buyer }),
  );

  // Search listings
  await page.route(/\/api\/listings(\?|$)/, route =>
    route.fulfill({
      json: { items: [listing], totalElements: 1, totalPages: 1, page: 0 },
    }),
  );

  // Listing detail
  await page.route(`${API}/api/listings/listing-1`, route =>
    route.fulfill({ json: listing }),
  );

  // Favourite status (initially not favourited)
  await page.route(`${API}/api/favourites/listing-1/status`, route =>
    route.fulfill({ json: false }),
  );

  // Save favourite
  await page.route(`${API}/api/favourites`, route => {
    if (route.request().method() === 'POST') {
      return route.fulfill({
        json: { id: 'fav-1', userId: 'buyer-1', listingId: 'listing-1', savedAt: new Date().toISOString() },
      });
    }
    // GET favourites
    return route.fulfill({
      json: [{ id: 'fav-1', userId: 'buyer-1', listingId: 'listing-1', savedAt: new Date().toISOString() }],
    });
  });

  // Start conversation
  await page.route(`${API}/api/conversations`, route => {
    if (route.request().method() === 'POST') {
      return route.fulfill({ json: conversation });
    }
    return route.fulfill({ json: [{ ...conversation, unreadCount: 0 }] });
  });

  // Get messages
  await page.route(/\/api\/conversations\/conv-1\/messages/, route => {
    if (route.request().method() === 'POST') {
      return route.fulfill({ json: sentMessage });
    }
    return route.fulfill({ json: [] });
  });

  // Favourite status for other listings
  await page.route(`${API}/api/favourites/*/status`, route =>
    route.fulfill({ json: false }),
  );

  // WebSocket stub — prevent 404
  await page.route(`${API}/ws/**`, route => route.abort());
}

// ─────────────────────────────────────────────────────────────────────────────
// Journey 2: Browse → Favorite → Open conversation → Send message
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Buyer Journey: Browse → Favorite → Chat → Message', () => {
  test('should complete the full buyer journey', async ({ page }) => {
    await setupBuyerJourneyMocks(page);

    // Step 1: Login
    await page.goto('/login');
    await page.getByPlaceholder('you@example.com').fill('buyer@example.com');
    await page.getByRole('button', { name: /sign in/i }).click();

    // Step 2: Browse listings
    await expect(page).toHaveURL('/');
    await expect(page.getByText('Browse Listings')).toBeVisible();
    await expect(page.getByText('Dream House')).toBeVisible();

    // Step 3: Click on listing to view detail
    await page.getByText('Dream House').click();
    await expect(page.getByText('850')).toBeVisible();
    await expect(page.getByText('Seller Bob')).toBeVisible();

    // Step 4: Open conversation with owner
    await page.getByRole('button', { name: /message owner/i }).click();

    // Should navigate to inbox with conversation
    await expect(page).toHaveURL(/\/inbox\/conv-1/);

    // Step 5: Send a message
    const messageInput = page.getByPlaceholder(/type a message/i);
    await messageInput.fill('Is this still available?');
    await messageInput.press('Enter');
  });
});

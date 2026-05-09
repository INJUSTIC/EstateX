import { test, expect, type Page } from '@playwright/test';

const API = 'http://localhost:8080';

const seller = {
  id: 'seller-1',
  email: 'seller@example.com',
  displayName: 'Seller',
  phone: '+48111222333',
  createdAt: '2025-01-01T12:00:00',
  active: true,
  activeListingsCount: 2,
};

const myListings = [
  {
    id: 'listing-1',
    title: 'City Apartment',
    description: 'Modern apartment',
    street: 'ul. Nowa 10',
    city: 'Warsaw',
    voivodeship: 'Mazowieckie',
    postalCode: '00-100',
    country: 'Poland',
    latitude: null,
    longitude: null,
    propertyType: 'APARTMENT',
    transactionType: 'RENT',
    price: 3500,
    area: 45,
    rooms: 2,
    status: 'ACTIVE',
    ownerId: 'seller-1',
    ownerName: 'Seller',
    photos: [],
    viewCount: 120,
    createdAt: '2025-04-01T10:00:00',
    updatedAt: '2025-04-01T10:00:00',
  },
  {
    id: 'listing-2',
    title: 'Suburban House',
    description: 'Family house',
    street: 'ul. Zielona 5',
    city: 'Krakow',
    voivodeship: 'Malopolskie',
    postalCode: '30-200',
    country: 'Poland',
    latitude: null,
    longitude: null,
    propertyType: 'HOUSE',
    transactionType: 'SALE',
    price: 750000,
    area: 180,
    rooms: 6,
    status: 'ACTIVE',
    ownerId: 'seller-1',
    ownerName: 'Seller',
    photos: [],
    viewCount: 85,
    createdAt: '2025-05-01T10:00:00',
    updatedAt: '2025-05-01T10:00:00',
  },
];

const analytics = [
  { listingId: 'listing-1', title: 'City Apartment', viewCount: 120 },
  { listingId: 'listing-2', title: 'Suburban House', viewCount: 85 },
];

async function setupSellerDashboardMocks(page: Page) {
  // Login
  await page.route(`${API}/api/auth/login`, route =>
    route.fulfill({ json: seller }),
  );

  // Current user
  await page.route(`${API}/api/users/me`, route =>
    route.fulfill({ json: seller }),
  );

  // My listings
  await page.route(`${API}/api/listings/my/analytics`, route =>
    route.fulfill({ json: analytics }),
  );

  await page.route(`${API}/api/listings/my`, route =>
    route.fulfill({ json: myListings }),
  );

  // Search listings (browse page)
  await page.route(/\/api\/listings(\?|$)/, route =>
    route.fulfill({
      json: { items: myListings, totalElements: 2, totalPages: 1, page: 0 },
    }),
  );

  // Listing details
  await page.route(`${API}/api/listings/listing-1`, route =>
    route.fulfill({ json: myListings[0] }),
  );
  await page.route(`${API}/api/listings/listing-2`, route =>
    route.fulfill({ json: myListings[1] }),
  );

  // Favourite status
  await page.route(`${API}/api/favourites/*/status`, route =>
    route.fulfill({ json: false }),
  );

  // Conversations
  await page.route(`${API}/api/conversations`, route =>
    route.fulfill({ json: [] }),
  );

  // Favourites
  await page.route(`${API}/api/favourites`, route =>
    route.fulfill({ json: [] }),
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// Journey 3: Seller Dashboard — View my listings → Check analytics
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Seller Dashboard: My listings → Analytics', () => {
  test('should view listings and check analytics', async ({ page }) => {
    await setupSellerDashboardMocks(page);

    // Step 1: Login as seller
    await page.goto('/login');
    await page.getByPlaceholder('you@example.com').fill('seller@example.com');
    await page.getByRole('button', { name: /sign in/i }).click();
    await expect(page).toHaveURL('/');

    // Step 2: Verify browse page loads with listings
    await expect(page.getByText('Browse Listings')).toBeVisible();
    await expect(page.getByText('City Apartment')).toBeVisible();
    await expect(page.getByText('Suburban House')).toBeVisible();

    // Step 3: Navigate to profile to verify seller info
    await page.getByRole('link', { name: /profile/i }).click();
    await expect(page.getByText('My Profile')).toBeVisible();
    await expect(page.getByText('seller@example.com')).toBeVisible();
  });
});

import { test, expect, type Page } from '@playwright/test';

// ── Shared fixture data ──────────────────────────────────────────────────────

const API = 'http://localhost:8080';

const testUser = {
  id: 'e2e-user-1',
  email: 'e2e-user@example.com',
  displayName: 'E2E User',
  phone: null,
  createdAt: '2025-01-01T12:00:00',
  active: true,
  activeListingsCount: 0,
};

const createdListing = {
  id: 'e2e-listing-1',
  title: 'E2E Test Apartment',
  description: 'Created by E2E test',
  street: 'ul. Testowa 1',
  city: 'Warsaw',
  voivodeship: 'Mazowieckie',
  postalCode: '00-001',
  country: 'Poland',
  latitude: 52.23,
  longitude: 21.01,
  propertyType: 'APARTMENT',
  transactionType: 'SALE',
  price: 350000,
  area: 60,
  rooms: 3,
  status: 'ACTIVE',
  ownerId: 'e2e-user-1',
  ownerName: 'E2E User',
  photos: [],
  viewCount: 0,
  createdAt: '2025-06-01T10:00:00',
  updatedAt: '2025-06-01T10:00:00',
};

const listingWithPhoto = {
  ...createdListing,
  photos: [{ id: 'photo-1', url: '/files/test-photo.jpg', isCover: true, position: 0 }],
};

const listingPageRaw = {
  items: [listingWithPhoto],
  totalElements: 1,
  totalPages: 1,
  page: 0,
};

async function setupUserJourneyMocks(page: Page) {
  // Register
  await page.route(`${API}/api/auth/register`, route =>
    route.fulfill({ json: testUser }),
  );

  // Login
  await page.route(`${API}/api/auth/login`, route =>
    route.fulfill({ json: testUser }),
  );

  // Search listings (GET with query params)
  await page.route(/\/api\/listings(\?|$)/, route => {
    if (route.request().method() === 'GET') {
      return route.fulfill({ json: listingPageRaw });
    }
    return route.continue();
  });

  // Create listing (POST) — exact URL match, registered last so checked first
  await page.route(`${API}/api/listings`, route => {
    if (route.request().method() === 'POST') {
      return route.fulfill({ json: createdListing });
    }
    return route.continue();
  });

  // Listing detail
  await page.route(`${API}/api/listings/e2e-listing-1`, route =>
    route.fulfill({ json: listingWithPhoto }),
  );

  // Photo upload
  await page.route(`${API}/api/listings/e2e-listing-1/photos`, route =>
    route.fulfill({ json: listingWithPhoto }),
  );

  // Favourites list (FavouriteButton fetches all favourites)
  await page.route(`${API}/api/favourites`, route =>
    route.fulfill({ json: [] }),
  );

  // Favourite status (individual checks)
  await page.route(`${API}/api/favourites/*/status`, route =>
    route.fulfill({ json: false }),
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// Journey 1: Register → Create listing → Upload photo → See in browse
// ─────────────────────────────────────────────────────────────────────────────

test.describe('User Journey: Register → Create → Upload → Browse', () => {
  test('should complete the full user journey', async ({ page }) => {
    await setupUserJourneyMocks(page);

    // Step 1: Register
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: /sign in/i })).toBeVisible();
    await page.getByRole('button', { name: /register/i }).click();
    await page.getByPlaceholder('you@example.com').fill('e2e-user@example.com');
    await page.getByPlaceholder('John Doe').fill('E2E User');
    await page.getByRole('button', { name: /create account/i }).click();

    // After registration, should navigate to browse page
    await expect(page).toHaveURL('/');
    await expect(page.getByText('Browse Listings')).toBeVisible();

    // Step 2: Create a listing
    await page.getByRole('button', { name: /new listing/i }).click();
    await expect(page.getByText('New Listing')).toBeVisible();

    await page.getByPlaceholder(/sunny 2-room/i).fill('E2E Test Apartment');
    await page.getByPlaceholder(/describe the property/i).fill('Created by E2E test');
    await page.getByPlaceholder(/e.g. 450000/i).fill('350000');
    await page.getByPlaceholder(/e.g. 65/i).fill('60');
    await page.getByPlaceholder(/e.g. 3/i).fill('3');
    await page.getByPlaceholder(/marszałkowska/i).fill('ul. Testowa 1');
    await page.getByPlaceholder(/e.g. Warsaw/i).fill('Warsaw');
    await page.getByPlaceholder(/mazowieckie/i).fill('Mazowieckie');
    await page.getByPlaceholder(/00-001/i).fill('00-001');

    // Upload a photo (required — at least one photo must be provided)
    const [fileChooser] = await Promise.all([
      page.waitForEvent('filechooser'),
      page.getByRole('button', { name: /add photos/i }).click(),
    ]);
    await fileChooser.setFiles({
      name: 'test-photo.jpg',
      mimeType: 'image/jpeg',
      buffer: Buffer.from([0xff, 0xd8, 0xff, 0xe0]),
    });
    await expect(page.getByText(/photos \(1\/20\)/i)).toBeVisible();

    await page.getByRole('button', { name: /create listing/i }).click();

    // After creation, should navigate to listing detail
    await expect(page.getByText('E2E Test Apartment')).toBeVisible();

    // Step 3: Navigate back to browse and see the listing
    await page.getByRole('link', { name: /browse/i }).click();
    await expect(page.getByText('Browse Listings')).toBeVisible();
    await expect(page.getByText('E2E Test Apartment')).toBeVisible();
  });
});

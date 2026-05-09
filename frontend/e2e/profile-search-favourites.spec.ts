import { test, expect, type Page } from '@playwright/test';

const API = 'http://localhost:8080';

// ── Shared fixture data ──────────────────────────────────────────────────────

const user = {
  id: 'user-1',
  email: 'user@example.com',
  displayName: 'Alice',
  phone: null,
  createdAt: '2025-01-01T12:00:00',
  active: true,
  activeListingsCount: 2,
};

const listing = {
  id: 'listing-1',
  title: 'Sunny Studio',
  description: 'A comfortable studio in the city',
  street: 'ul. Kwiatowa 3',
  city: 'Krakow',
  voivodeship: 'Malopolskie',
  postalCode: '30-001',
  country: 'Poland',
  latitude: null,
  longitude: null,
  propertyType: 'STUDIO',
  transactionType: 'RENT',
  price: 2800,
  area: 30,
  rooms: 1,
  status: 'ACTIVE',
  ownerId: 'owner-2',
  ownerName: 'Bob the Seller',
  photos: [{ id: 'photo-1', url: '/files/studio.jpg', isCover: true, position: 0 }],
  viewCount: 7,
  createdAt: '2025-05-01T10:00:00',
  updatedAt: '2025-05-01T10:00:00',
};

const browsePageRaw = {
  items: [listing],
  totalElements: 1,
  totalPages: 1,
  page: 0,
};

const emptyPageRaw = {
  items: [],
  totalElements: 0,
  totalPages: 0,
  page: 0,
};

async function loginAsUser(page: Page) {
  await page.route(`${API}/api/auth/login`, route =>
    route.fulfill({ json: user }),
  );
  await page.goto('/login');
  await page.getByPlaceholder('you@example.com').fill('user@example.com');
  await page.getByRole('button', { name: /sign in/i }).click();
  await expect(page).toHaveURL('/');
}

// ─────────────────────────────────────────────────────────────────────────────
// Journey 5a: Update user profile
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Profile: update display name', () => {
  test('should save profile changes and show updated name', async ({ page }) => {
    const updatedUser = { ...user, displayName: 'Alice Updated', phone: '+48111222333' };

    await loginAsUser(page);

    await page.route(`${API}/api/users/me`, async route => {
      if (route.request().method() === 'PUT') return route.fulfill({ json: updatedUser });
      return route.fulfill({ json: user });
    });
    await page.route(/\/api\/listings(\?|$)/, route =>
      route.fulfill({ json: browsePageRaw }),
    );

    // Navigate to profile
    await page.getByRole('link', { name: /profile/i }).click();
    await expect(page.getByText('My Profile')).toBeVisible();
    const nameInput = page.locator('input').nth(1);
    await expect(nameInput).toHaveValue('Alice');

    // Enable edit mode
    await page.getByRole('button', { name: /^edit$/i }).click();

    // Change display name and phone
    await nameInput.clear();
    await nameInput.fill('Alice Updated');

    const phoneInput = page.getByPlaceholder(/\+48 000/i);
    await phoneInput.fill('+48111222333');

    // Save
    await page.getByRole('button', { name: /save/i }).click();

    // Success toast should appear
    await expect(page.getByText('Profile updated successfully')).toBeVisible();

    // Updated name is shown
    await expect(nameInput).toHaveValue('Alice Updated');
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Journey 5b: Search and filter listings by city
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Search: filter listings by city', () => {
  test('should show matching listings when city filter is applied', async ({ page }) => {
    await loginAsUser(page);

    // Initially returns all listings; after filtering only the Krakow listing
    const filteredPageRaw = { ...browsePageRaw };
    let lastRequestedCity: string | null = null;

    await page.route(/\/api\/listings(\?|$)/, route => {
      const url = new URL(route.request().url());
      lastRequestedCity = url.searchParams.get('city');
      if (lastRequestedCity && lastRequestedCity.toLowerCase() !== 'krakow') {
        return route.fulfill({ json: emptyPageRaw });
      }
      return route.fulfill({ json: filteredPageRaw });
    });

    // Reload browse page so listings are fetched with mock in place
    await page.goto('/');

    // Browse page loads with default listings
    await expect(page.getByText('Browse Listings')).toBeVisible();
    await expect(page.getByText('Sunny Studio')).toBeVisible();

    // Enter city filter and apply
    await page.getByPlaceholder('City…').fill('Krakow');
    await page.getByRole('button', { name: /apply/i }).click();

    // Listing still visible (city matches)
    await expect(page.getByText('Sunny Studio')).toBeVisible();

    // Enter non-matching city
    await page.getByPlaceholder('City…').fill('Gdansk');
    await page.getByRole('button', { name: /apply/i }).click();

    // No listings should match
    await expect(page.getByText('No listings match your criteria.')).toBeVisible();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Journey 5c: Add listing to favourites and view on Favourites page
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Favourites: save and view favourited listing', () => {
  test('should add listing to favourites and see it on the favourites page', async ({ page }) => {
    const favourite = {
      id: 'fav-1',
      userId: 'user-1',
      listingId: 'listing-1',
      savedAt: '2025-06-01T12:00:00',
    };
    let isFavourited = false;

    await loginAsUser(page);

    await page.route(/\/api\/listings(\?|$)/, route =>
      route.fulfill({ json: browsePageRaw }),
    );
    await page.route(`${API}/api/listings/listing-1`, route =>
      route.fulfill({ json: listing }),
    );
    await page.route(`${API}/api/conversations`, route =>
      route.fulfill({ json: [] }),
    );
    await page.route(`${API}/api/favourites`, route => {
      if (route.request().method() === 'GET') {
        return route.fulfill({ json: isFavourited ? [favourite] : [] });
      }
      return route.continue();
    });
    await page.route(`${API}/api/favourites/listing-1`, route => {
      if (route.request().method() === 'POST') {
        isFavourited = true;
        return route.fulfill({ json: favourite });
      }
      if (route.request().method() === 'DELETE') {
        isFavourited = false;
        return route.fulfill({ status: 204, body: '' });
      }
      return route.continue();
    });

    // Navigate to listing detail as non-owner
    await page.goto('/listings/listing-1');
    await expect(page.getByText('Sunny Studio')).toBeVisible();

    // Click the heart (save to favourites)
    await page.locator('[title="Save to favourites"]').click();

    // Navigate to favourites page
    await page.getByRole('link', { name: /favourites/i }).click();
    await expect(page.getByText('My Favourites')).toBeVisible();
    await expect(page.getByText('1 saved properties')).toBeVisible();
    await expect(page.getByText('Sunny Studio')).toBeVisible();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Journey 5d: Pagination — navigate to next page of listings
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Pagination: navigate to next page', () => {
  test('should show next page of listings when Next is clicked', async ({ page }) => {
    const page1Raw = {
      items: [{ ...listing, id: 'listing-1', title: 'First Page Listing' }],
      totalElements: 2,
      totalPages: 2,
      page: 0,
    };
    const page2Raw = {
      items: [{ ...listing, id: 'listing-2', title: 'Second Page Listing' }],
      totalElements: 2,
      totalPages: 2,
      page: 1,
    };

    await loginAsUser(page);

    await page.route(/\/api\/listings(\?|$)/, route => {
      const url = new URL(route.request().url());
      const pageNum = parseInt(url.searchParams.get('page') ?? '0');
      return route.fulfill({ json: pageNum === 0 ? page1Raw : page2Raw });
    });

    // Reload browse page so listings are fetched with mock in place
    await page.goto('/');

    // First page is visible
    await expect(page.getByText('Browse Listings')).toBeVisible();
    await expect(page.getByText('First Page Listing')).toBeVisible();
    await expect(page.getByText('Page 1 / 2')).toBeVisible();

    // Navigate to next page
    await page.getByRole('button', { name: /next/i }).click();

    // Second page is visible
    await expect(page.getByText('Second Page Listing')).toBeVisible();
    await expect(page.getByText('Page 2 / 2')).toBeVisible();

    // Navigate back to first page
    await page.getByRole('button', { name: /prev/i }).click();
    await expect(page.getByText('First Page Listing')).toBeVisible();
    await expect(page.getByText('Page 1 / 2')).toBeVisible();
  });
});

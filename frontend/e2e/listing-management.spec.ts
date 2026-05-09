import { test, expect, type Page } from '@playwright/test';

const API = 'http://localhost:8080';

// ── Shared fixture data ──────────────────────────────────────────────────────

const owner = {
  id: 'owner-1',
  email: 'owner@example.com',
  displayName: 'Property Owner',
  phone: null,
  createdAt: '2025-01-01T12:00:00',
  active: true,
  activeListingsCount: 1,
};

const listing = {
  id: 'listing-1',
  title: 'City Flat',
  description: 'Nice cozy apartment in the city centre',
  street: 'ul. Nowa 1',
  city: 'Warsaw',
  voivodeship: 'Mazowieckie',
  postalCode: '00-001',
  country: 'Poland',
  latitude: null,
  longitude: null,
  propertyType: 'APARTMENT',
  transactionType: 'SALE',
  price: 500000,
  area: 55,
  rooms: 2,
  status: 'ACTIVE',
  ownerId: 'owner-1',
  ownerName: 'Property Owner',
  photos: [{ id: 'photo-1', url: '/files/photo.jpg', isCover: true, position: 0 }],
  viewCount: 10,
  createdAt: '2025-04-01T10:00:00',
  updatedAt: '2025-04-01T10:00:00',
};

const browsePageRaw = {
  items: [listing],
  totalElements: 1,
  totalPages: 1,
  page: 0,
};

async function loginAsOwner(page: Page) {
  await page.route(`${API}/api/auth/login`, route =>
    route.fulfill({ json: owner }),
  );
  await page.goto('/login');
  await page.getByPlaceholder('you@example.com').fill('owner@example.com');
  await page.getByRole('button', { name: /sign in/i }).click();
  await expect(page).toHaveURL('/');
}

// ─────────────────────────────────────────────────────────────────────────────
// Journey 4a: Create listing with photo upload
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Create Listing: photo upload required', () => {
  test('should block creation without photo then succeed after uploading', async ({ page }) => {
    const createdListing = { ...listing, id: 'new-1', title: 'New Apartment', photos: [] };
    const createdWithPhoto = {
      ...createdListing,
      photos: [{ id: 'p-new', url: '/files/new.jpg', isCover: true, position: 0 }],
    };

    await loginAsOwner(page);

    // Mocks
    await page.route(/\/api\/listings(\?|$)/, route => {
      if (route.request().method() === 'POST') return route.fulfill({ json: createdListing });
      return route.fulfill({ json: browsePageRaw });
    });
    await page.route(`${API}/api/listings/new-1`, route =>
      route.fulfill({ json: createdWithPhoto }),
    );
    await page.route(`${API}/api/listings/new-1/photos`, route =>
      route.fulfill({ json: createdWithPhoto }),
    );
    await page.route(`${API}/api/conversations`, route =>
      route.fulfill({ json: [] }),
    );

    // Navigate to create form
    await page.getByRole('button', { name: /new listing/i }).click();
    await expect(page.getByRole('heading', { name: 'New Listing' })).toBeVisible();

    // Fill required fields
    await page.getByPlaceholder(/sunny 2-room/i).fill('New Apartment');
    await page.getByPlaceholder(/e.g. 450000/i).fill('400000');
    await page.getByPlaceholder(/e.g. 65/i).fill('50');
    await page.getByPlaceholder(/marszałkowska/i).fill('ul. Testowa 1');
    await page.getByPlaceholder(/e.g. Warsaw/i).fill('Warsaw');
    await page.getByPlaceholder(/00-001/i).fill('00-001');

    // Try to submit without a photo — should show validation error
    await page.getByRole('button', { name: /create listing/i }).click();
    await expect(page.getByText('At least one photo is required.')).toBeVisible();

    // Upload a photo
    const [fileChooser] = await Promise.all([
      page.waitForEvent('filechooser'),
      page.getByRole('button', { name: /add photos/i }).click(),
    ]);
    await fileChooser.setFiles({
      name: 'apartment.jpg',
      mimeType: 'image/jpeg',
      buffer: Buffer.from([0xff, 0xd8, 0xff, 0xe0]),
    });
    await expect(page.getByText(/photos \(1\/20\)/i)).toBeVisible();

    // Validation error should clear after adding photo
    await expect(page.getByText('At least one photo is required.')).not.toBeVisible();

    // Submit successfully
    await page.getByRole('button', { name: /create listing/i }).click();

    // Should navigate to the new listing's detail page
    await expect(page).toHaveURL('/listings/new-1');
    await expect(page.getByText('New Apartment')).toBeVisible();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Journey 4b: Edit listing
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Edit Listing: update title and price', () => {
  test('should update listing details and reflect on detail page', async ({ page }) => {
    const updatedListing = { ...listing, title: 'Renovated City Flat', price: 550000 };
    let hasBeenEdited = false;

    await loginAsOwner(page);

    await page.route(/\/api\/listings(\?|$)/, route =>
      route.fulfill({ json: browsePageRaw }),
    );
    await page.route(`${API}/api/listings/listing-1`, async route => {
      if (route.request().method() === 'PUT') {
        hasBeenEdited = true;
        return route.fulfill({ json: updatedListing });
      }
      return route.fulfill({ json: hasBeenEdited ? updatedListing : listing });
    });
    await page.route(`${API}/api/conversations`, route =>
      route.fulfill({ json: [] }),
    );

    // Navigate to the listing detail page
    await page.goto('/listings/listing-1');
    await expect(page.getByText('City Flat')).toBeVisible();

    // Click Edit Listing
    await page.getByRole('button', { name: /edit listing/i }).click();
    await expect(page).toHaveURL('/listings/listing-1/edit');
    await expect(page.getByText('Edit Listing')).toBeVisible();

    // Update title and price
    const titleInput = page.getByPlaceholder(/sunny 2-room/i);
    await titleInput.clear();
    await titleInput.fill('Renovated City Flat');

    const priceInput = page.getByPlaceholder(/e.g. 450000/i);
    await priceInput.clear();
    await priceInput.fill('550000');

    // Save
    await page.getByRole('button', { name: /save changes/i }).click();

    // Should navigate back to detail page with updated data
    await expect(page).toHaveURL('/listings/listing-1');
    await expect(page.getByText('Renovated City Flat')).toBeVisible();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Journey 4c: Delete listing from My Listings
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Delete Listing: remove from My Listings', () => {
  test('should delete listing and show empty state', async ({ page }) => {
    let listingExists = true;

    await loginAsOwner(page);

    await page.route(`${API}/api/listings/my`, route =>
      route.fulfill({ json: listingExists ? [listing] : [] }),
    );
    await page.route(`${API}/api/listings/listing-1`, async route => {
      if (route.request().method() === 'DELETE') {
        listingExists = false;
        return route.fulfill({ status: 204, body: '' });
      }
      return route.continue();
    });

    // Accept the confirm dialog automatically
    page.on('dialog', dialog => dialog.accept());

    // Navigate to My Listings
    await page.getByRole('link', { name: /my listings/i }).click();
    await expect(page.getByText('My Listings')).toBeVisible();
    await expect(page.getByText('City Flat')).toBeVisible();

    // Click delete button
    await page.locator('[title="Delete"]').click();

    // List refreshes — listing should be gone
    await expect(page.getByText('City Flat')).not.toBeVisible();
    await expect(page.getByText("You haven't created any listings yet.")).toBeVisible();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Journey 4d: Archive and restore listing
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Archive Listing: toggle status in My Listings', () => {
  test('should archive active listing then restore it', async ({ page }) => {
    const archivedListing = { ...listing, status: 'ARCHIVED' };
    const restoredListing = { ...listing, status: 'ACTIVE' };
    let currentStatus = 'ACTIVE';

    await loginAsOwner(page);

    await page.route(`${API}/api/listings/my`, route =>
      route.fulfill({ json: [{ ...listing, status: currentStatus }] }),
    );
    await page.route(`${API}/api/listings/listing-1/status`, async route => {
      const body = await route.request().postDataJSON();
      currentStatus = body.status;
      return route.fulfill({ json: { ...listing, status: currentStatus } });
    });

    // Navigate to My Listings
    await page.getByRole('link', { name: /my listings/i }).click();
    await expect(page.getByText('City Flat')).toBeVisible();
    await expect(page.getByText('ACTIVE')).toBeVisible();

    // Archive the listing
    await page.locator('[title="Archive (hide)"]').click();
    await expect(page.getByText('ARCHIVED')).toBeVisible();

    // Restore the listing
    await page.locator('[title="Unarchive (show)"]').click();
    await expect(page.getByText('ACTIVE')).toBeVisible();
  });
});

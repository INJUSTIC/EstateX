import { describe, it, expect, beforeAll, afterAll, afterEach, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../test/mocks/server';
import { listingApi } from './listings';
import { api } from '../lib/axios';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('listingApi', () => {
  it('should search listings with default criteria', async () => {
    const page = await listingApi.search({});
    expect(page.content).toHaveLength(1);
    expect(page.content[0].title).toBe('Sunny Apartment');
    expect(page.totalElements).toBe(1);
  });

  it('should return listing page structure', async () => {
    const page = await listingApi.search({ page: 0, size: 20 });
    expect(page).toHaveProperty('content');
    expect(page).toHaveProperty('totalElements');
    expect(page).toHaveProperty('totalPages');
    expect(page).toHaveProperty('number');
  });

  it('should forward page param to the server', async () => {
    const page = await listingApi.search({ page: 3 });
    expect(page.number).toBe(3);
  });

  it('should fetch listing by id', async () => {
    const listing = await listingApi.getById('listing-1');
    expect(listing.id).toBe('listing-1');
    expect(listing.title).toBe('Sunny Apartment');
    expect(listing.price).toBe(450000);
    expect(listing.area).toBe(65);
    expect(listing.rooms).toBe(3);
    expect(listing.photos).toHaveLength(1);
    expect(listing.photos[0].isCover).toBe(true);
  });

  it('should create a new listing', async () => {
    localStorage.setItem('userId', 'user-1');
    const listing = await listingApi.create({
      title: 'New Apartment',
      city: 'Krakow',
      propertyType: 'APARTMENT',
      transactionType: 'SALE',
      price: 300000,
      area: 50,
      rooms: 2,
    });
    expect(listing.id).toBe('listing-new');
  });

  it('should update a listing', async () => {
    localStorage.setItem('userId', 'user-1');
    const listing = await listingApi.update('listing-1', { title: 'Updated Apartment', city: 'Gdansk' });
    expect(listing.id).toBe('listing-1');
    expect(listing.title).toBe('Updated Apartment');
    expect(listing.city).toBe('Gdansk');
  });

  it('should delete a listing', async () => {
    localStorage.setItem('userId', 'user-1');
    await expect(listingApi.delete('listing-1')).resolves.toBeUndefined();
  });

  it('should change listing status', async () => {
    // given
    localStorage.setItem('userId', 'user-1');

    // when
    const listing = await listingApi.changeStatus('listing-1', 'ARCHIVED');

    // then
    expect(listing.id).toBe('listing-1');
    expect(listing.status).toBe('ARCHIVED');
  });

  it('should get my listings', async () => {
    // given
    localStorage.setItem('userId', 'user-1');

    // when
    const listings = await listingApi.getMyListings();

    // then
    expect(listings).toHaveLength(1);
    expect(listings[0].title).toBe('Sunny Apartment');
    expect(listings[0].area).toBe(65);
  });

  it('should upload a photo', async () => {
    // given
    localStorage.setItem('userId', 'user-1');
    const file = new File(['test'], 'photo.jpg', { type: 'image/jpeg' });

    // when
    const listing = await listingApi.uploadPhoto('listing-1', file);

    // then
    expect(listing.id).toBe('listing-1');
  });

  it('should send photo with multipart content type', async () => {
    // given
    localStorage.setItem('userId', 'user-1');
    const file = new File(['test'], 'photo.jpg', { type: 'image/jpeg' });
    const postSpy = vi.spyOn(api, 'post');

    // when
    await listingApi.uploadPhoto('listing-1', file);

    // then
    expect(postSpy).toHaveBeenCalledWith(
      '/api/listings/listing-1/photos',
      expect.any(FormData),
      expect.objectContaining({
        headers: expect.objectContaining({ 'Content-Type': 'multipart/form-data' }),
      })
    );
    postSpy.mockRestore();
  });

  it('should delete a photo', async () => {
    // given
    localStorage.setItem('userId', 'user-1');

    // when / then
    await expect(listingApi.deletePhoto('listing-1', 'photo-1')).resolves.toBeUndefined();
  });

  it('should set cover photo', async () => {
    // given
    localStorage.setItem('userId', 'user-1');

    // when
    const listing = await listingApi.setCoverPhoto('listing-1', 'photo-1');

    // then
    expect(listing.id).toBe('listing-1');
  });

  it('should default photos to empty array when field is missing', async () => {
    // given
    server.use(
      http.get('http://localhost:8080/api/listings/:id', () =>
        HttpResponse.json({ id: 'listing-1', title: 'Bare', areaSqMeters: 10 })
      )
    );

    // when
    const listing = await listingApi.getById('listing-1');

    // then
    expect(listing.photos).toEqual([]);
  });

  it('should default photo isCover to false when cover field is absent', async () => {
    // given
    server.use(
      http.get('http://localhost:8080/api/listings/:id', () =>
        HttpResponse.json({ id: 'listing-1', photos: [{ id: 'p1', url: '/t.jpg' }] })
      )
    );

    // when
    const listing = await listingApi.getById('listing-1');

    // then
    expect(listing.photos[0].isCover).toBe(false);
  });

  it('should handle missing items in search response', async () => {
    // given
    server.use(
      http.get('http://localhost:8080/api/listings', () =>
        HttpResponse.json({ totalElements: 0, totalPages: 0, page: 0 })
      )
    );

    // when
    const page = await listingApi.search({});

    // then
    expect(page.content).toEqual([]);
  });

  it('should handle null getMyListings response', async () => {
    // given
    server.use(
      http.get('http://localhost:8080/api/listings/my', () =>
        HttpResponse.json(null)
      )
    );
    localStorage.setItem('userId', 'user-1');

    // when
    const listings = await listingApi.getMyListings();

    // then
    expect(listings).toEqual([]);
  });
});

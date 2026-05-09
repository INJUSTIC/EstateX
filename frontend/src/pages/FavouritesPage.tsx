import { useQuery } from '@tanstack/react-query';
import { favouriteApi } from '../api/favourites';
import { listingApi } from '../api/listings';
import { ListingCard } from '../components/listing/ListingCard';
import { Heart, AlertTriangle } from 'lucide-react';
import type { Listing } from '../types';

export function FavouritesPage() {
  const { data: favs = [], isLoading } = useQuery({
    queryKey: ['favourites'],
    queryFn: favouriteApi.getAll,
  });

  const listingQueries = favs.map((f) => f.listingId);

  return (
    <div className="fade-in">
      <div className="section-header">
        <div>
          <h1 className="section-title">My Favourites</h1>
          <p className="section-subtitle">{favs.length} saved properties</p>
        </div>
      </div>

      {isLoading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : favs.length === 0 ? (
        <div className="empty-state">
          <Heart size={48} />
          <p>No saved properties yet. Browse listings and heart the ones you love.</p>
        </div>
      ) : (
        <FavouriteGrid listingIds={listingQueries} />
      )}
    </div>
  );
}

function FavouriteGrid({ listingIds }: { listingIds: string[] }) {
  const queries = listingIds.map((id) => {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const q = useQuery({
      queryKey: ['listing', id],
      queryFn: () => listingApi.getById(id),
    });
    return { data: q.data, isError: q.isError, id };
  });

  return (
    <div className="listing-grid">
      {queries.map((q) => {
        if (q.isError) {
          return (
            <div key={q.id} className="listing-card" style={{ opacity: 0.5, pointerEvents: 'none' }}>
              <div className="listing-card__img-placeholder">
                <AlertTriangle size={32} />
              </div>
              <div className="listing-card__body">
                <p style={{ fontSize: 13, color: 'var(--danger)', fontWeight: 600 }}>Listing deleted</p>
                <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>This listing is no longer available.</p>
              </div>
            </div>
          );
        }
        if (!q.data) return null;
        const listing = q.data as Listing;
        if (listing.status !== 'ACTIVE') {
          return (
            <div key={listing.id} style={{ position: 'relative' }}>
              <div style={{ position: 'absolute', inset: 0, zIndex: 2, borderRadius: 'var(--radius-lg)',
                background: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center',
                flexDirection: 'column', gap: 6, color: '#fff', pointerEvents: 'none' }}>
                <AlertTriangle size={28} />
                <span style={{ fontSize: 13, fontWeight: 600 }}>No longer available</span>
              </div>
              <div style={{ opacity: 0.5, pointerEvents: 'none' }}>
                <ListingCard listing={listing} />
              </div>
            </div>
          );
        }
        return <ListingCard key={listing.id} listing={listing} />;
      })}
    </div>
  );
}

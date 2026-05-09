import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { listingApi } from '../api/listings';
import { chatApi } from '../api/chat';
import { useAuthStore } from '../store/auth';
import { FavouriteButton } from '../components/favourite/FavouriteButton';
import { MapPin, Maximize2, BedDouble, Eye, MessageCircle, ChevronLeft, Trash2, Pencil, Calendar } from 'lucide-react';

export function ListingDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { userId } = useAuthStore();
  const [selectedPhotoId, setSelectedPhotoId] = useState<string | null>(null);

  const { data: listing, isLoading } = useQuery({
    queryKey: ['listing', id],
    queryFn: () => listingApi.getById(id!),
    enabled: !!id,
  });

  const deleteListing = useMutation({
    mutationFn: () => listingApi.delete(id!),
    onSuccess: () => navigate('/'),
  });

  const { data: conversations = [] } = useQuery({
    queryKey: ['conversations'],
    queryFn: chatApi.getConversations,
    enabled: !!userId,
  });

  if (isLoading) return <div className="loading-center"><div className="spinner" /></div>;
  if (!listing) return <div className="empty-state"><p>Listing not found.</p></div>;

  const isOwner = listing.ownerId === userId;
  const photos = listing.photos ?? [];
  const cover = photos.find((p) => p.isCover) ?? photos[0];
  const displayedPhoto = photos.find((p) => p.id === selectedPhotoId) ?? cover;

  return (
    <div className="fade-in" style={{ maxWidth: 880, margin: '0 auto' }}>
      <button className="btn btn-ghost btn-sm mb-4" onClick={() => navigate(-1)}>
        <ChevronLeft size={15} /> Back
      </button>

      {/* Main photo */}
      {displayedPhoto ? (
        <img src={displayedPhoto.url} alt={listing.title}
          style={{ width: '100%', height: 380, objectFit: 'cover',
            borderRadius: 'var(--radius-lg)', marginBottom: 24 }} />
      ) : (
        <div style={{ width: '100%', height: 280, background: 'var(--bg-elevated)',
          borderRadius: 'var(--radius-lg)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', marginBottom: 24, color: 'var(--text-muted)' }}>
          <MapPin size={48} />
        </div>
      )}

      {/* Thumbnail strip */}
      {photos.length > 1 && (
        <div className="flex gap-2 mb-6" style={{ overflowX: 'auto', paddingBottom: 4 }}>
          {photos.map((p) => (
            <img key={p.id} src={p.url} alt=""
              onClick={() => setSelectedPhotoId(p.id)}
              style={{ height: 72, width: 108, objectFit: 'cover', borderRadius: 8,
                flexShrink: 0, cursor: 'pointer',
                border: p.id === displayedPhoto?.id ? '2px solid var(--accent)' : '2px solid transparent',
                opacity: p.id === displayedPhoto?.id ? 1 : 0.65,
                transition: 'opacity 0.15s, border-color 0.15s' }} />
          ))}
        </div>
      )}

      <div className="flex gap-4" style={{ alignItems: 'flex-start', flexWrap: 'wrap' }}>
        {/* Details */}
        <div style={{ flex: 1, minWidth: 0 }}>
          <div className="flex items-center gap-3 mb-2">
            <span className="listing-card__badge">{listing.transactionType}</span>
            {isOwner && (
              <span className="listing-card__badge" style={{ background: listing.status === 'ACTIVE' ? 'rgba(34,197,94,0.12)' : 'rgba(234,179,8,0.15)',
                color: listing.status === 'ACTIVE' ? 'var(--success)' : '#ca8a04' }}>
                {listing.status}
              </span>
            )}
            <span className="flex items-center gap-2 text-sm text-muted" style={{ marginLeft: 'auto' }}>
              <Eye size={13} /> {listing.viewCount} views
            </span>
          </div>

          <h1 style={{ fontSize: 26, fontWeight: 800, marginBottom: 8 }}>{listing.title}</h1>

          <div className="flex items-center gap-2 text-sm text-muted mb-4">
            <MapPin size={14} />
            {[listing.street, listing.city, listing.voivodeship, listing.postalCode].filter(Boolean).join(', ')}
          </div>

          <div className="flex gap-4 mb-6" style={{ flexWrap: 'wrap' }}>
            <div className="card" style={{ padding: '12px 20px', flex: 1, minWidth: 140 }}>
              <p style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 2 }}>PRICE</p>
              <p style={{ fontSize: 24, fontWeight: 800, color: 'var(--accent-hover)' }}>
                {listing.price.toLocaleString('pl-PL')} PLN
              </p>
            </div>
            <div className="card" style={{ padding: '12px 20px' }}>
              <p style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 2 }}>AREA</p>
              <p className="flex items-center gap-2" style={{ fontSize: 18, fontWeight: 700 }}>
                <Maximize2 size={16} /> {listing.area} m²
              </p>
            </div>
            {listing.rooms && (
              <div className="card" style={{ padding: '12px 20px' }}>
                <p style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 2 }}>ROOMS</p>
                <p className="flex items-center gap-2" style={{ fontSize: 18, fontWeight: 700 }}>
                  <BedDouble size={16} /> {listing.rooms}
                </p>
              </div>
            )}
            {listing.availableFrom && (
              <div className="card" style={{ padding: '12px 20px' }}>
                <p style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 2 }}>AVAILABLE FROM</p>
                <p className="flex items-center gap-2" style={{ fontSize: 18, fontWeight: 700 }}>
                  <Calendar size={16} /> {listing.availableFrom}
                </p>
              </div>
            )}
          </div>

          <p style={{ fontSize: 14, color: 'var(--text-secondary)', lineHeight: 1.7 }}>
            {listing.description}
          </p>
        </div>

        {/* Actions */}
        <div style={{ width: 220, minWidth: 200, flexShrink: 0, display: 'flex', flexDirection: 'column', gap: 10 }}>
          <div className="card" style={{ padding: 16 }}>
            <p style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>Listed by</p>
            <p style={{ fontWeight: 600 }}>{listing.ownerName}</p>
          </div>

          {!isOwner && (
            <>
              <button className="btn btn-primary w-full"
                onClick={() => {
                  const existing = conversations.find((c) => c.listingId === listing.id);
                  navigate(existing ? `/inbox/${existing.id}` : `/inbox/new?listingId=${listing.id}`);
                }}>
                <MessageCircle size={16} />
                Message Owner
              </button>
              <FavouriteButton listingId={listing.id} />
            </>
          )}

          {isOwner && (
            <>
              <button className="btn btn-secondary w-full"
                onClick={() => navigate(`/listings/${id}/edit`)}>
                <Pencil size={16} /> Edit Listing
              </button>
              <button className="btn btn-danger w-full"
                onClick={() => { if (confirm('Delete this listing?')) deleteListing.mutate(); }}
                disabled={deleteListing.isPending}>
                <Trash2 size={16} /> Delete Listing
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

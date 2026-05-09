import { useNavigate } from 'react-router-dom';
import type { Listing } from '../../types';
import { MapPin, Maximize2, BedDouble, Eye, Calendar } from 'lucide-react';
import { FavouriteButton } from '../favourite/FavouriteButton';

interface Props {
  listing: Listing;
}

export function ListingCard({ listing }: Props) {
  const navigate = useNavigate();
  const cover = listing.photos.find((p) => p.isCover) ?? listing.photos[0];

  return (
    <div className="listing-card" onClick={() => navigate(`/listings/${listing.id}`)}>
      {cover ? (
        <img className="listing-card__img" src={cover.url} alt={listing.title} />
      ) : (
        <div className="listing-card__img-placeholder">
          <MapPin size={32} />
        </div>
      )}

      <div className="listing-card__body">
        <div className="flex items-center justify-between gap-2">
          <span className="listing-card__price">{listing.price.toLocaleString('pl-PL')} PLN</span>
          <span className="listing-card__badge">{listing.transactionType}</span>
        </div>

        <p className="listing-card__title">{listing.title}</p>

        <div className="listing-card__meta">
          <span className="flex items-center gap-2">
            <MapPin size={12} />{listing.city}
          </span>
          <span className="flex items-center gap-2">
            <Maximize2 size={12} />{listing.area} m²
          </span>
          {listing.rooms && (
            <span className="flex items-center gap-2">
              <BedDouble size={12} />{listing.rooms}
            </span>
          )}
          {listing.availableFrom && (
            <span className="flex items-center gap-2">
              <Calendar size={12} />{listing.availableFrom}
            </span>
          )}
          <span className="flex items-center gap-2" style={{ marginLeft: 'auto' }}>
            <Eye size={12} />{listing.viewCount}
          </span>
        </div>

        <div className="flex items-center justify-between mt-2">
          <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>{listing.propertyType}</span>
          <span onClick={(e) => e.stopPropagation()}>
            <FavouriteButton listingId={listing.id} />
          </span>
        </div>
      </div>
    </div>
  );
}

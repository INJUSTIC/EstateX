import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { listingApi } from '../api/listings';
import { Building2, Archive, Eye, Pencil, Trash2, ArchiveRestore } from 'lucide-react';

export function MyListingsPage() {
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { data: listings = [], isLoading } = useQuery({
    queryKey: ['my-listings'],
    queryFn: listingApi.getMyListings,
  });

  const changeStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      listingApi.changeStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['my-listings'] }),
  });

  const deleteListing = useMutation({
    mutationFn: (id: string) => listingApi.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['my-listings'] }),
  });

  const statusColor: Record<string, string> = {
    ACTIVE: 'rgba(34,197,94,0.12)',
    ARCHIVED: 'rgba(234,179,8,0.15)',
    RENTED: 'rgba(99,102,241,0.12)',
  };
  const statusText: Record<string, string> = {
    ACTIVE: 'var(--success)',
    ARCHIVED: '#ca8a04',
    RENTED: 'var(--accent)',
  };

  return (
    <div className="fade-in">
      <div className="section-header">
        <div>
          <h1 className="section-title">My Listings</h1>
          <p className="section-subtitle">
            {listings.length} listing{listings.length !== 1 ? 's' : ''}
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/listings/new')}>
          + New Listing
        </button>
      </div>

      {isLoading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : listings.length === 0 ? (
        <div className="empty-state">
          <Building2 size={48} />
          <p>You haven't created any listings yet.</p>
        </div>
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          {listings.map((l, i) => (
            <div key={l.id} style={{
              display: 'flex', alignItems: 'center', gap: 14, padding: '14px 20px',
              borderBottom: i < listings.length - 1 ? '1px solid var(--border)' : undefined,
            }}>
              {/* Cover thumb */}
              {(() => {
                const cover = l.photos.find((p) => p.isCover) ?? l.photos[0];
                return cover ? (
                  <img src={cover.url} alt="" style={{ width: 60, height: 45, objectFit: 'cover', borderRadius: 6, flexShrink: 0 }} />
                ) : (
                  <div style={{ width: 60, height: 45, borderRadius: 6, background: 'var(--bg-elevated)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                    <Building2 size={18} style={{ color: 'var(--text-muted)' }} />
                  </div>
                );
              })()}

              {/* Info */}
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontWeight: 600, fontSize: 14, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {l.title}
                </p>
                <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                  {l.city} · {l.price.toLocaleString('pl-PL')} PLN · {l.area} m²
                  {l.rooms ? ` · ${l.rooms} rooms` : ''}
                </p>
              </div>

              {/* Status badge */}
              <span style={{
                fontSize: 11, fontWeight: 700, padding: '3px 8px', borderRadius: 99,
                background: statusColor[l.status] ?? 'var(--bg-elevated)',
                color: statusText[l.status] ?? 'var(--text-muted)',
                flexShrink: 0,
              }}>
                {l.status}
              </span>

              {/* Actions */}
              <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                <button className="btn btn-ghost btn-sm" title="View" onClick={() => navigate(`/listings/${l.id}`)}>
                  <Eye size={15} />
                </button>
                <button className="btn btn-ghost btn-sm" title="Edit" onClick={() => navigate(`/listings/${l.id}/edit`)}>
                  <Pencil size={15} />
                </button>
                {l.status === 'ACTIVE' ? (
                  <button className="btn btn-ghost btn-sm" title="Archive (hide)"
                    disabled={changeStatus.isPending}
                    onClick={() => changeStatus.mutate({ id: l.id, status: 'ARCHIVED' })}>
                    <Archive size={15} />
                  </button>
                ) : l.status === 'ARCHIVED' ? (
                  <button className="btn btn-ghost btn-sm" title="Unarchive (show)"
                    disabled={changeStatus.isPending}
                    onClick={() => changeStatus.mutate({ id: l.id, status: 'ACTIVE' })}>
                    <ArchiveRestore size={15} />
                  </button>
                ) : null}
                <button className="btn btn-ghost btn-sm" title="Delete"
                  style={{ color: 'var(--danger)' }}
                  disabled={deleteListing.isPending}
                  onClick={() => { if (confirm('Delete this listing?')) deleteListing.mutate(l.id); }}>
                  <Trash2 size={15} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

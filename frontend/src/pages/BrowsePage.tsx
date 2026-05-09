import { useState, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { listingApi } from '../api/listings';
import { ListingCard } from '../components/listing/ListingCard';
import type { ListingSearchCriteria, PropertyType, TransactionType } from '../types';
import { SlidersHorizontal, Search, ArrowUpDown } from 'lucide-react';

const PROPERTY_TYPES: PropertyType[] = ['APARTMENT', 'HOUSE', 'STUDIO', 'COMMERCIAL', 'LAND'];
const TRANSACTION_TYPES: TransactionType[] = ['RENT', 'SALE'];

const SORT_OPTIONS = [
  { label: 'Newest', sortBy: 'CREATED_AT' as const, sortDirection: 'DESC' as const },
  { label: 'Oldest', sortBy: 'CREATED_AT' as const, sortDirection: 'ASC' as const },
  { label: 'Price ↑', sortBy: 'PRICE' as const, sortDirection: 'ASC' as const },
  { label: 'Price ↓', sortBy: 'PRICE' as const, sortDirection: 'DESC' as const },
];

export function BrowsePage() {
  const [draft, setDraft] = useState<ListingSearchCriteria>({ page: 0, size: 20 });
  const [criteria, setCriteria] = useState<ListingSearchCriteria>({ page: 0, size: 20 });

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['listings', criteria],
    queryFn: () => listingApi.search(criteria),
    placeholderData: (prev) => prev,
  });

  const updateDraft = (patch: Partial<ListingSearchCriteria>) =>
    setDraft((c) => ({ ...c, ...patch }));

  const applyFilters = () =>
    setCriteria({ ...draft, page: 0 });

  const handleKeywordKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter') applyFilters();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draft]);

  const sortKey = `${criteria.sortBy ?? 'CREATED_AT'}_${criteria.sortDirection ?? 'DESC'}`;

  return (
    <div className="fade-in">
      <div className="section-header">
        <div>
          <h1 className="section-title">Browse Listings</h1>
          <p className="section-subtitle">
            {data ? `${data.totalElements.toLocaleString()} properties` : 'Loading…'}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <ArrowUpDown size={14} style={{ color: 'var(--text-muted)' }} />
          <select className="filter-select"
            value={sortKey}
            onChange={(e) => {
              const opt = SORT_OPTIONS.find((o) => `${o.sortBy}_${o.sortDirection}` === e.target.value);
              if (opt) setCriteria((c) => ({ ...c, sortBy: opt.sortBy, sortDirection: opt.sortDirection, page: 0 }));
            }}>
            {SORT_OPTIONS.map((o) => (
              <option key={`${o.sortBy}_${o.sortDirection}`} value={`${o.sortBy}_${o.sortDirection}`}>
                {o.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Filters */}
      <div className="filter-bar">
        <SlidersHorizontal size={16} style={{ color: 'var(--text-muted)', alignSelf: 'center' }} />

        <div style={{ position: 'relative', flex: 1, minWidth: 180, maxWidth: 260 }}>
          <Search size={14} style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input className="input" style={{ paddingLeft: 30 }} placeholder="Search by name…"
            value={draft.keyword ?? ''}
            onChange={(e) => updateDraft({ keyword: e.target.value || undefined })}
            onKeyDown={handleKeywordKeyDown} />
        </div>

        <input className="input" style={{ maxWidth: 150 }} placeholder="City…"
          value={draft.city ?? ''}
          onChange={(e) => updateDraft({ city: e.target.value || undefined })} />

        <select className="filter-select"
          value={draft.transactionType ?? ''}
          onChange={(e) => updateDraft({ transactionType: (e.target.value as TransactionType) || undefined })}>
          <option value="">All types</option>
          {TRANSACTION_TYPES.map((t) => <option key={t}>{t}</option>)}
        </select>

        <select className="filter-select"
          value={draft.propertyType ?? ''}
          onChange={(e) => updateDraft({ propertyType: (e.target.value as PropertyType) || undefined })}>
          <option value="">All properties</option>
          {PROPERTY_TYPES.map((t) => <option key={t}>{t}</option>)}
        </select>

        <input className="input no-spinners" style={{ maxWidth: 120 }} placeholder="Min price"
          type="number" value={draft.minPrice ?? ''}
          onChange={(e) => updateDraft({ minPrice: e.target.value ? +e.target.value : undefined })} />

        <input className="input no-spinners" style={{ maxWidth: 120 }} placeholder="Max price"
          type="number" value={draft.maxPrice ?? ''}
          onChange={(e) => updateDraft({ maxPrice: e.target.value ? +e.target.value : undefined })} />

        <input className="input no-spinners" style={{ maxWidth: 100 }} placeholder="Min rooms"
          type="number" value={draft.minRooms ?? ''}
          onChange={(e) => updateDraft({ minRooms: e.target.value ? +e.target.value : undefined })} />

        <label style={{ fontSize: 12, color: 'var(--text-muted)', whiteSpace: 'nowrap', alignSelf: 'center' }}>
          Available
        </label>
        <input className="input" type="date" style={{ maxWidth: 160 }}
          title="Earliest available from"
          value={draft.availableEarliest ?? ''}
          onChange={(e) => updateDraft({ availableEarliest: e.target.value || undefined })} />
        <span style={{ alignSelf: 'center', color: 'var(--text-muted)', fontSize: 12 }}>–</span>
        <input className="input" type="date" style={{ maxWidth: 160 }}
          title="Latest available from"
          value={draft.availableLatest ?? ''}
          onChange={(e) => updateDraft({ availableLatest: e.target.value || undefined })} />

        <button type="button" className="btn btn-ghost btn-sm" style={{ whiteSpace: 'nowrap' }}
          onClick={() => updateDraft({ availableEarliest: undefined, availableLatest: undefined })}>
          Show all
        </button>

        <button className="btn btn-primary" onClick={applyFilters}>
          Apply
        </button>
      </div>

      {/* Grid */}
      {isLoading && !data ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : !data?.content?.length ? (
        <div className="empty-state">
          <p>No listings match your criteria.</p>
        </div>
      ) : (
        <>
          {isFetching && (
            <div style={{ textAlign: 'center', padding: '8px 0', fontSize: 12, color: 'var(--text-muted)' }}>
              Updating results…
            </div>
          )}
          <div className="listing-grid">
            {data.content.map((l) => <ListingCard key={l.id} listing={l} />)}
          </div>

          {/* Pagination */}
          {data.totalPages > 1 && (
            <div className="flex items-center gap-3 mt-6" style={{ justifyContent: 'center' }}>
              <button className="btn btn-ghost btn-sm"
                disabled={criteria.page === 0}
                onClick={() => setCriteria((c) => ({ ...c, page: (c.page ?? 0) - 1 }))}>
                ← Prev
              </button>
              <span className="text-sm text-muted">
                Page {(criteria.page ?? 0) + 1} / {data.totalPages}
              </span>
              <button className="btn btn-ghost btn-sm"
                disabled={(criteria.page ?? 0) >= data.totalPages - 1}
                onClick={() => setCriteria((c) => ({ ...c, page: (c.page ?? 0) + 1 }))}>
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

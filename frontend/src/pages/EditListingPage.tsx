import { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { listingApi } from '../api/listings';
import { useAuthStore } from '../store/auth';
import type { PropertyType, TransactionType, Photo } from '../types';
import { Save, ChevronLeft, Upload, X, Calendar, Star } from 'lucide-react';

const PROPERTY_TYPES: PropertyType[] = ['APARTMENT', 'HOUSE', 'STUDIO', 'COMMERCIAL', 'LAND'];
const TRANSACTION_TYPES: TransactionType[] = ['RENT', 'SALE'];

function today() {
  return new Date().toISOString().split('T')[0];
}

interface FormData {
  title: string;
  description: string;
  street: string;
  city: string;
  voivodeship: string;
  postalCode: string;
  propertyType: PropertyType;
  transactionType: TransactionType;
  price: string;
  area: string;
  rooms: string;
  availableFrom: string;
}

const emptyForm: FormData = {
  title: '',
  description: '',
  street: '',
  city: '',
  voivodeship: '',
  postalCode: '',
  propertyType: 'APARTMENT',
  transactionType: 'SALE',
  price: '',
  area: '',
  rooms: '',
  availableFrom: '',
};

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="input-label">{label}</label>
      {children}
    </div>
  );
}

export function EditListingPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { userId } = useAuthStore();
  const qc = useQueryClient();
  const [form, setForm] = useState<FormData>(emptyForm);
  const [error, setError] = useState('');
  const [existingPhotos, setExistingPhotos] = useState<Photo[]>([]);
  const [pendingPhotos, setPendingPhotos] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { data: listing, isLoading } = useQuery({
    queryKey: ['listing', id],
    queryFn: () => listingApi.getById(id!),
    enabled: !!id,
  });

  useEffect(() => {
    if (!listing) return;
    setForm({
      title: listing.title ?? '',
      description: listing.description ?? '',
      street: listing.street ?? '',
      city: listing.city ?? '',
      voivodeship: listing.voivodeship ?? '',
      postalCode: listing.postalCode ?? '',
      propertyType: (listing.propertyType as PropertyType) ?? 'APARTMENT',
      transactionType: (listing.transactionType as TransactionType) ?? 'SALE',
      price: listing.price != null ? String(listing.price) : '',
      area: listing.area != null ? String(listing.area) : '',
      rooms: listing.rooms != null ? String(listing.rooms) : '',
      availableFrom: listing.availableFrom ?? '',
    });
    setExistingPhotos(listing.photos ?? []);
  }, [listing]);

  const set = (patch: Partial<FormData>) => setForm((f) => ({ ...f, ...patch }));

  const update = useMutation({
    mutationFn: async () => {
      await listingApi.update(id!, {
        title: form.title,
        description: form.description,
        street: form.street,
        city: form.city,
        voivodeship: form.voivodeship || undefined,
        postalCode: form.postalCode,
        country: 'Poland',
        propertyType: form.propertyType,
        transactionType: form.transactionType,
        price: Number(form.price),
        area: Number(form.area),
        rooms: form.rooms ? Number(form.rooms) : undefined,
        availableFrom: form.availableFrom || undefined,
      });
      for (const file of pendingPhotos) {
        await listingApi.uploadPhoto(id!, file);
      }
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['listing', id] });
      navigate(`/listings/${id}`);
    },
    onError: () => setError('Failed to update listing. Please check your inputs and try again.'),
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (listing && listing.ownerId !== userId) {
      setError('You are not the owner of this listing.');
      return;
    }
    if (existingPhotos.length + pendingPhotos.length === 0) {
      setError('At least one photo is required.');
      return;
    }
    setError('');
    update.mutate();
  };

  if (isLoading) return <div className="loading-center"><div className="spinner" /></div>;
  if (!listing) return <div className="empty-state"><p>Listing not found.</p></div>;

  return (
    <div className="fade-in" style={{ maxWidth: 680, margin: '0 auto' }}>
      <button className="btn btn-ghost btn-sm mb-4" onClick={() => navigate(-1)}>
        <ChevronLeft size={15} /> Back
      </button>

      <div className="section-header">
        <div>
          <h1 className="section-title">Edit Listing</h1>
          <p className="section-subtitle">Update the property details below</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
        {/* Basic info */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <p style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Basic Info
          </p>
          <Field label="Title">
            <input className="input" required placeholder="e.g. Sunny 2-room apartment in city centre"
              value={form.title} onChange={(e) => set({ title: e.target.value })} />
          </Field>
          <Field label="Description">
            <textarea className="input" rows={4} placeholder="Describe the property…"
              style={{ resize: 'vertical' }}
              value={form.description} onChange={(e) => set({ description: e.target.value })} />
          </Field>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Field label="Property Type">
              <select className="filter-select" style={{ width: '100%' }}
                value={form.propertyType}
                onChange={(e) => set({ propertyType: e.target.value as PropertyType })}>
                {PROPERTY_TYPES.map((t) => <option key={t}>{t}</option>)}
              </select>
            </Field>
            <Field label="Transaction Type">
              <select className="filter-select" style={{ width: '100%' }}
                value={form.transactionType}
                onChange={(e) => set({ transactionType: e.target.value as TransactionType })}>
                {TRANSACTION_TYPES.map((t) => <option key={t}>{t}</option>)}
              </select>
            </Field>
          </div>
        </div>

        {/* Financials & Dimensions */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <p style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Financials & Dimensions
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12 }}>
            <Field label="Price (PLN)">
              <input className="input no-spinners" required type="number" min="0" placeholder="e.g. 450000"
                value={form.price} onChange={(e) => set({ price: e.target.value })} />
            </Field>
            <Field label="Area (m²)">
              <input className="input no-spinners" required type="number" min="1" placeholder="e.g. 65"
                value={form.area} onChange={(e) => set({ area: e.target.value })} />
            </Field>
            <Field label="Rooms">
              <input className="input no-spinners" type="number" min="1" placeholder="e.g. 3"
                value={form.rooms} onChange={(e) => set({ rooms: e.target.value })} />
            </Field>
          </div>
          {form.transactionType === 'RENT' && (
            <Field label="Available from">
              <div className="flex items-center gap-2">
                <input className="input" type="date" style={{ maxWidth: 200 }}
                  min={today()}
                  value={form.availableFrom} onChange={(e) => set({ availableFrom: e.target.value })} />
                <button type="button" className="btn btn-ghost btn-sm"
                  onClick={() => set({ availableFrom: today() })}>
                  <Calendar size={14} /> From now
                </button>
              </div>
            </Field>
          )}
        </div>

        {/* Photos */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <p style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Photos ({existingPhotos.length + pendingPhotos.length}/20)
          </p>
          <input type="file" ref={fileInputRef} accept="image/*" multiple hidden
            onChange={(e) => {
              const files = Array.from(e.target.files ?? []);
              setPendingPhotos((p) => [...p, ...files].slice(0, 20 - existingPhotos.length));
              e.target.value = '';
            }} />
          {existingPhotos.length > 0 && (
            <div className="flex gap-2" style={{ flexWrap: 'wrap' }}>
              {existingPhotos.map((p) => (
                <div key={p.id} style={{ position: 'relative', width: 100, height: 75 }}>
                  <img src={p.url} alt=""
                    style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 6,
                      border: p.isCover ? '2px solid var(--accent)' : '2px solid transparent' }} />
                  {p.isCover && (
                    <Star size={12} style={{ position: 'absolute', top: 4, left: 4, color: 'var(--accent)' }} />
                  )}
                  <button type="button"
                    style={{ position: 'absolute', top: -6, right: -6, background: 'var(--danger)',
                      color: '#fff', border: 'none', borderRadius: '50%', width: 20, height: 20,
                      display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
                    onClick={async () => {
                      await listingApi.deletePhoto(id!, p.id);
                      setExistingPhotos((prev) => prev.filter((x) => x.id !== p.id));
                    }}>
                    <X size={12} />
                  </button>
                </div>
              ))}
            </div>
          )}
          {pendingPhotos.length > 0 && (
            <div className="flex gap-2" style={{ flexWrap: 'wrap' }}>
              {pendingPhotos.map((f, i) => (
                <div key={i} style={{ position: 'relative', width: 100, height: 75 }}>
                  <img src={URL.createObjectURL(f)} alt=""
                    style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 6 }} />
                  <button type="button"
                    style={{ position: 'absolute', top: -6, right: -6, background: 'var(--danger)',
                      color: '#fff', border: 'none', borderRadius: '50%', width: 20, height: 20,
                      display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
                    onClick={() => setPendingPhotos((prev) => prev.filter((_, j) => j !== i))}>
                    <X size={12} />
                  </button>
                </div>
              ))}
            </div>
          )}
          <button type="button" className="btn btn-ghost"
            disabled={existingPhotos.length + pendingPhotos.length >= 20}
            onClick={() => fileInputRef.current?.click()}>
            <Upload size={16} /> Add Photos
          </button>
        </div>

        {/* Location */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <p style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Location
          </p>
          <Field label="Street">
            <input className="input" required placeholder="e.g. ul. Marszałkowska 1"
              value={form.street} onChange={(e) => set({ street: e.target.value })} />
          </Field>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Field label="City">
              <input className="input" required placeholder="e.g. Warsaw"
                value={form.city} onChange={(e) => set({ city: e.target.value })} />
            </Field>
            <Field label="Voivodeship">
              <input className="input" placeholder="e.g. Mazowieckie"
                value={form.voivodeship} onChange={(e) => set({ voivodeship: e.target.value })} />
            </Field>
          </div>
          <Field label="Postal Code">
            <input className="input" required placeholder="e.g. 00-001" style={{ maxWidth: 160 }}
              value={form.postalCode} onChange={(e) => set({ postalCode: e.target.value })} />
          </Field>
        </div>

        {error && (
          <p style={{ fontSize: 13, color: 'var(--danger)', background: 'rgba(239,68,68,0.1)',
            padding: '10px 14px', borderRadius: 8 }}>{error}</p>
        )}

        <div className="flex gap-3">
          <button type="submit" className="btn btn-primary" disabled={update.isPending}>
            <Save size={16} />
            {update.isPending ? 'Saving…' : 'Save Changes'}
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

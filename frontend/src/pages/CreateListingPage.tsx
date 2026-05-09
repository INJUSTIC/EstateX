import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { listingApi } from '../api/listings';
import type { PropertyType, TransactionType } from '../types';
import { PlusCircle, ChevronLeft, Upload, X, Calendar } from 'lucide-react';

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

const initial: FormData = {
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

export function CreateListingPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<FormData>(initial);
  const [error, setError] = useState('');
  const [pendingPhotos, setPendingPhotos] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const set = (patch: Partial<FormData>) => setForm((f) => ({ ...f, ...patch }));

  const create = useMutation({
    mutationFn: async () => {
      const listing = await listingApi.create({
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
      // Upload photos after listing creation
      for (const file of pendingPhotos) {
        await listingApi.uploadPhoto(listing.id, file);
      }
      return listing;
    },
    onSuccess: (listing) => navigate(`/listings/${listing.id}`),
    onError: () => setError('Failed to create listing. Please check your inputs and try again.'),
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (pendingPhotos.length === 0) {
      setError('At least one photo is required.');
      return;
    }
    setError('');
    create.mutate();
  };

  const addPhotos = (newFiles: File[]) => {
    setPendingPhotos((prev) => [...prev, ...newFiles].slice(0, 20));
    setError('');
  };

  const removePhoto = (index: number) => {
    setPendingPhotos((prev) => prev.filter((_, i) => i !== index));
  };

  return (
    <div className="fade-in" style={{ maxWidth: 680, margin: '0 auto' }}>
      <button className="btn btn-ghost btn-sm mb-4" onClick={() => navigate(-1)}>
        <ChevronLeft size={15} /> Back
      </button>

      <div className="section-header">
        <div>
          <h1 className="section-title">New Listing</h1>
          <p className="section-subtitle">Fill in the property details below</p>
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
            Photos ({pendingPhotos.length}/20)
          </p>
          <input type="file" ref={fileInputRef} accept="image/*" multiple hidden
            onChange={(e) => {
              const files = Array.from(e.target.files ?? []);
              addPhotos(files);
              e.target.value = '';
            }} />
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
                    onClick={() => removePhoto(i)}>
                    <X size={12} />
                  </button>
                </div>
              ))}
            </div>
          )}
          <button type="button" className="btn btn-ghost"
            disabled={pendingPhotos.length >= 20}
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
          <button type="submit" className="btn btn-primary" disabled={create.isPending}>
            <PlusCircle size={16} />
            {create.isPending ? 'Creating…' : 'Create Listing'}
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

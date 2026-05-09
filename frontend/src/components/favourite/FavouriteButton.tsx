import { useState } from 'react';
import { Heart } from 'lucide-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { favouriteApi } from '../../api/favourites';

interface Props { listingId: string; }

export function FavouriteButton({ listingId }: Props) {
  const qc = useQueryClient();

  const { data: favourites = [] } = useQuery({
    queryKey: ['favourites'],
    queryFn: favouriteApi.getAll,
  });

  const fav = favourites.find((f) => f.listingId === listingId);
  const [optimistic, setOptimistic] = useState<boolean | null>(null);
  const isSaved = optimistic !== null ? optimistic : !!fav;

  const save = useMutation({
    mutationFn: () => favouriteApi.save(listingId),
    onMutate: () => setOptimistic(true),
    onSettled: () => { setOptimistic(null); qc.invalidateQueries({ queryKey: ['favourites'] }); },
  });

  const remove = useMutation({
    mutationFn: () => favouriteApi.remove(listingId),
    onMutate: () => setOptimistic(false),
    onSettled: () => { setOptimistic(null); qc.invalidateQueries({ queryKey: ['favourites'] }); },
  });

  return (
    <button
      className="btn-icon"
      title={isSaved ? 'Remove from favourites' : 'Save to favourites'}
      style={{ color: isSaved ? 'var(--danger)' : undefined }}
      onClick={() => (isSaved && fav ? remove.mutate() : save.mutate())}
    >
      <Heart size={15} fill={isSaved ? 'currentColor' : 'none'} />
    </button>
  );
}

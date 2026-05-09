import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppLayout } from './components/layout/AppLayout';
import { LoginPage } from './pages/LoginPage';
import { BrowsePage } from './pages/BrowsePage';
import { ListingDetailPage } from './pages/ListingDetailPage';
import { FavouritesPage } from './pages/FavouritesPage';
import { InboxPage } from './pages/InboxPage';
import { CreateListingPage } from './pages/CreateListingPage';
import { UserProfilePage } from './pages/UserProfilePage';
import { MyListingsPage } from './pages/MyListingsPage';
import { EditListingPage } from './pages/EditListingPage';
import { PublicUserProfilePage } from './pages/PublicUserProfilePage';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 30_000 } },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<AppLayout />}>
            <Route index element={<BrowsePage />} />
            <Route path="listings/new" element={<CreateListingPage />} />
            <Route path="listings/:id/edit" element={<EditListingPage />} />
            <Route path="listings/:id" element={<ListingDetailPage />} />
            <Route path="favourites" element={<FavouritesPage />} />
            <Route path="inbox" element={<InboxPage />} />
            <Route path="inbox/:id" element={<InboxPage />} />
            <Route path="profile" element={<UserProfilePage />} />
            <Route path="my-listings" element={<MyListingsPage />} />
            <Route path="users/:userId" element={<PublicUserProfilePage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

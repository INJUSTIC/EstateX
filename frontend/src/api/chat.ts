import { api } from '../lib/axios';
import type { Conversation, Message } from '../types';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function transformConversation(raw: any): Conversation {
  return {
    ...raw,
    ownerId: raw.listingOwnerId ?? raw.ownerId,
    listingStatus: raw.listingStatus ?? null,
    initiatorName: raw.initiatorName ?? undefined,
    ownerName: raw.ownerName ?? undefined,
  };
}

export const chatApi = {
  getConversations: (): Promise<Conversation[]> =>
    api.get('/api/conversations').then((r) => (r.data ?? []).map(transformConversation)),

  startConversation: (listingId: string): Promise<Conversation> =>
    api.post('/api/conversations', { listingId }).then((r) => transformConversation(r.data)),

  getMessages: (conversationId: string): Promise<Message[]> =>
    api.get(`/api/conversations/${conversationId}/messages`).then((r) => r.data.items),

  sendMessage: (conversationId: string, content: string): Promise<Message> =>
    api.post(`/api/conversations/${conversationId}/messages`, { content }).then((r) => r.data),
};

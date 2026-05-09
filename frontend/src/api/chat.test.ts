import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../test/mocks/server';
import { chatApi } from './chat';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('chatApi', () => {
  it('should fetch conversations', async () => {
    localStorage.setItem('userId', 'user-1');
    const convs = await chatApi.getConversations();
    expect(convs).toHaveLength(1);
    expect(convs[0].id).toBe('conv-1');
    expect(convs[0].listingTitle).toBe('Sunny Apartment');
    expect(convs[0].unreadCount).toBe(2);
    expect(convs[0].ownerId).toBe('owner-1');
    expect(convs[0].listingStatus).toBe('ACTIVE');
    expect(convs[0].initiatorName).toBe('Alice');
    expect(convs[0].ownerName).toBe('Bob');
  });

  it('should start a conversation', async () => {
    localStorage.setItem('userId', 'user-1');
    const conv = await chatApi.startConversation('listing-1');
    expect(conv.id).toBe('conv-1');
    expect(conv.listingId).toBe('listing-1');
    expect(conv.initiatorId).toBe('user-1');
    expect(conv.ownerId).toBe('owner-1');
  });

  it('should fetch messages for a conversation', async () => {
    localStorage.setItem('userId', 'user-1');
    const messages = await chatApi.getMessages('conv-1');
    expect(Array.isArray(messages)).toBe(true);
    expect(messages).toHaveLength(1);
    expect(messages[0].id).toBe('msg-1');
    expect(messages[0].conversationId).toBe('conv-1');
  });

  it('should send a message', async () => {
    localStorage.setItem('userId', 'user-1');
    const msg = await chatApi.sendMessage('conv-1', 'New message');
    expect(msg.id).toBe('msg-new');
    expect(msg.content).toBe('New message');
    expect(msg.conversationId).toBe('conv-1');
  });

  it('should handle null getConversations response', async () => {
    // given
    server.use(
      http.get('http://localhost:8080/api/conversations', () =>
        HttpResponse.json(null)
      )
    );
    localStorage.setItem('userId', 'user-1');

    // when
    const convs = await chatApi.getConversations();

    // then
    expect(convs).toEqual([]);
  });
});

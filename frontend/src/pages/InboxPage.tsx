import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { chatApi } from '../api/chat';
import { useAuthStore } from '../store/auth';
import type { Message } from '../types';
import { Send, MessageCircle, ExternalLink, AlertTriangle, User } from 'lucide-react';

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
}

export function InboxPage() {
  const { id: activeId } = useParams<{ id?: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { userId } = useAuthStore();
  const qc = useQueryClient();
  const [text, setText] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const isPending = activeId === 'new';
  const pendingListingId = isPending ? searchParams.get('listingId') : null;

  const { data: conversations = [] } = useQuery({
    queryKey: ['conversations'],
    queryFn: chatApi.getConversations,
    refetchInterval: 10_000,
  });

  const messagesQuery = useQuery({
    queryKey: ['messages', activeId],
    queryFn: () => chatApi.getMessages(activeId!),
    enabled: !!activeId && !isPending,
  });
  const messages = messagesQuery.data ?? [];

  // Fix 3: Clear unread badge immediately when messages load
  useEffect(() => {
    if (messagesQuery.isSuccess && activeId && !isPending) {
      qc.invalidateQueries({ queryKey: ['conversations'] });
    }
  }, [messagesQuery.dataUpdatedAt, activeId, isPending, qc]);

  const activeConv = conversations.find((c) => c.id === activeId);

  // Derive companion name from conversation data
  const otherPersonId = activeConv
    ? (activeConv.ownerId === userId ? activeConv.initiatorId : activeConv.ownerId)
    : undefined;
  const otherPersonName = activeConv
    ? (activeConv.ownerId === userId ? activeConv.initiatorName : activeConv.ownerName)
    : undefined;

  // WebSocket real-time subscription (skip when pending new conversation)
  const stompRef = useRef<Client | null>(null);
  useEffect(() => {
    if (!activeId || isPending) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL ?? 'http://localhost:8080'}/ws`),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/conversation.${activeId}`, (frame) => {
          const newMessage: Message = JSON.parse(frame.body);
          qc.setQueryData<Message[]>(['messages', activeId], (prev = []) => {
            if (prev.some((m) => m.id === newMessage.id)) return prev;
            return [...prev, newMessage];
          });
          qc.invalidateQueries({ queryKey: ['conversations'] });
        });
      },
    });

    client.activate();
    stompRef.current = client;

    return () => {
      client.deactivate();
      stompRef.current = null;
    };
  }, [activeId, isPending, qc]);

  // Auto-scroll to newest message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Fix 5: Create conversation on first send if pending
  const send = useMutation({
    mutationFn: async () => {
      if (isPending && pendingListingId) {
        const conv = await chatApi.startConversation(pendingListingId);
        await chatApi.sendMessage(conv.id, text);
        return conv.id;
      }
      await chatApi.sendMessage(activeId!, text);
      return activeId!;
    },
    onSuccess: (convId) => {
      setText('');
      if (isPending) {
        qc.invalidateQueries({ queryKey: ['conversations'] });
        navigate(`/inbox/${convId}`, { replace: true });
      } else {
        qc.invalidateQueries({ queryKey: ['messages', activeId] });
      }
    },
  });

  const chatTitle = isPending
    ? 'New conversation'
    : (otherPersonName ?? activeConv?.listingTitle ?? '…');
  const chatSubtitle = otherPersonName ? activeConv?.listingTitle : undefined;

  const listingUnavailable = activeConv && (activeConv.listingId == null || (activeConv.listingStatus && activeConv.listingStatus !== 'ACTIVE'));

  return (
    <div className="fade-in" style={{ margin: '-28px', height: 'calc(100vh - 60px)', display: 'flex' }}>
      {/* Conversation list */}
      <aside className="chat-sidebar" style={{ width: 280, flexShrink: 0 }}>
        <p style={{ fontSize: 11, fontWeight: 700, color: 'var(--text-muted)',
          textTransform: 'uppercase', letterSpacing: '0.06em', padding: '4px 4px 8px' }}>
          Inbox
        </p>

        {conversations.length === 0 ? (
          <div className="empty-state" style={{ padding: 32 }}>
            <MessageCircle size={32} />
            <p style={{ fontSize: 12 }}>No conversations yet</p>
          </div>
        ) : conversations.map((c) => {
          const name = c.ownerId === userId ? c.initiatorName : c.ownerName;
          const unavailable = c.listingId == null || (c.listingStatus && c.listingStatus !== 'ACTIVE');
          return (
            <div key={c.id} className={`conv-item ${c.id === activeId ? 'active' : ''}`}
              onClick={() => navigate(`/inbox/${c.id}`)}>
              <div className="avatar" style={{ width: 36, height: 36 }}>
                {(name ?? c.listingTitle)[0]}
              </div>
              <div style={{ minWidth: 0 }}>
                <p className="conv-item__title" style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {name ?? c.listingTitle}
                </p>
                <p className="conv-item__sub" style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {unavailable ? '⚠ Listing unavailable' : c.listingTitle}
                </p>
              </div>
              {(c.unreadCount ?? 0) > 0 && <span className="badge">{c.unreadCount}</span>}
            </div>
          );
        })}
      </aside>

      {/* Message area */}
      <div className="chat-window" style={{ flex: 1, borderLeft: '1px solid var(--border)' }}>
        {!activeId ? (
          <div className="empty-state" style={{ height: '100%' }}>
            <MessageCircle size={48} />
            <p>Select a conversation to start chatting.</p>
          </div>
        ) : (
          <>
            {/* Header */}
            <div style={{ padding: '14px 20px', borderBottom: '1px solid var(--border)',
              display: 'flex', alignItems: 'center', gap: 12 }}>
              <div className="avatar">
                {(otherPersonName ?? activeConv?.listingTitle ?? '?')[0]}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontWeight: 600 }}>{chatTitle}</p>
                {chatSubtitle && (
                  <p style={{ fontSize: 12, color: 'var(--text-muted)', overflow: 'hidden',
                    textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {chatSubtitle}
                  </p>
                )}
              </div>
              {/* Profile link */}
              {otherPersonId && (
                <Link to={`/users/${otherPersonId}`}
                  style={{ color: 'var(--text-muted)', display: 'flex', alignItems: 'center',
                    gap: 4, fontSize: 12, textDecoration: 'none', flexShrink: 0 }}>
                  <User size={14} /> Profile
                </Link>
              )}
              {/* Fix 6: Link to listing */}
              {(activeConv?.listingId || pendingListingId) && (
                <Link to={`/listings/${activeConv?.listingId ?? pendingListingId}`}
                  style={{ color: 'var(--text-muted)', display: 'flex', alignItems: 'center',
                    gap: 4, fontSize: 12, textDecoration: 'none', flexShrink: 0 }}>
                  <ExternalLink size={14} /> View listing
                </Link>
              )}
            </div>

            {/* Listing unavailable banner */}
            {listingUnavailable && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 20px',
                background: 'rgba(234,179,8,0.1)', borderBottom: '1px solid var(--border)',
                fontSize: 13, color: '#ca8a04' }}>
                <AlertTriangle size={14} />
                {activeConv?.listingId == null ? 'The listing has been deleted.' : 'The listing is no longer active.'}
              </div>
            )}

            {/* Messages / pending state */}
            {isPending ? (
              <div className="chat-messages" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>
                  Send a message to start the conversation.
                </p>
              </div>
            ) : (
              <div className="chat-messages">
                {messages.map((m) => (
                  <div key={m.id} className={`message-bubble ${m.senderId === userId ? 'mine' : 'theirs'}`}>
                    {m.content}
                    <div className="message-time">{formatTime(m.sentAt)}</div>
                  </div>
                ))}
                <div ref={messagesEndRef} />
              </div>
            )}

            {/* Input */}
            <div className="chat-input-bar">
              <input className="input" placeholder="Type a message…"
                value={text} onChange={(e) => setText(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); if (text.trim()) send.mutate(); } }} />
              <button className="btn btn-primary" onClick={() => text.trim() && send.mutate()}
                disabled={!text.trim() || send.isPending}>
                <Send size={16} />
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

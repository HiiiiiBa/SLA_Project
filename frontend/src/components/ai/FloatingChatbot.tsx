"use client";

import { FormEvent, useEffect, useRef, useState } from "react";
import { Bot, Loader2, MessageCircle, Send, Sparkles, X } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { ChatMessageContent } from "@/components/ai/ChatMessageContent";
import { ApiError, apiFetch } from "@/lib/api";
import { cn } from "@/lib/utils";
import type { AiChatMessage, AiChatResponse } from "@/types";

export function FloatingChatbot() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<AiChatMessage[]>([
    {
      role: "assistant",
      content:
        "Bonjour ! Je suis l'assistant SLA Monitor. Posez-moi des questions sur vos SLA, incidents, alertes, projets et services.",
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (open) {
      messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages, open, loading]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const trimmed = input.trim();
    if (!trimmed || loading) return;

    const userMessage: AiChatMessage = { role: "user", content: trimmed };
    const nextMessages = [...messages, userMessage];
    setMessages(nextMessages);
    setInput("");
    setLoading(true);
    setError(null);

    try {
      const history = nextMessages
        .filter((message) => message.role === "user" || message.role === "assistant")
        .slice(-10);
      const response = await apiFetch<AiChatResponse>("/api/ai/chat", {
        method: "POST",
        body: JSON.stringify({
          message: trimmed,
          history: history.slice(0, -1),
        }),
      });
      setMessages((current) => [
        ...current,
        { role: "assistant", content: response.reply },
      ]);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Impossible d'obtenir une réponse");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-3">
      {open && (
        <div className="flex h-[min(70vh,560px)] w-[min(92vw,380px)] flex-col overflow-hidden rounded-2xl border border-border bg-card shadow-2xl shadow-primary/10">
          <div className="flex items-center justify-between border-b border-border bg-gradient-to-r from-primary/10 to-accent/10 px-4 py-3">
            <div className="flex items-center gap-2">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-accent text-white">
                <Bot className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm font-semibold text-heading">Assistant SLA</p>
                <p className="text-xs text-muted">Propulsé par Gemini</p>
              </div>
            </div>
            <button
              type="button"
              onClick={() => setOpen(false)}
              className="rounded-lg p-2 text-muted transition hover:bg-card hover:text-foreground"
              aria-label="Fermer le chatbot"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          <div className="scroll-area flex-1 space-y-3 overflow-y-auto bg-card px-4 py-4">
            {messages.map((message, index) => (
              <div
                key={`${message.role}-${index}`}
                className={cn(
                  "max-w-[90%] rounded-2xl px-4 py-3",
                  message.role === "user"
                    ? "ml-auto bg-primary text-sm leading-relaxed text-white whitespace-pre-wrap"
                    : "bg-card/80 text-body ring-1 ring-border/60",
                )}
              >
                {message.role === "user" ? (
                  message.content
                ) : (
                  <ChatMessageContent content={message.content} />
                )}
              </div>
            ))}
            {loading && (
              <div className="inline-flex items-center gap-2 rounded-2xl bg-card/80 px-4 py-3 text-sm text-muted ring-1 ring-border/60">
                <Loader2 className="h-4 w-4 animate-spin" />
                Analyse en cours...
              </div>
            )}
            {error && (
              <div className="rounded-xl border border-error/30 bg-error/10 px-3 py-2 text-xs text-error">
                {error}
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <form onSubmit={handleSubmit} className="border-t border-border p-3">
            <div className="flex items-end gap-2">
              <textarea
                value={input}
                onChange={(event) => setInput(event.target.value)}
                rows={2}
                placeholder="Posez une question sur vos données SLA..."
                className="min-h-[44px] flex-1 resize-none rounded-xl border border-border bg-background px-3 py-2 text-sm text-body outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/20"
              />
              <Button type="submit" loading={loading} className="!px-3 !py-3" disabled={!input.trim()}>
                <Send className="h-4 w-4" />
              </Button>
            </div>
          </form>
        </div>
      )}

      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        className={cn(
          "group flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-primary to-accent text-white shadow-xl shadow-primary/30 transition hover:scale-105",
          open && "rotate-0",
        )}
        aria-label={open ? "Fermer l'assistant IA" : "Ouvrir l'assistant IA"}
      >
        {open ? <X className="h-6 w-6" /> : <MessageCircle className="h-6 w-6" />}
        {!open && (
          <Sparkles className="absolute -right-1 -top-1 h-4 w-4 text-yellow-300" />
        )}
      </button>
    </div>
  );
}

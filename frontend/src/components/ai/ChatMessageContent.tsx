"use client";

import { Fragment } from "react";

function formatInline(text: string) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g);
  return parts.map((part, index) => {
    if (part.startsWith("**") && part.endsWith("**")) {
      return (
        <strong key={index} className="font-semibold text-heading">
          {part.slice(2, -2)}
        </strong>
      );
    }
    return <Fragment key={index}>{part}</Fragment>;
  });
}

interface ChatMessageContentProps {
  content: string;
}

export function ChatMessageContent({ content }: ChatMessageContentProps) {
  const lines = content.split("\n");

  return (
    <div className="space-y-1.5 text-sm leading-relaxed">
      {lines.map((line, index) => {
        const trimmed = line.trim();

        if (!trimmed) {
          return <div key={index} className="h-1" />;
        }

        const listMatch = trimmed.match(/^([-*•]|\d+\.)\s+(.*)$/);
        if (listMatch) {
          return (
            <div key={index} className="flex gap-2">
              <span className="mt-0.5 shrink-0 text-primary">•</span>
              <span className="min-w-0 break-words">{formatInline(listMatch[2])}</span>
            </div>
          );
        }

        return (
          <p key={index} className="break-words">
            {formatInline(trimmed)}
          </p>
        );
      })}
    </div>
  );
}

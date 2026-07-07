"use client";

import { AlertTriangle, Brain, Lightbulb, Target, TrendingUp } from "lucide-react";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { cn } from "@/lib/utils";
import type { IncidentAnalysis } from "@/types";

interface IncidentAnalysisCardProps {
  analysis: IncidentAnalysis;
  className?: string;
}

const priorityStyles: Record<string, string> = {
  Low: "bg-success/15 text-success border-success/30",
  Medium: "bg-warning/15 text-warning border-warning/30",
  High: "bg-orange-500/15 text-orange-600 border-orange-500/30 dark:text-orange-400",
  Critical: "bg-error/15 text-error border-error/30",
};

export function IncidentAnalysisCard({ analysis, className }: IncidentAnalysisCardProps) {
  const priorityKey = analysis.estimatedPriority ?? "Medium";
  const priorityClass = priorityStyles[priorityKey] ?? priorityStyles.Medium;

  return (
    <Card className={cn("border-primary/20 bg-gradient-to-br from-primary/5 via-card to-accent/5", className)}>
      <CardHeader
        title="Analyse IA"
        description="Générée par Google Gemini à partir des données de l'incident"
      />
      <CardBody className="space-y-5">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-accent text-white shadow-lg shadow-primary/20">
            <Brain className="h-5 w-5" />
          </div>
          <span
            className={cn(
              "rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-wider",
              priorityClass,
            )}
          >
            Priorité estimée : {priorityKey}
          </span>
        </div>

        <section className="space-y-2">
          <div className="flex items-center gap-2 text-sm font-semibold text-heading">
            <Target className="h-4 w-4 text-primary" />
            Résumé
          </div>
          <p className="text-sm leading-relaxed text-body">{analysis.summary}</p>
        </section>

        <div className="grid gap-4 md:grid-cols-2">
          <section className="space-y-2 rounded-xl border border-border/60 bg-card/60 p-4">
            <div className="flex items-center gap-2 text-sm font-semibold text-heading">
              <AlertTriangle className="h-4 w-4 text-warning" />
              Cause probable
            </div>
            <p className="text-sm leading-relaxed text-body">{analysis.probableCause}</p>
          </section>

          <section className="space-y-2 rounded-xl border border-border/60 bg-card/60 p-4">
            <div className="flex items-center gap-2 text-sm font-semibold text-heading">
              <TrendingUp className="h-4 w-4 text-accent" />
              Impact métier
            </div>
            <p className="text-sm leading-relaxed text-body">{analysis.businessImpact}</p>
          </section>
        </div>

        <section className="space-y-3">
          <div className="flex items-center gap-2 text-sm font-semibold text-heading">
            <Lightbulb className="h-4 w-4 text-primary" />
            Étapes de résolution recommandées
          </div>
          <ol className="space-y-2">
            {analysis.recommendedSteps.map((step, index) => (
              <li
                key={`${index}-${step.slice(0, 24)}`}
                className="flex gap-3 rounded-xl border border-border/50 bg-card/50 px-4 py-3 text-sm text-body"
              >
                <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary">
                  {index + 1}
                </span>
                <span>{step}</span>
              </li>
            ))}
          </ol>
        </section>
      </CardBody>
    </Card>
  );
}

import { describe, expect, it } from "vitest";
import { formatPercent, formatScore, formatDate } from "@/lib/utils";
import { toApiDateTime, toInputDateTime } from "@/lib/datetime";

describe("utils", () => {
  it("formatPercent adds percent sign with 2 decimals", () => {
    expect(formatPercent(99.5)).toBe("99.50%");
  });

  it("formatScore keeps 2 decimals", () => {
    expect(formatScore(12.345)).toBe("12.35");
  });

  it("formatDate returns dash for empty value", () => {
    expect(formatDate(null)).toBe("—");
    expect(formatDate(undefined)).toBe("—");
  });
});

describe("datetime", () => {
  it("toApiDateTime appends seconds when missing", () => {
    expect(toApiDateTime("2026-07-14T10:30")).toBe("2026-07-14T10:30:00");
  });

  it("toApiDateTime leaves full datetime unchanged", () => {
    expect(toApiDateTime("2026-07-14T10:30:00")).toBe("2026-07-14T10:30:00");
  });

  it("toInputDateTime truncates to minutes", () => {
    expect(toInputDateTime("2026-07-14T10:30:45")).toBe("2026-07-14T10:30");
  });

  it("toInputDateTime returns empty string for null", () => {
    expect(toInputDateTime(null)).toBe("");
  });
});

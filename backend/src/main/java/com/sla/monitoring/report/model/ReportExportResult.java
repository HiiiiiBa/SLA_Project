package com.sla.monitoring.report.model;

/**
 * Binary export payload returned to HTTP clients.
 */
public record ReportExportResult(byte[] content, String filename, String contentType) {
}

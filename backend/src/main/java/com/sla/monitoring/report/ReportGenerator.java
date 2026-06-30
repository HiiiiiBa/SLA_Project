package com.sla.monitoring.report;

import com.sla.monitoring.report.model.ReportExportData;
import com.sla.monitoring.report.model.ReportExportResult;

/**
 * Generates a report export in a specific format.
 */
public interface ReportGenerator {

    ReportExportResult generate(ReportExportData data);
}

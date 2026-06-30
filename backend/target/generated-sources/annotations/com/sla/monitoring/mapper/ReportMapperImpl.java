package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.ReportCreateRequest;
import com.sla.monitoring.dto.response.ReportResponse;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.Sla;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T12:04:43+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class ReportMapperImpl implements ReportMapper {

    @Override
    public ReportResponse toResponse(Report report) {
        if ( report == null ) {
            return null;
        }

        ReportResponse.ReportResponseBuilder reportResponse = ReportResponse.builder();

        reportResponse.slaId( reportSlaId( report ) );
        reportResponse.id( report.getId() );
        reportResponse.slaResult( report.getSlaResult() );
        reportResponse.periodStart( report.getPeriodStart() );
        reportResponse.periodEnd( report.getPeriodEnd() );
        reportResponse.generatedAt( report.getGeneratedAt() );
        reportResponse.format( report.getFormat() );
        reportResponse.createdAt( report.getCreatedAt() );
        reportResponse.updatedAt( report.getUpdatedAt() );

        return reportResponse.build();
    }

    @Override
    public Report toEntity(ReportCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Report.ReportBuilder report = Report.builder();

        report.slaResult( request.getSlaResult() );
        report.periodStart( request.getPeriodStart() );
        report.periodEnd( request.getPeriodEnd() );
        report.format( request.getFormat() );

        return report.build();
    }

    private Long reportSlaId(Report report) {
        Sla sla = report.getSla();
        if ( sla == null ) {
            return null;
        }
        return sla.getId();
    }
}

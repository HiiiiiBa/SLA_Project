package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.response.IncidentCommentResponse;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.IncidentComment;
import com.sla.monitoring.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-06T09:53:29+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class IncidentCommentMapperImpl implements IncidentCommentMapper {

    @Override
    public IncidentCommentResponse toResponse(IncidentComment comment) {
        if ( comment == null ) {
            return null;
        }

        IncidentCommentResponse.IncidentCommentResponseBuilder incidentCommentResponse = IncidentCommentResponse.builder();

        incidentCommentResponse.incidentId( commentIncidentId( comment ) );
        incidentCommentResponse.authorId( commentAuthorId( comment ) );
        incidentCommentResponse.id( comment.getId() );
        incidentCommentResponse.content( comment.getContent() );
        incidentCommentResponse.createdAt( comment.getCreatedAt() );

        incidentCommentResponse.authorName( formatAuthorName(comment.getAuthor()) );

        return incidentCommentResponse.build();
    }

    private Long commentIncidentId(IncidentComment incidentComment) {
        Incident incident = incidentComment.getIncident();
        if ( incident == null ) {
            return null;
        }
        return incident.getId();
    }

    private Long commentAuthorId(IncidentComment incidentComment) {
        User author = incidentComment.getAuthor();
        if ( author == null ) {
            return null;
        }
        return author.getId();
    }
}

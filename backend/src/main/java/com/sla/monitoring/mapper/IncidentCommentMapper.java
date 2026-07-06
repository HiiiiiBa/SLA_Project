package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.response.IncidentCommentResponse;
import com.sla.monitoring.entity.IncidentComment;
import com.sla.monitoring.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidentCommentMapper {

    @Mapping(target = "incidentId", source = "incident.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", expression = "java(formatAuthorName(comment.getAuthor()))")
    IncidentCommentResponse toResponse(IncidentComment comment);

    default String formatAuthorName(User author) {
        if (author == null) {
            return null;
        }
        return author.getFirstName() + " " + author.getLastName();
    }
}

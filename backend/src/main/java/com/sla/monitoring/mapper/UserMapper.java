package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.UserCreateRequest;
import com.sla.monitoring.dto.request.UserUpdateRequest;
import com.sla.monitoring.dto.response.UserResponse;
import com.sla.monitoring.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    User toEntity(UserCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);
}

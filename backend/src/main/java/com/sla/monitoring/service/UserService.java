package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.UserCreateRequest;
import com.sla.monitoring.dto.request.UserUpdateRequest;
import com.sla.monitoring.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    UserResponse findById(Long id);

    List<UserResponse> findAll();

    UserResponse findByEmail(String email);
}

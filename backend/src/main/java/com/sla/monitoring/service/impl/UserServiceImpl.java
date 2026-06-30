package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.UserCreateRequest;
import com.sla.monitoring.dto.request.UserUpdateRequest;
import com.sla.monitoring.dto.response.UserResponse;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.DuplicateResourceException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.UserMapper;
import com.sla.monitoring.repository.UserRepository;
import com.sla.monitoring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (request.getRole() == null) {
            throw new BusinessException("Role is required");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserEntityById(id);

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (request.getRole() == null) {
            throw new BusinessException("Role is required");
        }

        userMapper.updateEntity(request, user);
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserEntityById(id);
        userRepository.delete(user);
    }

    @Override
    public UserResponse findById(Long id) {
        return userMapper.toResponse(findUserEntityById(id));
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = findUserEntityById(id);
        user.setEnabled(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = findUserEntityById(id);
        user.setEnabled(false);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse resetPassword(Long id, String password) {
        if (!StringUtils.hasText(password) || password.length() < 8) {
            throw new BusinessException("Password must be at least 8 characters");
        }
        User user = findUserEntityById(id);
        user.setPassword(passwordEncoder.encode(password));
        return userMapper.toResponse(userRepository.save(user));
    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}

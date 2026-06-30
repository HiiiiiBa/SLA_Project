package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.response.NotificationResponse;
import com.sla.monitoring.entity.enums.NotificationChannel;
import com.sla.monitoring.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "Notification history (WebSocket and email)")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List notification history with optional channel filter")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> findAll(
            @RequestParam(required = false) NotificationChannel channel) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.findAll(channel)));
    }
}

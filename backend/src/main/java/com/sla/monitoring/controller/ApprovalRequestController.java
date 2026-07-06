package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.ApprovalRequestCreateRequest;
import com.sla.monitoring.dto.request.ApprovalReviewRequest;
import com.sla.monitoring.dto.response.ApprovalRequestResponse;
import com.sla.monitoring.service.ApprovalRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/approval-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Approval Requests", description = "Manager action requests requiring admin validation")
public class ApprovalRequestController {

    private final ApprovalRequestService approvalRequestService;

    @PostMapping
    @Operation(summary = "Submit an action for admin approval (manager only)")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> submit(
            @Valid @RequestBody ApprovalRequestCreateRequest request) {
        ApprovalRequestResponse response = approvalRequestService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Demande soumise à validation admin", response));
    }

    @GetMapping
    @Operation(summary = "List approval requests (pending for admin, history for manager)")
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> findAll(
            @RequestParam(required = false) String scope) {
        if ("pending".equalsIgnoreCase(scope)) {
            return ResponseEntity.ok(ApiResponse.success(approvalRequestService.findPending()));
        }
        if ("mine".equalsIgnoreCase(scope)) {
            return ResponseEntity.ok(ApiResponse.success(approvalRequestService.findMine()));
        }
        return ResponseEntity.ok(ApiResponse.success(approvalRequestService.findPending()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get approval request by ID")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(approvalRequestService.findById(id)));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve and execute a pending request (admin only)")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalReviewRequest review) {
        ApprovalRequestResponse response = approvalRequestService.approve(id, review);
        return ResponseEntity.ok(ApiResponse.success("Demande approuvée et exécutée", response));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a pending request (admin only)")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalReviewRequest review) {
        ApprovalRequestResponse response = approvalRequestService.reject(id, review);
        return ResponseEntity.ok(ApiResponse.success("Demande refusée", response));
    }
}

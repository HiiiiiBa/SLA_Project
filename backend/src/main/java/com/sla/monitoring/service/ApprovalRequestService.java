package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.ApprovalRequestCreateRequest;
import com.sla.monitoring.dto.request.ApprovalReviewRequest;
import com.sla.monitoring.dto.response.ApprovalRequestResponse;

import java.util.List;

public interface ApprovalRequestService {

    ApprovalRequestResponse submit(ApprovalRequestCreateRequest request);

    ApprovalRequestResponse approve(Long id, ApprovalReviewRequest review);

    ApprovalRequestResponse reject(Long id, ApprovalReviewRequest review);

    List<ApprovalRequestResponse> findPending();

    List<ApprovalRequestResponse> findMine();

    ApprovalRequestResponse findById(Long id);
}

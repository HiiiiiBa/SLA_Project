package com.sla.monitoring.notification;

import com.sla.monitoring.entity.ApprovalRequest;

public interface ApprovalNotificationService {

    void notifyAdminsOfSubmission(ApprovalRequest request);

    void notifyRequesterApproved(ApprovalRequest request);

    void notifyRequesterRejected(ApprovalRequest request);
}

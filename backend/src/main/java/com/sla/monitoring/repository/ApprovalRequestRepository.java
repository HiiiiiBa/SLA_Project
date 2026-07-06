package com.sla.monitoring.repository;

import com.sla.monitoring.entity.ApprovalRequest;
import com.sla.monitoring.entity.enums.ApprovalActionType;
import com.sla.monitoring.entity.enums.ApprovalRequestStatus;
import com.sla.monitoring.entity.enums.ApprovalTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    @Query("""
            SELECT ar FROM ApprovalRequest ar
            JOIN FETCH ar.requester
            LEFT JOIN FETCH ar.reviewer
            WHERE ar.status = :status
            ORDER BY ar.createdAt DESC
            """)
    List<ApprovalRequest> findByStatusWithUsers(@Param("status") ApprovalRequestStatus status);

    @Query("""
            SELECT ar FROM ApprovalRequest ar
            JOIN FETCH ar.requester
            LEFT JOIN FETCH ar.reviewer
            WHERE ar.requester.id = :requesterId
            ORDER BY ar.createdAt DESC
            """)
    List<ApprovalRequest> findByRequesterIdWithUsers(@Param("requesterId") Long requesterId);

    @Query("""
            SELECT ar FROM ApprovalRequest ar
            JOIN FETCH ar.requester
            LEFT JOIN FETCH ar.reviewer
            WHERE ar.id = :id
            """)
    Optional<ApprovalRequest> findByIdWithUsers(@Param("id") Long id);

    boolean existsByActionTypeAndTargetTypeAndTargetIdAndStatus(
            ApprovalActionType actionType,
            ApprovalTargetType targetType,
            Long targetId,
            ApprovalRequestStatus status);
}

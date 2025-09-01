package timdev.timdev.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Request;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByCreatedBy(User user);
    List<Request> findByStatus(ApprovalStatus status);
    List<Request> findByCreatedByAndStatus(User user, ApprovalStatus status);
    List<Request> findByApprovalLevel(ApprovalLevel approvalLevel);

    List<Request> findByStatusAndApprovalLevel(ApprovalStatus status, ApprovalLevel approvalLevel);
    // by user approval level 
    List<Request> findByStatusAndCurrentLevel(ApprovalStatus status, ApprovalLevel approvalLevel);

    List<Request> findByStatusInAndApprovalLevel(List<ApprovalStatus> statuses, ApprovalLevel approvalLevel);
    List<Request> findByStatusInAndCurrentLevel(List<ApprovalStatus> statuses, ApprovalLevel approvalLevel);

    List<Request> findAllByStatus(ApprovalStatus status);

    // Optional: convenience methods
    default List<Request> findAllPendingRequests() {
        return findAllByStatus(ApprovalStatus.PENDING);
    }

    default List<Request> findAllApprovedRequests() {
        return findAllByStatus(ApprovalStatus.APPROVED);
    }

    default List<Request> findAllCancelledRequests() {
        return findAllByStatus(ApprovalStatus.CANCELLED);
    }

    default List<Request> findAllRejectedRequests() {
        return findAllByStatus(ApprovalStatus.REJECTED);
    }

    
}
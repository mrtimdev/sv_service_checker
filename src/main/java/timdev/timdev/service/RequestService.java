package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import timdev.timdev.entity.Request;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;

public interface RequestService {
    Request createRequest(Request request, User createdBy);
    Request updateRequest(Request request);
    Request save(Request request);
    void deleteRequest(Long id);
    Optional<Request> findById(Long id);
    List<Request> findAllRequests();
    List<Request> findAllPendingRequests();
    List<Request> findAllApprovedRequests();
    List<Request> findAllCancelledRequests();
    List<Request> findAllRejectedRequests();
    List<Request> findByUser(User user);
    List<Request> findByStatus(ApprovalStatus status);
    List<Request> findByUserAndStatus(User user, ApprovalStatus status);
    Request changeStatus(Long requestId, ApprovalStatus status);
    List<Request> findByIds(List<Long> ids);
    List<Request> findByStatusAndApprovalLevel(ApprovalStatus status, ApprovalLevel approvalLevel);
    List<Request> findByStatusAndCurrentLevel(ApprovalStatus status, ApprovalLevel approvalLevel);
    List<Request> findByStatusInAndApprovalLevel(List<ApprovalStatus> statuses, ApprovalLevel approvalLevel);
    List<Request> findByStatusInAndCurrentLevel(List<ApprovalStatus> statuses, ApprovalLevel approvalLevel);
}

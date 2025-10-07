package timdev.timdev.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Approval;
import timdev.timdev.entity.Request;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;


@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByRequest(Request request);
    List<Approval> findByRequestId(Long requestId);
    List<Approval> findByApprover(User approver);
    List<Approval> findByApproverAndStatus(User approver, ApprovalStatus status);
    Optional<Approval> findByRequestAndLevel(Request request, ApprovalLevel level);

    boolean existsByRequestIdAndApproverId(Long requestId, Long approverId);
}
package timdev.timdev.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.entity.Approval;
import timdev.timdev.entity.Request;
import timdev.timdev.entity.Setting;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.enums.ApproveOrRejectRequestDTO;
import timdev.timdev.repository.ApprovalRepository;
import timdev.timdev.repository.SettingRepository;

@Service
public class ApprovalService {

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserService userService;
    @Autowired
    private SettingRepository settingRepo;



    public Approval createApproval(Request request, ApprovalLevel level, User approver) {
        Approval approval = new Approval();
        approval.setRequest(request);
        approval.setLevel(level);
        approval.setApprover(approver);
        approval.setStatus(ApprovalStatus.PENDING);
        return approvalRepository.save(approval);
    }


    public Approval updateApproval(Approval approval) {
        return approvalRepository.save(approval);
    }


    public Optional<Approval> findById(Long id) {
        return approvalRepository.findById(id);
    }

    public List<Approval> findByRequest(Request request) {
        return approvalRepository.findByRequest(request);
    }

  
    public List<Approval> findByApprover(User approver) {
        return approvalRepository.findByApprover(approver);
    }

 
    public Approval processApproval(Long approvalId, ApprovalStatus status, String comments) {
        Optional<Approval> approvalOpt = approvalRepository.findById(approvalId);
        if (approvalOpt.isPresent()) {
            Approval approval = approvalOpt.get();
            approval.setStatus(status);
            approval.setComments(comments);
            approval.setApprovalDate(LocalDateTime.now());

            Approval savedApproval = approvalRepository.save(approval);

            // Check if all approvals are done and update request status accordingly
            Request request = approval.getRequest();
            if (isRequestFullyApproved(request)) {
                request.setStatus(ApprovalStatus.APPROVED);
                requestService.updateRequest(request);
            } else if (status == ApprovalStatus.REJECTED) {
                request.setStatus(ApprovalStatus.REJECTED);
                requestService.updateRequest(request);
            }

            return savedApproval;
        }
        return null;
    }

    public boolean isRequestFullyApproved(Request request) {
        List<Approval> approvals = approvalRepository.findByRequest(request);
        return approvals.stream()
                .allMatch(approval -> approval.getStatus() == ApprovalStatus.APPROVED);
    }


    public List<Approval> findByRequestId(Long requestId) {
        return approvalRepository.findByRequestId(requestId);
    }




    public Approval approveOrRejectRequest(ApproveOrRejectRequestDTO dto, User user) {
        Request request = requestService.findById(dto.getRequestId()).orElse(null);
        ApprovalLevel requestLevel = request.getApprovalLevel();


        User approver = userService.findById(dto.getApproverId()).orElse(null);
        boolean alreadyApproved = approvalRepository.existsByRequestIdAndApproverId(request.getId(), approver.getId());
        if (alreadyApproved) {
            throw new RuntimeException("This user has already approved/rejected this request");
        }

        if(request.getCreatedBy().getId() == user.getId()) {
            throw new RuntimeException("Oop!, You can not approved/rejected your own request");
        }

        if (requestLevel == null) {
            throw new RuntimeException("Approval level not set for this request");
        }

          // ✅ Load system setting
        Setting setting = settingRepo.findById(1L)
                .orElseThrow(() -> new RuntimeException("Approval setting not found"));
        ApprovalLevel configuredLevel = setting.getApprovedLevel();

        if(!ApprovalStatus.REJECTED.equals(dto.getStatus())) {
            switch (requestLevel) {
                case LEVEL_0 -> request.setApprovalLevel(ApprovalLevel.LEVEL_1);
                case LEVEL_1 -> request.setApprovalLevel(ApprovalLevel.LEVEL_2);
                case LEVEL_2 -> request.setApprovalLevel(ApprovalLevel.LEVEL_3);
                case LEVEL_3 -> {
                    request.setStatus(ApprovalStatus.APPROVED);
                }
                default -> throw new RuntimeException("Unknown approval level: " + requestLevel);
            }

            // ✅ Check configured approvedLevel
            if (request.getApprovalLevel().equals(configuredLevel)) {
                request.setStatus(ApprovalStatus.APPROVED);
            } else if (request.getStatus() != ApprovalStatus.APPROVED) {
                request.setStatus(ApprovalStatus.INREVIEW);
            }

            switch (request.getApprovalLevel()) {
                case LEVEL_1 -> request.setCurrentLevel(ApprovalLevel.LEVEL_2);
                case LEVEL_2 -> request.setCurrentLevel(ApprovalLevel.LEVEL_3);
                case LEVEL_3 -> request.setCurrentLevel(ApprovalLevel.LEVEL_3);
                default -> throw new RuntimeException("Unknown approval level: " + requestLevel);
            }
        } else {
            request.setStatus(ApprovalStatus.REJECTED);
        }
        if(ApprovalLevel.LEVEL_3.equals(request.getApprovalLevel())) {
            request.setStatus(ApprovalStatus.APPROVED);
        }
        
        requestService.save(request);

        

        Approval approval = new Approval();
        approval.setRequest(request);
        approval.setApprover(approver);
        approval.setLevel(request.getApprovalLevel());
        approval.setStatus(dto.getStatus());
        approval.setComments(dto.getComments());
        approval.setApprovalDate(LocalDateTime.now());
        return approvalRepository.save(approval);
    }


    @Transactional
    public void deleteById(Long id) {
        if (approvalRepository.existsById(id)) {
            approvalRepository.deleteById(id);
        } else {
            throw new RuntimeException("Approval with ID " + id + " not found");
        }
    }

    @Transactional
    public void deleteAndCheckRequestLevel(Long id) {
        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval with ID " + id + " not found"));

        Request request = approval.getRequest();

        if (approval.getLevel() != request.getApprovalLevel() && request.getStatus() == ApprovalStatus.APPROVED) {
            throw new RuntimeException(
                "Cannot delete approval: request is already at LEVEL_3 or approved"
            );
        }

        // Safe to delete
        approvalRepository.deleteById(id);

        // Update request's approval level to the previous level
        switch (request.getApprovalLevel()) {
            case LEVEL_1 -> request.setApprovalLevel(ApprovalLevel.LEVEL_0);
            case LEVEL_2 -> request.setApprovalLevel(ApprovalLevel.LEVEL_1);
            case LEVEL_3 -> request.setApprovalLevel(ApprovalLevel.LEVEL_2); // Optional safety, though deletion not allowed at LEVEL_3
            default -> request.setApprovalLevel(ApprovalLevel.LEVEL_0);
        }

        switch (request.getApprovalLevel()) {
            case LEVEL_1 -> request.setCurrentLevel(ApprovalLevel.LEVEL_2);
            case LEVEL_2 -> request.setCurrentLevel(ApprovalLevel.LEVEL_3);
            case LEVEL_3 -> request.setCurrentLevel(ApprovalLevel.LEVEL_3);
            default -> request.setCurrentLevel(request.getCurrentLevel());
        }

        // Optionally, update request status if needed
        if (request.getApprovalLevel() != ApprovalLevel.LEVEL_3 && request.getStatus() == ApprovalStatus.APPROVED) {
            request.setStatus(ApprovalStatus.INREVIEW);
        }

        requestService.save(request);
    }

}

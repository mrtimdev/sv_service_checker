package timdev.timdev.dto;

import java.time.LocalDateTime;

import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;

public class ApprovalDTO {
    private Long id;
    private Long requestId;
    private String requestTitle;
    private ApprovalLevel level;
    private ApprovalStatus status;
    private Long approverId;
    private String approverName;
    private String comments;
    private LocalDateTime approvalDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getRequestTitle() { return requestTitle; }
    public void setRequestTitle(String requestTitle) { this.requestTitle = requestTitle; }

    public ApprovalLevel getLevel() { return level; }
    public void setLevel(ApprovalLevel level) { this.level = level; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }
}

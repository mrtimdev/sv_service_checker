package timdev.timdev.enums;

public class ApproveOrRejectRequestDTO {
    
    private Long requestId;
    private ApprovalStatus status;  
    private String comments;
    private Long approverId;       
    private ApprovalLevel level;
    
    public Long getRequestId() {
        return requestId;
    }
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    public ApprovalStatus getStatus() {
        return status;
    }
    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }
    public Long getApproverId() {
        return approverId;
    }
    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }
    public ApprovalLevel getLevel() {
        return level;
    }
    public void setLevel(ApprovalLevel level) {
        this.level = level;
    } 
}

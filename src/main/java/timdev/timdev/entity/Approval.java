package timdev.timdev.entity;


import jakarta.persistence.*;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "approvals")
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @Enumerated(EnumType.STRING)
    private ApprovalLevel level;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver;

    private String comments;
    private LocalDateTime approvalDate;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Request getRequest() {
        return request;
    }
    public void setRequest(Request request) {
        this.request = request;
    }
    public ApprovalLevel getLevel() {
        return level;
    }
    public void setLevel(ApprovalLevel level) {
        this.level = level;
    }
    public ApprovalStatus getStatus() {
        return status;
    }
    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
    public User getApprover() {
        return approver;
    }
    public void setApprover(User approver) {
        this.approver = approver;
    }
    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }
    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    // Constructors, Getters and Setters
    // ...
}
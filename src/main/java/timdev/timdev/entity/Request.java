package timdev.timdev.entity;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;

@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The Request Title is required")
    @Column(nullable = false)  
    private String title;
    
    @NotNull(message = "The Request Date is required")
    @Column(nullable = false)  
    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate date;
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    private String attachment;

    public String getAttachment() {
    return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    @NotBlank(message = "The Request By is required")
    private String requestBy;
    public String getRequestBy() {
        return requestBy;
    }

    public void setRequestBy(String requestBy) {
        this.requestBy = requestBy;
    }

    private String description;
    private String items;
    
    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private String urgencyLevel;

    @Enumerated(EnumType.STRING)
    private ApprovalLevel approvalLevel;

    // this filed is for approver by setting level 
    @Enumerated(EnumType.STRING)
    private ApprovalLevel currentLevel;
   

    public ApprovalLevel getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(ApprovalLevel currentLevel) {
        this.currentLevel = currentLevel;
    }

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<Approval> approvals;


    public ApprovalLevel getApprovalLevel() {
        return approvalLevel;
    }

    public void setApprovalLevel(ApprovalLevel approvalLevel) {
        this.approvalLevel = approvalLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public List<Approval> getApprovals() {
        return approvals;
    }

    public void setApprovals(List<Approval> approvals) {
        this.approvals = approvals;
    }
}
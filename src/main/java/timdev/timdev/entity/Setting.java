package timdev.timdev.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import timdev.timdev.enums.ApprovalLevel;

@Entity
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ApprovalLevel approvedLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApprovalLevel getApprovedLevel() {
        return approvedLevel;
    }

    public void setApprovedLevel(ApprovalLevel approvedLevel) {
        this.approvedLevel = approvedLevel;
    }

}

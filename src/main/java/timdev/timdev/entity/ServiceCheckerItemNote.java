package timdev.timdev.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class ServiceCheckerItemNote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_checker_item_id")
    private ServiceCheckerItem serviceCheckerItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_item_id")
    // @JsonBackReference
    private InspectionItem inspectionItem;
    
    private String note;
    private boolean passed;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public ServiceCheckerItem getServiceCheckerItem() {
        return serviceCheckerItem;
    }
    public void setServiceCheckerItem(ServiceCheckerItem serviceCheckerItem) {
        this.serviceCheckerItem = serviceCheckerItem;
    }
    public InspectionItem getInspectionItem() {
        return inspectionItem;
    }
    public void setInspectionItem(InspectionItem inspectionItem) {
        this.inspectionItem = inspectionItem;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public boolean isPassed() {
        return passed;
    }
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}

package timdev.timdev.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;


@Entity
public class InspectionResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private ServiceChecker serviceChecker;
    
    @ManyToOne
    private InspectionItem item;
    
    private boolean passed; // yes/no
    private String notes;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public ServiceChecker getServiceChecker() {
        return serviceChecker;
    }
    public void setServiceChecker(ServiceChecker serviceChecker) {
        this.serviceChecker = serviceChecker;
    }
    public InspectionItem getItem() {
        return item;
    }
    public void setItem(InspectionItem item) {
        this.item = item;
    }
    public boolean isPassed() {
        return passed;
    }
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
}

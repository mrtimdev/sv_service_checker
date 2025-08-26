package timdev.timdev.entity;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_checker_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCheckerItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_checker_id")
    @JsonBackReference
    private ServiceChecker serviceChecker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private InspectionCategory category;
    
    public InspectionCategory getCategory() {
        return category;
    }

    public void setCategory(InspectionCategory category) {
        this.category = category;
    }

    @OneToMany(mappedBy = "serviceCheckerItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<ServiceCheckerItemNote> notes;

    // @OneToMany(mappedBy = "serviceCheckerItem", cascade = CascadeType.ALL, orphanRemoval = true)
    // // @JsonBackReference
    // private List<ServiceCheckerNote> notes;

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

    public ServiceChecker getServiceChecker() {
        return serviceChecker;
    }

    public void setServiceChecker(ServiceChecker serviceChecker) {
        this.serviceChecker = serviceChecker;
    }

    // public List<ServiceCheckerNote> getNotes() {
    //     return notes;
    // }

    // public void setNotes(List<ServiceCheckerNote> notes) {
    //     this.notes = notes;
    // }

    public List<ServiceCheckerItemNote> getNotes() {
        return notes;
    }

    public void setNotes(List<ServiceCheckerItemNote> notes) {
        this.notes = notes;
    }
}

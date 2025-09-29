package timdev.timdev.entity;




import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import timdev.timdev.enums.ServiceCheckerStatus;

@Entity
@Table(name = "service_checkers")

@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ServiceChecker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceCheckerStatus status = ServiceCheckerStatus.CHECKING;

    @OneToMany(mappedBy = "serviceChecker", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<InspectionResult> inspectionResults;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by", nullable=true)
    private User updatedBy;
    

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "serviceChecker", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ServiceCheckerItem> items;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
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

    public List<ServiceCheckerItem> getItems() {
        return items;
    }

    public void setItems(List<ServiceCheckerItem> items) {
        this.items = items;
    }

    public ServiceCheckerStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceCheckerStatus status) {
        this.status = status;
    }

    public List<InspectionResult> getInspectionResults() {
        return inspectionResults;
    }

    public void setInspectionResults(List<InspectionResult> inspectionResults) {
        this.inspectionResults = inspectionResults;
    }


    public ServiceCheckerItemNote getResultForItem(Long itemId) {
        return this.getItems().stream()
            .flatMap(item -> item.getNotes().stream())
            .filter(note -> note.getInspectionItem().getId().equals(itemId))
            .findFirst()
            .orElse(null);
    }


    public long getPassedCount() {
        return this.items.stream()
                .flatMap(item -> item.getNotes().stream())
                .filter(ServiceCheckerItemNote::isPassed)
                .count();
    }

    public long getNotPassedCount() {
        return this.items.stream()
                .flatMap(item -> item.getNotes().stream())
                .filter(note -> !note.isPassed())
                .count();
    }

    public long getCheckedCount() {
        return this.items.stream()
                .flatMap(item -> item.getNotes().stream())
                .filter(ServiceCheckerItemNote::isPassed)
                .count();
    }

    public long getNotCheckedCount() {
        return this.items.stream()
                .flatMap(item -> item.getNotes().stream())
                .filter(note -> !note.isPassed())
                .count();
    }

    public long getTotalItems() {
        return this.items.stream().count();
    }
    // count item's notes
    public long getItemNoteCount() {
        return this.items.stream()
                .flatMap(item -> item.getNotes().stream())
                .count();
    }

    public String issuesStatus() {
        if (getNotCheckedCount() > 0) {
            return "Unchecked";
        }
        return "Checked";
    }
}

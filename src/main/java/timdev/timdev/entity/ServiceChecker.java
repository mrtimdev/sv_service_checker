package timdev.timdev.entity;




import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import timdev.timdev.converter.ExternalDriverDTOConverter;
import timdev.timdev.dto.ExternalDriverDTO;
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
    @JoinColumn(name = "driver_id", nullable=true)
    private Driver driver;

    @Column(name = "ex_driver_id", nullable = true)
    @Convert(converter = ExternalDriverDTOConverter.class)
    private ExternalDriverDTO exDriver;


    @Column(nullable = false)
    private String licensePlate;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String licensePlateEstimated;
    
    private String imagePath;
    
    // Device Info
    private String deviceId;
    private String deviceModel;
    private String devicePlatform;
    private String appVersion;



    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable=true)
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by", nullable=true)
    private User updatedBy;

    private String cancelReason;

    @Column(name = "cancelled_at", nullable = true)
    private LocalDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cancelled_by", nullable=true)
    private User cancelledBy;

    
    

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

    public ExternalDriverDTO getExDriver() {
        return exDriver;
    }

    public void setExDriver(ExternalDriverDTO exDriver) {
        this.exDriver = exDriver;
    }


    @Transient
    public String getTimeAgo() {
        Duration duration = Duration.between(this.createdAt, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return seconds + " seconds ago";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 2592000) return (seconds / 86400) + " days ago";
        if (seconds < 31104000) return (seconds / 2592000) + " months ago";
        return (seconds / 31104000) + " years ago";
    }

    @Transient
    public long getHoursSinceEdit() {
        if (this.createdAt == null) {
            return 0; 
        }
        return Duration.between(this.createdAt, LocalDateTime.now()).toHours();
    }

    @Transient
    public boolean canEdit() {
        return getHoursSinceEdit() <= 24;
    }

    @Transient
    public String getEditNote() {
        return canEdit() ? "Editable" : "Cannot edit (over 24h)";
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDevicePlatform() {
        return devicePlatform;
    }

    public void setDevicePlatform(String devicePlatform) {
        this.devicePlatform = devicePlatform;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getLicensePlateEstimated() {
        return licensePlateEstimated;
    }

    public void setLicensePlateEstimated(String licensePlateEstimated) {
        this.licensePlateEstimated = licensePlateEstimated;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public User getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(User cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}

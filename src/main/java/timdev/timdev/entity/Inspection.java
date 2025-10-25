package timdev.timdev.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.enums.InspectionStatus;
import timdev.timdev.listener.AuditListener;

@Entity
@Audited
@EntityListeners(AuditListener.class)
@Table(name = "inspections")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'COMPLETED'")
    private InspectionStatus status = InspectionStatus.COMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_inspection_id", nullable = false)
    private TruckInspection truckInspection; 

    private LocalDate expiredDate;

    @Min(1)
    private Integer quantity = 0;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy; 

    @Column(name = "created_at", updatable = false, nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;


    public boolean isExpired() {
        if (expiredDate == null) return false;
        return expiredDate.isBefore(LocalDate.now()) || expiredDate.isEqual(LocalDate.now());
    }

    public String getCreatedAgo() {
        if (createdAt == null) return "Unknown";

        LocalDateTime now = LocalDateTime.now();

        // If createdAt is in the future
        if (createdAt.isAfter(now)) {
            return "in the future";
        }

        Duration duration = Duration.between(createdAt, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "just now";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }

        long days = hours / 24;
        if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        }

        long months = days / 30;
        if (months < 12) {
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        }

        long years = days / 365;
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }

    public String getUpdatedAgo() {
        if (updatedAt == null) return "Never updated";

        LocalDateTime now = LocalDateTime.now();

        // If updatedAt is in the future
        if (updatedAt.isAfter(now)) {
            return "in the future";
        }

        Duration duration = Duration.between(updatedAt, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "just now";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }

        long days = hours / 24;
        if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        }

        long months = days / 30;
        if (months < 12) {
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        }

        long years = days / 365;
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }




    public long expiredDurationDays() {
        if (expiredDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiredDate) + 1;
    }

    public String expiredDurationText() {
        if (expiredDate == null) return "No expiration date";
        long days = expiredDurationDays();
        if (days > 0) return days + " days left";
        else if (days < 0) return Math.abs(days) + " days expired";
        else return "Expires today";
    }

    public String expiredDurationKHText() {
        if (expiredDate == null) return "មិនមានកាលបរិច្ឆេទផុតកំណត់";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expiredDate);

        if (days < 0) {
            return "ផុតកំណត់ " + Math.abs(days) + " ថ្ងៃហើយ";
        } else if (days == 0) {
            return "ផុតកំណត់ថ្ងៃនេះ";
        } else if (days <= 30) {
            return "នៅសល់ " + days + " ថ្ងៃ (ជិតផុតកំណត់)";
        } else {
            return "នៅសល់ " + days + " ថ្ងៃ";
        }
    }


    public String expiredColor_() {
        long days = expiredDurationDays();
        if (days <= 31) return "bg-red-500 dark:bg-red-700 text-white need-to-inspection"; // less than or equal 31 days
        else if (days <= 90) return "bg-yellow-500 dark:bg-yellow-600 text-white need-to-inspection"; // 32-90 days
        return "insufficient"; // more than 90 days → normal
    }

    public String expiredColor() {
        long days = expiredDurationDays() - 30; // expiredDate - today

        if (days > 0 && days <= 30) {
            // Will expire within the next 30 days
            return "bg-yellow-500 dark:bg-yellow-600 text-white need-to-inspection";
        } else if (days < 0) {
            // Already expired → red
            return "bg-red-500 dark:bg-red-700 text-white need-to-inspection";
        }
        // More than 30 days left → normal
        return "insufficient";
    }






    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TruckInspection getTruckInspection() {
        return truckInspection;
    }

    public void setTruckInspection(TruckInspection truckInspection) {
        this.truckInspection = truckInspection;
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
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


    public InspectionStatus getStatus() {
        return status;
    }


    public void setStatus(InspectionStatus status) {
        this.status = status;
    }

}
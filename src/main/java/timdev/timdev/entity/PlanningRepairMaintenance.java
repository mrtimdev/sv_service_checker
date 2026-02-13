package timdev.timdev.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import timdev.timdev.entity.Truck;
import timdev.timdev.enums.IntervalType;
import timdev.timdev.enums.PlanningStatus;

@Entity
@Table(name = "planning_repair_maintenance")
public class PlanningRepairMaintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planning_id")
    private Long planningId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @Column(name = "plan_title")
    private String planTitle;

    @Column(name = "plan_task_text", columnDefinition = "TEXT")
    private String planTaskText;

    @Column(name = "actual_task_text", columnDefinition = "TEXT")
    private String actualTaskText;

    @Column(name = "note_text", columnDefinition = "TEXT")
    private String noteText;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "planned_duration_days")
    private Integer plannedDurationDays;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "interval_type", nullable = false)
    private IntervalType intervalType = IntervalType.NONE;

    @Column(name = "interval_value")
    private Integer intervalValue;

    @Column(name = "next_planned_date")
    private LocalDate nextPlannedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlanningStatus status = PlanningStatus.PLANNED;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        computePlannedDuration();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        computePlannedDuration();
    }

    public void computePlannedDuration() {
        if (plannedStartDate != null && plannedEndDate != null) {
            long days = ChronoUnit.DAYS.between(plannedStartDate, plannedEndDate);
            plannedDurationDays = (int) days;
        }
    }

    public Long getPlanningId() {
        return planningId;
    }

    public void setPlanningId(Long planningId) {
        this.planningId = planningId;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public String getPlanTaskText() {
        return planTaskText;
    }

    public void setPlanTaskText(String planTaskText) {
        this.planTaskText = planTaskText;
    }

    public String getActualTaskText() {
        return actualTaskText;
    }

    public void setActualTaskText(String actualTaskText) {
        this.actualTaskText = actualTaskText;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public LocalDate getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(LocalDate plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(LocalDate plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public Integer getPlannedDurationDays() {
        return plannedDurationDays;
    }

    public void setPlannedDurationDays(Integer plannedDurationDays) {
        this.plannedDurationDays = plannedDurationDays;
    }

    public LocalDateTime getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(LocalDateTime actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public LocalDateTime getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDateTime actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public IntervalType getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(IntervalType intervalType) {
        this.intervalType = intervalType;
    }

    public Integer getIntervalValue() {
        return intervalValue;
    }

    public void setIntervalValue(Integer intervalValue) {
        this.intervalValue = intervalValue;
    }

    public LocalDate getNextPlannedDate() {
        return nextPlannedDate;
    }

    public void setNextPlannedDate(LocalDate nextPlannedDate) {
        this.nextPlannedDate = nextPlannedDate;
    }

    public PlanningStatus getStatus() {
        return status;
    }

    public void setStatus(PlanningStatus status) {
        this.status = status;
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
}

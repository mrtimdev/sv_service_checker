package timdev.timdev.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.PlanningRepairMaintenance;
import timdev.timdev.entity.Truck;
import timdev.timdev.enums.IntervalType;
import timdev.timdev.enums.PlanningStatus;
import timdev.timdev.repository.PlanningRepairMaintenanceRepository;
import timdev.timdev.repository.TruckRepository;

@Service
@RequiredArgsConstructor
public class PlanningRepairMaintenanceService {

    private final PlanningRepairMaintenanceRepository planningRepo;
    private final TruckRepository truckRepo;

    @Transactional
    public PlanningRepairMaintenance createPlanning(PlanningRepairMaintenance planning) {
        if (planning.getTruck() != null && planning.getTruck().getId() != null) {
            List<PlanningStatus> activeStatuses = List.of(PlanningStatus.PLANNED, PlanningStatus.IN_PROGRESS);
            boolean hasActive = planningRepo.existsByTruck_IdAndStatusIn(planning.getTruck().getId(), activeStatuses);
            if (hasActive) {
                throw new RuntimeException("Truck already has an active PM plan");
            }
        }
        planning.setStatus(PlanningStatus.PLANNED);
        if (planning.getPlanTitle() == null || planning.getPlanTitle().isBlank()) {
            planning.setPlanTitle("Annual PM Inspection");
        }
        if (planning.getIntervalType() == null) {
            planning.setIntervalType(IntervalType.YEAR);
        }
        if (planning.getIntervalValue() == null && planning.getIntervalType() != IntervalType.NONE) {
            planning.setIntervalValue(1);
        }
        if (planning.getPlannedStartDate() != null && planning.getPlannedEndDate() == null) {
            planning.setPlannedEndDate(planning.getPlannedStartDate().plusDays(7));
        }
        planning.setPlannedDurationDays(calculateDuration(planning.getPlannedStartDate(), planning.getPlannedEndDate()));
        return planningRepo.save(planning);
    }

    @Transactional
    public PlanningRepairMaintenance updatePlanning(Long id, PlanningRepairMaintenance data) {
        PlanningRepairMaintenance existing = planningRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Planning not found"));

        if (existing.getStatus() == PlanningStatus.COMPLETED) {
            throw new RuntimeException("Cannot modify completed planning");
        }

        if (data.getTruck() != null && data.getTruck().getId() != null) {
            Truck truck = truckRepo.findById(data.getTruck().getId())
                .orElseThrow(() -> new RuntimeException("Truck not found"));
            existing.setTruck(truck);
        }

        existing.setPlanTitle(data.getPlanTitle());
        existing.setPlanTaskText(data.getPlanTaskText());
        existing.setActualTaskText(data.getActualTaskText());
        existing.setNoteText(data.getNoteText());
        existing.setPlannedStartDate(data.getPlannedStartDate());
        existing.setPlannedEndDate(data.getPlannedEndDate());
        if (existing.getPlannedStartDate() != null && existing.getPlannedEndDate() == null) {
            existing.setPlannedEndDate(existing.getPlannedStartDate().plusDays(7));
        }
        existing.setIntervalType(data.getIntervalType());
        existing.setIntervalValue(data.getIntervalValue());
        existing.setPlannedDurationDays(calculateDuration(existing.getPlannedStartDate(), existing.getPlannedEndDate()));

        return planningRepo.save(existing);
    }

    @Transactional
    public PlanningRepairMaintenance startRepair(Long id) {
        PlanningRepairMaintenance planning = planningRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Planning not found"));

        List<PlanningRepairMaintenance> inProgress = planningRepo
            .findByTruck_IdAndStatus(planning.getTruck().getId(), PlanningStatus.IN_PROGRESS);
        if (!inProgress.isEmpty()) {
            throw new RuntimeException("Another repair is already in progress for this vehicle");
        }
        if (planning.getStatus() == PlanningStatus.COMPLETED || planning.getStatus() == PlanningStatus.CANCELLED) {
            throw new RuntimeException("Cannot start a completed or cancelled planning");
        }

        planning.setStatus(PlanningStatus.IN_PROGRESS);
        planning.setActualStartDate(LocalDateTime.now());
        return planningRepo.save(planning);
    }

    @Transactional
    public PlanningRepairMaintenance completeRepair(Long id) {
        PlanningRepairMaintenance planning = planningRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Planning not found"));

        if (planning.getStatus() == PlanningStatus.CANCELLED) {
            throw new RuntimeException("Cannot complete a cancelled planning");
        }
        planning.setStatus(PlanningStatus.COMPLETED);
        planning.setActualEndDate(LocalDateTime.now());
        PlanningRepairMaintenance saved = planningRepo.save(planning);

        generateNextScheduleIfPreventive(planning);

        return saved;
    }

    @Transactional
    public PlanningRepairMaintenance cancelPlanning(Long id) {
        PlanningRepairMaintenance planning = planningRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Planning not found"));

        planning.setStatus(PlanningStatus.CANCELLED);
        return planningRepo.save(planning);
    }

    @Transactional
    public int bulkCreate(List<Long> truckIds, PlanningRepairMaintenance template) {
        if (truckIds == null || truckIds.isEmpty()) {
            return 0;
        }
        int created = 0;
        for (Long truckId : truckIds) {
            Truck truck = truckRepo.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found: " + truckId));
            PlanningRepairMaintenance plan = new PlanningRepairMaintenance();
            plan.setTruck(truck);
            plan.setPlanTitle(template.getPlanTitle());
            plan.setPlanTaskText(template.getPlanTaskText());
            plan.setActualTaskText(template.getActualTaskText());
            plan.setNoteText(template.getNoteText());
            plan.setPlannedStartDate(template.getPlannedStartDate());
            plan.setPlannedEndDate(template.getPlannedEndDate());
            plan.setIntervalType(template.getIntervalType());
            plan.setIntervalValue(template.getIntervalValue());
            createPlanning(plan);
            created++;
        }
        return created;
    }

    private void generateNextScheduleIfPreventive(PlanningRepairMaintenance planning) {
        if (planning.getActualEndDate() == null) {
            return;
        }

        IntervalType intervalType = planning.getIntervalType();
        Integer intervalValue = planning.getIntervalValue();
        if (intervalType == null) {
            intervalType = IntervalType.YEAR;
            intervalValue = intervalValue != null ? intervalValue : 1;
        }
        if (intervalType == IntervalType.NONE) {
            return;
        }
        if (intervalValue == null || intervalValue <= 0) {
            intervalValue = 1;
        }

        LocalDate baseDate = planning.getActualEndDate().toLocalDate();
        LocalDate nextDate;
        if (intervalType == IntervalType.MONTH) {
            nextDate = baseDate.plusMonths(intervalValue);
        } else if (intervalType == IntervalType.YEAR) {
            nextDate = baseDate.plusYears(intervalValue);
        } else if (intervalType == IntervalType.KM) {
            return;
        } else {
            return;
        }

        PlanningRepairMaintenance next = new PlanningRepairMaintenance();
        next.setTruck(planning.getTruck());
        next.setPlanTitle(planning.getPlanTitle());
        next.setPlanTaskText(planning.getPlanTaskText());
        next.setActualTaskText(null);
        next.setNoteText(planning.getNoteText());
        next.setPlannedStartDate(nextDate);
        Integer duration = planning.getPlannedDurationDays();
        next.setPlannedEndDate(duration != null ? nextDate.plusDays(duration) : null);
        next.setPlannedDurationDays(duration);
        next.setIntervalType(intervalType);
        next.setIntervalValue(intervalValue);
        next.setNextPlannedDate(nextDate);
        next.setStatus(PlanningStatus.PLANNED);

        planningRepo.save(next);
    }

    private Integer calculateDuration(LocalDate start, LocalDate end) {
        if (start == null || end == null) return null;
        long days = ChronoUnit.DAYS.between(start, end);
        return (int) days;
    }
}

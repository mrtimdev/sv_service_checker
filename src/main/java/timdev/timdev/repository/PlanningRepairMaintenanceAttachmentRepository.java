package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.PlanningRepairMaintenanceAttachment;

@Repository
public interface PlanningRepairMaintenanceAttachmentRepository extends JpaRepository<PlanningRepairMaintenanceAttachment, Long> {
}

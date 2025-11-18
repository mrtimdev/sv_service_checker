package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import timdev.timdev.dto.ApproveStatus;
import timdev.timdev.entity.CompanyTruck;
import timdev.timdev.entity.SubTruck;
import timdev.timdev.entity.Truck;

@Repository
public interface SubTruckRepository extends JpaRepository<SubTruck, Long>, JpaSpecificationExecutor<SubTruck> {

    default Page<SubTruck> findFiltered(
            String licensePlate,
            String truckOwner,
            Long truckId,
            ApproveStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return findAll(
                createFilterSpecification(licensePlate, truckOwner, truckId, status, startDate, endDate),
                pageable
        );
    }

    private Specification<SubTruck> createFilterSpecification(
            String licensePlate,
            String truckOwner,
            Long truckId,
            ApproveStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // License plate filter
            if (licensePlate != null && !licensePlate.trim().isEmpty()) {
                Join<SubTruck, Truck> truckJoin = root.join("truck");
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(truckJoin.get("licensePlate")),
                                "%" + licensePlate.toLowerCase() + "%"
                        )
                );
            }

            // Truck ID filter
            if (truckId != null) {
                Join<SubTruck, Truck> truckJoin = root.join("truck");
                predicates.add(criteriaBuilder.equal(truckJoin.get("id"), truckId));
            }

            if (truckOwner != null && !truckOwner.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("truckOwner"),
                        "%" + truckOwner + "%"
                ));
            }


            // Status filter
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Date range filter
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    boolean existsByTruckAndDate(Truck truck, LocalDate date);

    Optional<SubTruck> findByTruckAndDate(Truck truck, LocalDate date);
}

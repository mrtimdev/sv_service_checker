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
import timdev.timdev.entity.FuelRequest;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;

@Repository
public interface FuelRequestRepository extends JpaRepository<FuelRequest, Long>, JpaSpecificationExecutor<FuelRequest> {

    default Page<FuelRequest> findFiltered(
            String requester,
            String position,
            String purpose,
            Long truckId,
            ApproveStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable,
            Long createdById
    ) {
        return findAll(
                createFilterSpecification(requester, position, purpose ,truckId, status, startDate, endDate, createdById),
                pageable
        );
    }

    private Specification<FuelRequest> createFilterSpecification(
            String requester,
            String position,
            String purpose,
            Long truckId,
            ApproveStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Long createdById
    ) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

        
            // Truck ID filter
            if (truckId != null) {
                Join<FuelRequest, Truck> truckJoin = root.join("truck");
                predicates.add(criteriaBuilder.equal(truckJoin.get("id"), truckId));
            }

            if (requester != null && !requester.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("requester"),
                        "%" + requester + "%"
                ));
            }

            if (position != null && !position.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("position"),
                        "%" + position + "%"
                ));
            }
            if (purpose != null && !purpose.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("purpose"),
                        "%" + purpose + "%"
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

            if (createdById != null) {
                Join<FuelRequest, User> userJoin = root.join("createdBy");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), createdById));
            }

            if (createdById != null) {
                Join<FuelRequest, User> userJoin = root.join("createdBy");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), createdById));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    boolean existsByTruckAndDate(Truck truck, LocalDate date);

    Optional<FuelRequest> findByTruckAndDate(Truck truck, LocalDate date);


    

}

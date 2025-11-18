package timdev.timdev.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.TruckGroup;

@Repository
public interface TruckGroupRepository extends JpaRepository<TruckGroup, Long> {
    boolean existsByName(String name);
}
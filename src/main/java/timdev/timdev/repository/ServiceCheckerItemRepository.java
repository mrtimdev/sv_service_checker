package timdev.timdev.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.ServiceCheckerItem;

public interface ServiceCheckerItemRepository extends JpaRepository<ServiceCheckerItem, Long> {
    
}

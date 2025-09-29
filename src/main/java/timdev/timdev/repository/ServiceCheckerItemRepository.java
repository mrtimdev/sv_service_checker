package timdev.timdev.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import timdev.timdev.entity.ServiceCheckerItem;

public interface ServiceCheckerItemRepository extends JpaRepository<ServiceCheckerItem, Long> {
    

    @Modifying
    @Query("DELETE FROM ServiceCheckerItem i WHERE i.category.id = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Long categoryId);
}

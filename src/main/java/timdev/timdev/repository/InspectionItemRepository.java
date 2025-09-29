package timdev.timdev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.InspectionItem;


@Repository
public interface InspectionItemRepository extends JpaRepository<InspectionItem, Long> {
    List<InspectionItem> findByCategoryId(Long categoryId);

    List<InspectionItem> findAllByOrderByCategory_NameAscNameAsc();


    Optional<InspectionItem> findByName(String name);
    
    @Query("SELECT i FROM InspectionItem i WHERE i.name = :name AND i.id != :id")
    Optional<InspectionItem> findByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
    
    boolean existsByName(String name);
    
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM InspectionItem i WHERE i.name = :name AND i.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}
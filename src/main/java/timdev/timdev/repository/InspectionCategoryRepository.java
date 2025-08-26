package timdev.timdev.repository;

import java.util.List;
import java.util.Locale.Category;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.InspectionCategory;


@Repository
public interface InspectionCategoryRepository extends JpaRepository<InspectionCategory, Long> {


    /**
     * Find category by name
     * @param name Category name
     * @return Optional containing the category if found
     */
    Optional<InspectionCategory> findByName(String name);

    /**
     * Find category by name excluding a specific ID
     * @param name Category name
     * @param id Category ID to exclude
     * @return Optional containing the category if found
     */
    @Query("SELECT c FROM InspectionCategory c WHERE c.name = :name AND c.id != :id")
    Optional<InspectionCategory> findByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * Check if category exists by name
     * @param name Category name
     * @return true if category exists
     */
    boolean existsByName(String name);

    /**
     * Check if category exists by name excluding a specific ID
     * @param name Category name
     * @param id Category ID to exclude
     * @return true if category exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM InspectionCategory c WHERE c.name = :name AND c.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);



    @Query("SELECT c FROM InspectionCategory c LEFT JOIN FETCH c.items")
    List<InspectionCategory> findAllWithItems();
    
    @Query("SELECT c FROM InspectionCategory c LEFT JOIN FETCH c.items WHERE c.id = :id")
    Optional<InspectionCategory> findByIdWithItems(@Param("id") Long id);
}
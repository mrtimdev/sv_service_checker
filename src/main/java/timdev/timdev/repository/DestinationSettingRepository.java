package timdev.timdev.repository;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Distribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.DestinationSetting;

@Repository
public interface DestinationSettingRepository extends JpaRepository<DestinationSetting, Long>, JpaSpecificationExecutor<Distribution> {

    DestinationSetting findByCode(String code);
    DestinationSetting findByName(String name);


    // Pagination + Search
    @Query("""
        SELECT d FROM DestinationSetting d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<DestinationSetting> searchWithPage(String search, Pageable pageable);

    // Show all + Sort
    @Query("""
        SELECT d FROM DestinationSetting d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    List<DestinationSetting> searchAll(String search, Sort sort);
    
}

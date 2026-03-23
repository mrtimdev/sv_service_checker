package timdev.timdev.repository;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Distribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;

@Repository
public interface DestinationSettingRepository
                extends JpaRepository<DestinationSetting, Long>, JpaSpecificationExecutor<Distribution> {

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

        @Query("""
                        SELECT d FROM DestinationSetting d

                        WHERE (
                                LOWER(d.code) LIKE LOWER(CONCAT('%', :q, '%'))
                                OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%'))
                                OR CAST(d.distance AS string) LIKE CONCAT('%', :q, '%')
                        )

                        ORDER BY d.code DESC
                        """)
        List<DestinationSetting> searchSettingsByCodeAndName(
                        @Param("q") String q);

        @Query("""
                        SELECT t FROM DestinationSetting t
                        WHERE t.id NOT IN (
                            SELECT a.destination.id FROM DestinationScaleStation a WHERE (:settingId IS NULL OR a.destination.id != :settingId)
                        )
                        """)
        List<DestinationSetting> findSettingsIsNotInScaleStation(
                        @Param("settingId") Long settingId);

        @Query("""
                        SELECT t FROM DestinationSetting t
                        WHERE t.id NOT IN (
                            SELECT a.destination.id FROM DestinationPort a WHERE (:settingId IS NULL OR a.destination.id != :settingId)
                        )
                        """)
        List<DestinationSetting> findSettingsIsNotInPort(
                        @Param("settingId") Long settingId);

        // Or if you want trucks without averages for specific measurements:
        // @Query("SELECT t FROM DestinationSetting t WHERE t.id NOT IN " +
        // "(SELECT a.destinationSetting.id FROM DestinationScaleStation a WHERE
        // a.measurement.id = :measurementId)")
        // List<DestinationSetting>
        // findDestinationScaleStationsWithoutForMeasurement(@Param("measurementId")
        // Long measurementId);

}

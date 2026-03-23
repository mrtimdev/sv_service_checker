package timdev.timdev.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import timdev.timdev.entity.ScaleStation;
import timdev.timdev.dto.DestinationScaleStationDTO;
import timdev.timdev.entity.DestinationScaleStation;
import timdev.timdev.entity.DestinationSetting;

@Repository
public interface DestinationScaleStationRepository extends JpaRepository<DestinationScaleStation, Long> {

        List<DestinationScaleStation> findByDestinationId(Long destinationId);

        @Query("""
                            SELECT new timdev.timdev.dto.DestinationScaleStationDTO(
                                dss.id,
                                d.id,
                                ss.id,
                                ss.name,
                                dss.amount
                            )
                            FROM DestinationScaleStation dss
                            JOIN dss.destination d
                            JOIN dss.scaleStation ss
                            WHERE d.id = :destinationId
                        """)
        List<DestinationScaleStationDTO> findDTOByDestinationId(Long destinationId);

        // Find with paging
        Page<DestinationScaleStation> findByDestinationId(Long destinationId, Pageable pageable);

        // Optional: find by truck + scaleStation
        Optional<DestinationScaleStation> findByDestinationIdAndScaleStationId(Long destinationId, Long scaleStationId);

        @Query("SELECT a.scaleStation FROM DestinationScaleStation a WHERE a.destination.id = :destinationId")
        List<ScaleStation> findScaleStationsByDestinationId(Long destinationId);

        @Modifying
        @Transactional
        @Query("DELETE FROM DestinationScaleStation d WHERE d.destination.id = :destinationId")
        void deleteByDestination(Long destinationId);

        @Query("SELECT a FROM DestinationScaleStation a WHERE (:destinationId IS NULL OR a.destination.id = :destinationId) "
                        +
                        "ORDER BY a.createdAt DESC")
        Page<DestinationScaleStation> findByDestinationWithFilter(@Param("destinationId") Long destinationId,
                        Pageable pageable);

        List<DestinationScaleStation> findByDestinationIdOrderByCreatedAtDesc(Long destinationId);

        List<DestinationScaleStation> findAllByOrderByCreatedAtDesc();

        @Query("SELECT a FROM DestinationScaleStation a WHERE a.destination.id = :destinationId AND a.scaleStation.id = :scaleStationId")
        Optional<DestinationScaleStation> findDestinationScaleStationByTruckAndScaleStation(
                        @Param("destinationId") Long destinationId,
                        @Param("scaleStationId") Long scaleStationId);

        boolean existsByDestinationIdAndScaleStationId(Long destinationId, Long scaleStationId);

        boolean existsByDestinationIdAndScaleStationIdAndIdNot(Long destinationId, Long scaleStationId, Long id);

        List<DestinationScaleStation> findByDestinationIn(List<DestinationSetting> destinations);

        @Query("SELECT dss FROM DestinationScaleStation dss " +
                        "JOIN FETCH dss.scaleStation ss " +
                        "WHERE ss.name = :scaleStationName " +
                        "AND dss.minWeight <= :totalWeight " +
                        "AND dss.maxWeight > :totalWeight")
        Optional<DestinationScaleStation> findScaleFee(
                        @Param("scaleStationName") String scaleStationName,
                        @Param("totalWeight") BigDecimal totalWeight);

        @Query("SELECT dss FROM DestinationScaleStation dss " +
                        "JOIN FETCH dss.scaleStation ss " +
                        "WHERE ss.isHighway = true " +
                        "AND ss.name = :scaleStationName")
        Optional<DestinationScaleStation> findHighwayScale(@Param("scaleStationName") String scaleStationName);

        @Query("SELECT dss FROM DestinationScaleStation dss " +
                        "JOIN FETCH dss.scaleStation ss " +
                        "WHERE ss.name = :scaleStationName")
        List<DestinationScaleStation> findByScaleStationName(@Param("scaleStationName") String scaleStationName);

        @Query("SELECT CASE WHEN COUNT(dss) > 0 THEN true ELSE false END " +
                        "FROM DestinationScaleStation dss " +
                        "JOIN dss.scaleStation ss " +
                        "WHERE ss.name = :scaleStationName " +
                        "AND ss.isHighway = true")
        boolean isHighwayStation(@Param("scaleStationName") String scaleStationName);
}

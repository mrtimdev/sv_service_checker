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
import timdev.timdev.dto.DestinationPortDTO;
import timdev.timdev.entity.DestinationPort;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Port;

@Repository
public interface DestinationPortRepository extends JpaRepository<DestinationPort, Long> {

    List<DestinationPort> findByDestinationId(Long destinationId);

    // Find with paging
    Page<DestinationPort> findByDestinationId(Long destinationId, Pageable pageable);

    // Optional: find by truck + scaleStation
    Optional<DestinationPort> findByDestinationIdAndPortId(Long destinationId, Long portId);

    @Query("SELECT a.port FROM DestinationPort a WHERE a.destination.id = :destinationId")
    List<Port> findScaleStationsByDestinationId(Long destinationId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DestinationPort d WHERE d.destination.id = :destinationId")
    void deleteByDestination(Long destinationId);

    @Query("SELECT a FROM DestinationPort a WHERE (:destinationId IS NULL OR a.destination.id = :destinationId) "
            +
            "ORDER BY a.createdAt DESC")
    Page<DestinationPort> findByDestinationWithFilter(@Param("destinationId") Long destinationId,
            Pageable pageable);

    List<DestinationPort> findByDestinationIdOrderByCreatedAtDesc(Long destinationId);

    List<DestinationPort> findAllByOrderByCreatedAtDesc();

    @Query("SELECT a FROM DestinationPort a WHERE a.destination.id = :destinationId AND a.port.id = :portId")
    Optional<DestinationPort> findDestinationScaleStationByTruckAndScaleStation(
            @Param("destinationId") Long destinationId,
            @Param("portId") Long portId);

    boolean existsByDestinationIdAndPortId(Long destinationId, Long portId);

    boolean existsByDestinationIdAndPortIdAndIdNot(Long destinationId, Long portId, Long id);

    List<DestinationPort> findByDestinationIn(List<DestinationSetting> destinations);

    @Query("SELECT dss FROM DestinationPort dss " +
            "JOIN FETCH dss.port ss " +
            "WHERE ss.name = :portName " +
            "AND dss.minWeight <= :totalWeight " +
            "AND dss.maxWeight > :totalWeight")
    Optional<DestinationPort> findScaleFee(
            @Param("portName") String portName,
            @Param("totalWeight") BigDecimal totalWeight);

    @Query("SELECT dss FROM DestinationPort dss " +
            "JOIN FETCH dss.port ss " +
            "WHERE ss.isHighway = true " +
            "AND ss.name = :portName")
    Optional<DestinationPort> findHighwayScale(@Param("portName") String portName);

    @Query("SELECT dss FROM DestinationPort dss " +
            "JOIN FETCH dss.port ss " +
            "WHERE ss.name = :portName")
    List<DestinationPort> findByPortName(@Param("portName") String portName);

    @Query("SELECT CASE WHEN COUNT(dss) > 0 THEN true ELSE false END " +
            "FROM DestinationPort dss " +
            "JOIN dss.port ss " +
            "WHERE ss.name = :portName " +
            "AND ss.isHighway = true")
    boolean isHighwayStation(@Param("portName") String portName);

    @Query("""
                SELECT new timdev.timdev.dto.DestinationPortDTO(
                    dp.id,
                    d.id,
                    p.id,
                    p.name,
                    dp.amount,
                    p.type
                )
                FROM DestinationPort dp
                JOIN dp.destination d
                JOIN dp.port p
                WHERE d.id = :destinationId
            """)
    List<DestinationPortDTO> findDTOByDestinationId(@Param("destinationId") Long destinationId);
}

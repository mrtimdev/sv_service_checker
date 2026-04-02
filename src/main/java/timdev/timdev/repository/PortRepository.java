package timdev.timdev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.dto.PortType;
import timdev.timdev.entity.Port;

@Repository
public interface PortRepository extends JpaRepository<Port, Long> {

    // Find ports by type
    List<Port> findByType(PortType type);

    // Check if a port exists by name
    boolean existsByName(String name);

    // Find a port by name
    Optional<Port> findByName(String name);
}
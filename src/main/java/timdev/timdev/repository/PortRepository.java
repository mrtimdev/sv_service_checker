package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Port;

@Repository
public interface PortRepository extends JpaRepository<Port, Long> {

}
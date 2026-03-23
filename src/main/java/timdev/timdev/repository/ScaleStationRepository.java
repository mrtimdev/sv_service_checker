package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.ScaleStation;

@Repository
public interface ScaleStationRepository extends JpaRepository<ScaleStation, Long> {

}
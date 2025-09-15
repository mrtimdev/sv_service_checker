package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckDistance;



@Repository
public interface TruckDistanceRepository extends JpaRepository<TruckDistance, Long> {

    List<TruckDistance> findByTruck(Truck truck);

    List<TruckDistance> findByDate(LocalDate date);

    List<TruckDistance> findByDateBetween(LocalDate start, LocalDate end);
}

package timdev.timdev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.micrometer.core.instrument.Measurement;
import timdev.timdev.entity.Average;

@Repository
public interface AverageRepository extends JpaRepository<Average, Long> {
    Optional<Measurement> findByValue(double value);


    Average findByMeasurementId(Long measurementId);
    Average findByValueAndMeasurementId(double value, Long measurementId);
}

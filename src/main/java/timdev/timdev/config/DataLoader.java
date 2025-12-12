package timdev.timdev.config;


import jakarta.annotation.PostConstruct;
import timdev.timdev.entity.Measurement;
import timdev.timdev.repository.MeasurementRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader {

    private final MeasurementRepository measurementRepository;

    public DataLoader(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    // Mapping choice:
    // MILD      -> "ស្រាល"      (light)
    // MODERATE  -> "មធ្យម"     (moderate)
    // SEVERE    -> "ធ្ងន់ធ្ងរ" (severe)
    // TRANSFER  -> "ផ្ទេរ"     (transfer)
    @PostConstruct
    @Transactional
    public void init() {
        createIfNotExists("MILD", "ស្រាល");
        createIfNotExists("MODERATE", "មធ្យម");
        createIfNotExists("SEVERE", "ធ្ងន់ធ្ងរ");
        createIfNotExists("TRANSFER", "ផ្ទេរ");
    }

    private void createIfNotExists(String name, String nativeName) {
        measurementRepository.findByName(name)
                .orElseGet(() -> measurementRepository.save(new Measurement(name, nativeName)));
    }
}
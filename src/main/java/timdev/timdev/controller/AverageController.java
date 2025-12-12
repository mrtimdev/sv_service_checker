package timdev.timdev.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import timdev.timdev.entity.Average;
import timdev.timdev.entity.Measurement;
import timdev.timdev.repository.AverageRepository;
import timdev.timdev.repository.MeasurementRepository;



@Controller
@RequestMapping("/averages")
public class AverageController {



    private final MeasurementRepository measurementRepository;
    private final AverageRepository averageRepository;

    public AverageController(MeasurementRepository measurementRepository,
                             AverageRepository averageRepository) {
        this.measurementRepository = measurementRepository;
        this.averageRepository = averageRepository;
    }

    @GetMapping("/form")
    public String showForm(Model model) {
        List<Measurement> measurements = measurementRepository.findAll();
        model.addAttribute("measurements", measurements);

        // default Average object with default value 0.16
        Average avg = new Average();
        avg.setValue(0.16);
        model.addAttribute("average", avg);
        return "averages/form";
    }

     @GetMapping("/edit/{id}")
    public String showFormEdit(@PathVariable("id") Long id,Model model) {
        List<Measurement> measurements = measurementRepository.findAll();
        model.addAttribute("measurements", measurements);
        Average avg = averageRepository.findById(id).orElse(null);
        model.addAttribute("average", avg);
        return "averages/form";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam("value") double value,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("measurementId") Long measurementId,
            Model model) {

        Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid measurement id: " + measurementId));

        // ------------------------------
        // 1. CHECK UNIQUE (value + measurement)
        // ------------------------------
        Average duplicate = averageRepository.findByValueAndMeasurementId(value, measurementId);

        if (duplicate != null && (id == null || !duplicate.getId().equals(id))) {
            // duplicate found but not the same record
            model.addAttribute("error", "This average already exists for this measurement.");
            model.addAttribute("measurements", measurementRepository.findAll());
            model.addAttribute("average", new Average(value, measurement));
            return "averages/form";
        }

        // ------------------------------
        // 2. UPDATE
        // ------------------------------
        if (id != null) {
            Average existing = averageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid average id: " + id));

            existing.setValue(value);
            existing.setMeasurement(measurement);

            averageRepository.save(existing);
            return "redirect:/averages";
        }

        // ------------------------------
        // 3. CREATE
        // ------------------------------
        Average average = new Average(value, measurement);
        averageRepository.save(average);

        return "redirect:/averages";
    }



    @GetMapping({"", "/list"})
    public String list(Model model) {
        model.addAttribute("averages", averageRepository.findAll());
        return "averages/index";
    }
    
}

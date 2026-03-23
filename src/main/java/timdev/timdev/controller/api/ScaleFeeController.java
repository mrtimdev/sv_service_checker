// ScaleFeeController.java
package timdev.timdev.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import timdev.timdev.dto.DestinationFullDTO;
import timdev.timdev.dto.DestinationPortDTO;
import timdev.timdev.dto.DestinationScaleStationDTO;
import timdev.timdev.dto.DestinationSettingDTO;
import timdev.timdev.entity.DestinationScaleStation;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.service.DestinationPortService;
import timdev.timdev.service.DestinationScaleStationService;
import timdev.timdev.service.DestinationSettingService;
import timdev.timdev.service.ScaleFeeService;
import timdev.timdev.service.TruckService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/scale-fee")
@CrossOrigin(origins = "*")
public class ScaleFeeController {

    @Autowired
    private ScaleFeeService scaleFeeService;

    @Autowired
    private DestinationSettingService destinationSettingService;

    @Autowired
    private TruckService truckService;

    @Autowired
    private DestinationScaleStationService destinationScaleStationService;

    @Autowired
    private DestinationPortService destinationPortService;

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateScaleFee(@RequestBody ScaleFeeRequest request) {
        try {
            BigDecimal amount = scaleFeeService.calculateScaleFee(
                    request.getLicensePlate(),
                    request.getScaleStationName(),
                    request.getTotalWeight());

            // Get truck info for response (for display purposes only)
            Integer routeNumber = null;
            String truckGroup = null;
            boolean truckFound = false;

            Optional<Truck> truckOpt = truckService.findByLicensePlate(request.getLicensePlate());
            if (truckOpt.isPresent()) {
                truckFound = true;
                Truck truck = truckOpt.get();
                if (truck.getRouteNumber() != null) {
                    try {
                        routeNumber = Integer.parseInt(truck.getRouteNumber());
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
                truckGroup = truck.getGroup();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("amount", amount);
            response.put("routeNumber", routeNumber);
            response.put("truckGroup", truckGroup);
            response.put("licensePlate", request.getLicensePlate());
            response.put("scaleStation", request.getScaleStationName());
            response.put("totalWeight", request.getTotalWeight());
            response.put("isHighway", false);
            response.put("truckFound", truckFound);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/destination-with-scales")
    public ResponseEntity<Map<String, Object>> getDestinationWithScales(
            @RequestParam("destinationName") String destinationName) {

        Map<String, Object> response = new HashMap<>();

        DestinationSetting setting = destinationSettingService.findByName(destinationName);

        if (setting == null) {
            response.put("success", false);
            response.put("message", "Destination not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        List<DestinationScaleStationDTO> scales = destinationScaleStationService.getByDestinationId(setting.getId());

        response.put("success", true);
        response.put("destination", setting);
        response.put("scales", scales);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/destination-ports")
    public ResponseEntity<?> getPortsByDestination(@RequestParam Long destinationId) {

        List<DestinationPortDTO> ports = destinationPortService.getByDestinationId(destinationId);

        return ResponseEntity.ok(ports);
    }

    @GetMapping("/destination-with-scales-ports")
    public ResponseEntity<?> getFullData(@RequestParam String destinationName) {

        DestinationSetting setting = destinationSettingService.findByName(destinationName);

        if (setting == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Destination not found"));
        }

        // ✅ convert destination → DTO
        DestinationSettingDTO destinationDTO = new DestinationSettingDTO(
                setting.getId(),
                setting.getName());

        // ✅ get lists
        List<DestinationScaleStationDTO> scales = destinationScaleStationService.getByDestinationId(setting.getId());

        List<DestinationPortDTO> ports = destinationPortService.getByDestinationId(setting.getId());

        // ✅ final response DTO
        DestinationFullDTO response = new DestinationFullDTO(
                destinationDTO,
                scales,
                ports);

        return ResponseEntity.ok(response);
    }

    // @PostMapping("/batch-calculate")
    // public ResponseEntity<Map<String, Object>>
    // batchCalculateScaleFees(@RequestBody BatchScaleFeeRequest request) {
    // try {
    // Map<String, BigDecimal> results = scaleFeeService.batchCalculateScaleFees(
    // request.getLicensePlates(),
    // request.getScaleStationName(),
    // request.getTotalWeight());

    // Map<String, Object> response = new HashMap<>();
    // response.put("success", true);
    // response.put("results", results);
    // response.put("scaleStation", request.getScaleStationName());
    // response.put("totalWeight", request.getTotalWeight());

    // return ResponseEntity.ok(response);
    // } catch (Exception e) {
    // Map<String, Object> response = new HashMap<>();
    // response.put("success", false);
    // response.put("error", e.getMessage());
    // return ResponseEntity.badRequest().body(response);
    // }
    // }

    // @GetMapping("/rules/station/{scaleStationName}")
    // public ResponseEntity<Map<String, Object>> getRulesByStation(@PathVariable
    // String scaleStationName) {
    // try {
    // List<DestinationScaleStation> rules =
    // scaleFeeService.getScaleFeeRulesForStation(scaleStationName);

    // Map<String, Object> response = new HashMap<>();
    // response.put("success", true);
    // response.put("scaleStation", scaleStationName);
    // response.put("rules", rules);
    // response.put("count", rules.size());

    // return ResponseEntity.ok(response);
    // } catch (Exception e) {
    // Map<String, Object> response = new HashMap<>();
    // response.put("success", false);
    // response.put("error", e.getMessage());
    // return ResponseEntity.badRequest().body(response);
    // }
    // }

    // @GetMapping("/check-highway/{scaleStationName}")
    // public ResponseEntity<Map<String, Object>> checkHighwayStation(@PathVariable
    // String scaleStationName) {
    // try {
    // boolean isHighway = scaleFeeService.isHighwayStation(scaleStationName);

    // Map<String, Object> response = new HashMap<>();
    // response.put("success", true);
    // response.put("scaleStation", scaleStationName);
    // response.put("isHighway", isHighway);

    // return ResponseEntity.ok(response);
    // } catch (Exception e) {
    // Map<String, Object> response = new HashMap<>();
    // response.put("success", false);
    // response.put("error", e.getMessage());
    // return ResponseEntity.badRequest().body(response);
    // }
    // }
}

class ScaleFeeRequest {
    private String licensePlate;
    private String scaleStationName;
    private BigDecimal totalWeight;

    // Getters and Setters
    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getScaleStationName() {
        return scaleStationName;
    }

    public void setScaleStationName(String scaleStationName) {
        this.scaleStationName = scaleStationName;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }
}

class BatchScaleFeeRequest {
    private List<String> licensePlates;
    private String scaleStationName;
    private BigDecimal totalWeight;

    // Getters and Setters
    public List<String> getLicensePlates() {
        return licensePlates;
    }

    public void setLicensePlates(List<String> licensePlates) {
        this.licensePlates = licensePlates;
    }

    public String getScaleStationName() {
        return scaleStationName;
    }

    public void setScaleStationName(String scaleStationName) {
        this.scaleStationName = scaleStationName;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }
}
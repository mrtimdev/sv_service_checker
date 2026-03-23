package timdev.timdev.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import timdev.timdev.entity.Truck;
import timdev.timdev.service.TruckService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trucks")
@CrossOrigin(origins = "*")
public class TruckApiController {

    @Autowired
    private TruckService truckService;

    @GetMapping("/route/{licensePlate}")
    public ResponseEntity<Map<String, Object>> getTruckRoute(@PathVariable String licensePlate) {
        try {
            Truck truck = truckService.findByLicensePlate(licensePlate).orElse(null);
            Map<String, Object> response = new HashMap<>();

            if (truck != null) {
                response.put("licensePlate", truck.getLicensePlate());
                response.put("routeNumber", truck.getRouteNumber());
                response.put("group", truck.getGroup());
                response.put("found", true);
            } else {
                response.put("found", false);
                response.put("licensePlate", licensePlate);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/batch-route")
    public ResponseEntity<Map<String, Object>> getBatchTruckRoutes(@RequestBody List<String> licensePlates) {
        try {
            Map<String, Object> results = new HashMap<>();

            for (String plate : licensePlates) {
                Truck truck = truckService.findByLicensePlate(plate).orElse(null);
                if (truck != null) {
                    Map<String, Object> truckInfo = new HashMap<>();
                    truckInfo.put("routeNumber", truck.getRouteNumber());
                    truckInfo.put("group", truck.getGroup());
                    results.put(plate, truckInfo);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
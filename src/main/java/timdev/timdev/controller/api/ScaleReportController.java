package timdev.timdev.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import timdev.timdev.entity.*;
import timdev.timdev.repository.*;
import timdev.timdev.service.DestinationSettingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/scale-reports")
public class ScaleReportController {

    @Autowired
    private TruckReportRepository truckReportRepository;

    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private ScaleStationRepository scaleStationRepository;

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private TruckReportScaleRepository truckReportScaleRepository;

    @Autowired
    private TruckReportPortRepository truckReportPortRepository;

    @Autowired
    private DestinationSettingService destinationSettingService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/submit")
    public ResponseEntity<?> submitReport(@RequestBody Map<String, Object> reportData,
            Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "anonymous";

            // Extract display data from the report
            List<Map<String, Object>> displayData = objectMapper.convertValue(
                    reportData.get("displayData"),
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            List<TruckReport> savedReports = new ArrayList<>();

            for (Map<String, Object> rowData : displayData) {
                TruckReport report = new TruckReport();
                DestinationSetting setting = null;
                if (rowData.get("destination") != null) {
                    setting = destinationSettingService.findByName(rowData.get("destination").toString());
                }

                // Set basic fields
                if (rowData.get("date") != null) {
                    String dateStr = rowData.get("date").toString();
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                        LocalDate date = LocalDate.parse(dateStr, formatter);
                        report.setReportDate(date);
                    } catch (Exception e) {
                        report.setReportDate(LocalDate.now());
                    }
                }

                // Find or create truck
                String licensePlate = (String) rowData.get("truckNo");
                Truck truck = truckRepository.findByLicensePlate(licensePlate)
                        .orElseGet(() -> {
                            Truck newTruck = new Truck();
                            newTruck.setLicensePlate(licensePlate);
                            return truckRepository.save(newTruck);
                        });
                report.setTruck(truck);

                // Set other fields
                report.setProductType((String) rowData.get("cargoType"));
                report.setLoadingLocation((String) rowData.get("pickup"));
                report.setDropLocation((String) rowData.get("dropoff"));
                report.setRoute(rowData.get("routeNumber") != null ? rowData.get("routeNumber").toString() : null);

                if (setting != null) {
                    report.setDestination(setting);
                    report.setTotalDestination(setting.getName());
                }

                // Set weights
                report.setCargoWeight(parseBigDecimal(rowData.get("cargoWeight")));
                report.setTruckWeight(parseBigDecimal(rowData.get("truckWeight")));
                report.setTotalWeight(parseBigDecimal(rowData.get("totalWeight")));

                // Set fees
                report.setTrafficPoliceFee(parseBigDecimal(rowData.get("policeValue")));
                report.setOtherExpense(parseBigDecimal(rowData.get("otherValue")));
                report.setOnHighway(rowData.get("nationalRoadValue").toString());
                report.setNote((String) rowData.get("markValue"));
                report.setOtherRemark((String) rowData.get("noteValue"));

                // Parse and set selected scales
                List<Object> scaleIdsObj = (List<Object>) rowData.get("scaleIds");
                Map<String, BigDecimal> scaleAmounts = objectMapper.convertValue(
                        rowData.get("scaleAmounts"),
                        new TypeReference<Map<String, BigDecimal>>() {
                        });

                if (scaleIdsObj != null && !scaleIdsObj.isEmpty()) {
                    // Convert IDs to String safely
                    List<String> scaleIds = scaleIdsObj.stream()
                            .map(id -> id != null ? id.toString() : null)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    // Calculate total scale fee
                    BigDecimal totalScaleFee = scaleIds.stream()
                            .map(id -> scaleAmounts.getOrDefault(id, BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    report.setScaleFee(totalScaleFee);

                    // Create scale relationships
                    for (String scaleId : scaleIds) {
                        scaleStationRepository.findById(Long.parseLong(scaleId)).ifPresent(scaleStation -> {
                            TruckReportScale reportScale = new TruckReportScale();
                            reportScale.setTruckReport(report);
                            reportScale.setScaleStation(scaleStation);
                            reportScale.setAmount(scaleAmounts.getOrDefault(scaleId, BigDecimal.ZERO));
                            report.addScale(reportScale);
                        });
                    }
                }

                // Parse and set selected ports
                List<Object> portIdsObj = (List<Object>) rowData.get("portIds");
                Map<String, BigDecimal> portAmounts = objectMapper.convertValue(
                        rowData.get("portAmounts"),
                        new TypeReference<Map<String, BigDecimal>>() {
                        });

                if (portIdsObj != null && !portIdsObj.isEmpty()) {
                    // Convert IDs to String safely
                    List<String> portIds = portIdsObj.stream()
                            .map(id -> id != null ? id.toString() : null)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    // Calculate total port fee
                    BigDecimal totalPortFee = portIds.stream()
                            .map(id -> portAmounts.getOrDefault(id, BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    report.setPortFee(totalPortFee);

                    // Create port relationships
                    for (String portId : portIds) {
                        portRepository.findById(Long.parseLong(portId)).ifPresent(port -> {
                            TruckReportPort reportPort = new TruckReportPort();
                            reportPort.setTruckReport(report);
                            reportPort.setPort(port);
                            reportPort.setAmount(portAmounts.getOrDefault(portId, BigDecimal.ZERO));
                            report.addPort(reportPort);
                        });
                    }
                }

                // Save the report (cascades to scales and ports)
                TruckReport savedReport = truckReportRepository.save(report);
                savedReports.add(savedReport);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Report submitted successfully",
                    "reportCount", savedReports.size(),
                    "reportIds", savedReports.stream().map(TruckReport::getId).collect(Collectors.toList())));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error submitting report: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "anonymous";

            // Find the report
            TruckReport report = truckReportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));

            // The relationships are already configured with CascadeType.ALL
            // This will automatically delete all related entities:
            // - TruckReportScale (scale relationships)
            // - TruckReportPort (port relationships)
            // Note: The Truck entity itself is NOT deleted (should use CascadeType.MERGE,
            // not ALL)

            // Log the deletion (optional)
            System.out.println("User " + username + " is deleting report ID: " + id);

            // Delete the report (cascades to child entities)
            truckReportRepository.delete(report);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Report deleted successfully",
                    "id", id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error deleting report: " + e.getMessage()));
        }
    }

    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteReports(@RequestBody List<Long> ids, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "anonymous";

            List<Long> deletedIds = new ArrayList<>();
            List<Long> notFoundIds = new ArrayList<>();

            for (Long id : ids) {
                try {
                    TruckReport report = truckReportRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Report not found"));

                    truckReportRepository.delete(report);
                    deletedIds.add(id);
                } catch (Exception e) {
                    notFoundIds.add(id);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Deleted " + deletedIds.size() + " reports",
                    "deletedIds", deletedIds,
                    "notFoundIds", notFoundIds));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error deleting reports: " + e.getMessage()));
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null)
            return BigDecimal.ZERO;
        try {
            String strValue = value.toString().replaceAll("[^0-9.-]", "");
            return new BigDecimal(strValue);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
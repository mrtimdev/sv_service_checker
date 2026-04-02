package timdev.timdev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timdev.timdev.entity.*;
import timdev.timdev.repository.*;

import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/scale-reports")
public class ScaleReportWebController {

    @Autowired
    private TruckReportRepository truckReportRepository;

    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private DestinationSettingRepository destinationRepository;

    @Autowired
    private ScaleStationRepository scaleStationRepository;

    @Autowired
    private PortRepository portRepository;

    @GetMapping
    public String listReports(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long truckId,
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) Long scaleStationId,
            @RequestParam(required = false) Long portId,
            @RequestParam(required = false) BigDecimal minScaleFee,
            @RequestParam(required = false) BigDecimal maxScaleFee,
            @RequestParam(required = false) BigDecimal minTotalWeight,
            @RequestParam(required = false) BigDecimal maxTotalWeight,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") String pageSize, // String to accept "all"
            @RequestParam(defaultValue = "reportDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        // Build specification with filters
        Specification<TruckReport> spec = buildSpecification(
                startDate, endDate, query, truckId, destinationId,
                scaleStationId, portId, minScaleFee, maxScaleFee,
                minTotalWeight, maxTotalWeight);

        // Get all filtered reports (for statistics)
        List<TruckReport> allFilteredReports = truckReportRepository.findAll(spec);

        // Handle pagination or "all"
        Page<TruckReport> reportPage;
        boolean isAllMode = "all".equalsIgnoreCase(pageSize);
        int actualPageSize = 50; // Default value
        int currentPageToUse = page;

        if (isAllMode) {
            // If "all" is selected, use the total count as page size
            actualPageSize = allFilteredReports.size();
            currentPageToUse = 0; // Always start from page 0 when in "all" mode

            // Create pageable to get all records
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(0, Math.max(actualPageSize, 1), sort);
            reportPage = truckReportRepository.findAll(spec, pageable);
        } else {
            // Parse the page size as integer
            try {
                actualPageSize = Integer.parseInt(pageSize);
                if (actualPageSize <= 0)
                    actualPageSize = 50;
            } catch (NumberFormatException e) {
                actualPageSize = 50; // Default if invalid
            }

            // Create pageable with sorting
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, actualPageSize, sort);
            reportPage = truckReportRepository.findAll(spec, pageable);
            currentPageToUse = page;
        }

        // Calculate summary statistics
        BigDecimal totalScaleFees = allFilteredReports.stream()
                .map(r -> r.getScaleFee() != null ? r.getScaleFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPortFees = allFilteredReports.stream()
                .map(r -> r.getPortFee() != null ? r.getPortFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOtherExpenses = allFilteredReports.stream()
                .map(r -> r.getOtherExpense() != null ? r.getOtherExpense() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWeight = allFilteredReports.stream()
                .map(r -> r.getTotalWeight() != null ? r.getTotalWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add attributes to model
        model.addAttribute("reports", reportPage.getContent());
        model.addAttribute("currentPage", currentPageToUse);
        model.addAttribute("totalPages", isAllMode ? 1 : reportPage.getTotalPages());
        model.addAttribute("totalItems", allFilteredReports.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("actualPageSize", actualPageSize);
        model.addAttribute("isAllMode", isAllMode);
        model.addAttribute("currentPageSize", actualPageSize); // Add this for display calculations

        // Filter values for form
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("query", query);
        model.addAttribute("truckId", truckId);
        model.addAttribute("destinationId", destinationId);
        model.addAttribute("scaleStationId", scaleStationId);
        model.addAttribute("portId", portId);
        model.addAttribute("minScaleFee", minScaleFee);
        model.addAttribute("maxScaleFee", maxScaleFee);
        model.addAttribute("minTotalWeight", minTotalWeight);
        model.addAttribute("maxTotalWeight", maxTotalWeight);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        // Statistics
        model.addAttribute("totalScaleFees", totalScaleFees);
        model.addAttribute("totalPortFees", totalPortFees);
        model.addAttribute("totalOtherExpenses", totalOtherExpenses);
        model.addAttribute("totalExpenses", totalScaleFees.add(totalPortFees).add(totalOtherExpenses));
        model.addAttribute("totalWeight", totalWeight);

        // Dropdown data
        model.addAttribute("trucks", truckRepository.findAll());
        model.addAttribute("destinations", destinationRepository.findAll());
        model.addAttribute("scaleStations", scaleStationRepository.findAll());
        model.addAttribute("ports", portRepository.findAll());

        // Page size options - now includes "all"
        model.addAttribute("pageSizes", new Object[] { 25, 50, 100, 200, 500, "all" });

        return "scale-reports/list";
    }

    // reports
    @GetMapping("/report")
    public String listReportsPage(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long truckId,
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) Long scaleStationId,
            @RequestParam(required = false) Long portId,
            @RequestParam(required = false) BigDecimal minScaleFee,
            @RequestParam(required = false) BigDecimal maxScaleFee,
            @RequestParam(required = false) BigDecimal minTotalWeight,
            @RequestParam(required = false) BigDecimal maxTotalWeight,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") String pageSize, // String to accept "all"
            @RequestParam(defaultValue = "reportDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        // Build specification with filters
        Specification<TruckReport> spec = buildSpecification(
                startDate, endDate, query, truckId, destinationId,
                scaleStationId, portId, minScaleFee, maxScaleFee,
                minTotalWeight, maxTotalWeight);

        // Get all filtered reports (for statistics)
        List<TruckReport> allFilteredReports = truckReportRepository.findAll(spec);

        // Handle pagination or "all"
        Page<TruckReport> reportPage;
        boolean isAllMode = "all".equalsIgnoreCase(pageSize);
        int actualPageSize = 50; // Default value
        int currentPageToUse = page;

        if (isAllMode) {
            // If "all" is selected, use the total count as page size
            actualPageSize = allFilteredReports.size();
            currentPageToUse = 0; // Always start from page 0 when in "all" mode

            // Create pageable to get all records
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(0, Math.max(actualPageSize, 1), sort);
            reportPage = truckReportRepository.findAll(spec, pageable);
        } else {
            // Parse the page size as integer
            try {
                actualPageSize = Integer.parseInt(pageSize);
                if (actualPageSize <= 0)
                    actualPageSize = 50;
            } catch (NumberFormatException e) {
                actualPageSize = 50; // Default if invalid
            }

            // Create pageable with sorting
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, actualPageSize, sort);
            reportPage = truckReportRepository.findAll(spec, pageable);
            currentPageToUse = page;
        }

        // Calculate summary statistics
        BigDecimal totalScaleFees = allFilteredReports.stream()
                .map(r -> r.getScaleFee() != null ? r.getScaleFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPortFees = allFilteredReports.stream()
                .map(r -> r.getPortFee() != null ? r.getPortFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOtherExpenses = allFilteredReports.stream()
                .map(r -> r.getOtherExpense() != null ? r.getOtherExpense() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWeight = allFilteredReports.stream()
                .map(r -> r.getTotalWeight() != null ? r.getTotalWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add attributes to model
        model.addAttribute("reports", reportPage.getContent());
        model.addAttribute("currentPage", currentPageToUse);
        model.addAttribute("totalPages", isAllMode ? 1 : reportPage.getTotalPages());
        model.addAttribute("totalItems", allFilteredReports.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("actualPageSize", actualPageSize);
        model.addAttribute("isAllMode", isAllMode);
        model.addAttribute("currentPageSize", actualPageSize); // Add this for display calculations

        // Filter values for form
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("query", query);
        model.addAttribute("truckId", truckId);
        model.addAttribute("destinationId", destinationId);
        model.addAttribute("scaleStationId", scaleStationId);
        model.addAttribute("portId", portId);
        model.addAttribute("minScaleFee", minScaleFee);
        model.addAttribute("maxScaleFee", maxScaleFee);
        model.addAttribute("minTotalWeight", minTotalWeight);
        model.addAttribute("maxTotalWeight", maxTotalWeight);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        // Statistics
        model.addAttribute("totalScaleFees", totalScaleFees);
        model.addAttribute("totalPortFees", totalPortFees);
        model.addAttribute("totalOtherExpenses", totalOtherExpenses);
        model.addAttribute("totalExpenses", totalScaleFees.add(totalPortFees).add(totalOtherExpenses));
        model.addAttribute("totalWeight", totalWeight);

        // Dropdown data
        model.addAttribute("trucks", truckRepository.findAll());
        model.addAttribute("destinations", destinationRepository.findAll());
        model.addAttribute("scaleStations", scaleStationRepository.findAll());
        model.addAttribute("ports", portRepository.findAll());

        // Page size options - now includes "all"
        model.addAttribute("pageSizes", new Object[] { 25, 50, 100, 200, 500, "all" });

        return "scale-reports/report";
    }

    @GetMapping("/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        TruckReport report = truckReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        model.addAttribute("report", report);
        model.addAttribute("selectedScales", report.getSelectedScales());
        model.addAttribute("selectedPorts", report.getSelectedPorts());

        return "scale-reports/view";
    }

    @GetMapping("/export")
    @ResponseBody
    public List<ExportReportDTO> exportReports(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long truckId,
            @RequestParam(required = false) Long destinationId) {

        Specification<TruckReport> spec = buildSpecification(
                startDate, endDate, query, truckId, destinationId,
                null, null, null, null, null, null);

        List<TruckReport> reports = truckReportRepository.findAll(spec, Sort.by("reportDate").descending());

        return reports.stream().map(report -> {
            ExportReportDTO dto = new ExportReportDTO();
            dto.setId(report.getId());
            dto.setReportDate(report.getReportDate());
            dto.setLicensePlate(report.getTruck() != null ? report.getTruck().getLicensePlate() : null);
            dto.setDestination(report.getTotalDestination());
            dto.setCargoWeight(report.getCargoWeight());
            dto.setTotalWeight(report.getTotalWeight());
            dto.setScaleFee(report.getScaleFee());
            dto.setPortFee(report.getPortFee());
            dto.setOtherExpense(report.getOtherExpense());
            dto.setTotalExpense(
                    report.getScaleFee() != null ? report.getScaleFee()
                            : BigDecimal.ZERO
                                    .add(report.getPortFee() != null ? report.getPortFee() : BigDecimal.ZERO)
                                    .add(report.getOtherExpense() != null ? report.getOtherExpense()
                                            : BigDecimal.ZERO));
            dto.setNote(report.getNote());

            // Get selected scale names
            if (report.getSelectedScales() != null) {
                dto.setSelectedScales(report.getSelectedScales().stream()
                        .map(ts -> ts.getScaleStation().getName())
                        .collect(Collectors.joining(", ")));
            }

            // Get selected port names
            if (report.getSelectedPorts() != null) {
                dto.setSelectedPorts(report.getSelectedPorts().stream()
                        .map(tp -> tp.getPort().getName())
                        .collect(Collectors.joining(", ")));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/statistics")
    @ResponseBody
    public StatisticsDTO getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        Specification<TruckReport> spec = buildSpecification(
                startDate, endDate, null, null, null, null, null, null, null, null, null);

        List<TruckReport> reports = truckReportRepository.findAll(spec);

        StatisticsDTO stats = new StatisticsDTO();
        stats.setTotalReports(reports.size());

        // Calculate totals
        BigDecimal totalScaleFees = BigDecimal.ZERO;
        BigDecimal totalPortFees = BigDecimal.ZERO;
        BigDecimal totalOtherExpenses = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (TruckReport report : reports) {
            totalScaleFees = totalScaleFees.add(report.getScaleFee() != null ? report.getScaleFee() : BigDecimal.ZERO);
            totalPortFees = totalPortFees.add(report.getPortFee() != null ? report.getPortFee() : BigDecimal.ZERO);
            totalOtherExpenses = totalOtherExpenses
                    .add(report.getOtherExpense() != null ? report.getOtherExpense() : BigDecimal.ZERO);
            totalWeight = totalWeight.add(report.getTotalWeight() != null ? report.getTotalWeight() : BigDecimal.ZERO);
        }

        stats.setTotalScaleFees(totalScaleFees);
        stats.setTotalPortFees(totalPortFees);
        stats.setTotalOtherExpenses(totalOtherExpenses);
        stats.setTotalExpenses(totalScaleFees.add(totalPortFees).add(totalOtherExpenses));
        stats.setTotalWeight(totalWeight);

        return stats;
    }

    private Specification<TruckReport> buildSpecification(
            LocalDate startDate, LocalDate endDate, String query, Long truckId, Long destinationId,
            Long scaleStationId, Long portId, BigDecimal minScaleFee, BigDecimal maxScaleFee,
            BigDecimal minTotalWeight, BigDecimal maxTotalWeight) {

        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Date range filter
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("reportDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("reportDate"), endDate));
            }

            // Search query (truck plate, destination, product type, loading/drop location)
            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.toLowerCase() + "%";

                // Join with truck for license plate search
                Join<TruckReport, Truck> truckJoin = root.join("truck", JoinType.LEFT);

                Predicate truckPlatePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(truckJoin.get("licensePlate")), searchPattern);

                Predicate destinationPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("totalDestination")), searchPattern);

                Predicate productTypePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("productType")), searchPattern);

                Predicate loadingLocationPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("loadingLocation")), searchPattern);

                Predicate dropLocationPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("dropLocation")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        truckPlatePredicate, destinationPredicate, productTypePredicate,
                        loadingLocationPredicate, dropLocationPredicate));
            }

            // Truck filter
            if (truckId != null) {
                Join<TruckReport, Truck> truckJoin = root.join("truck");
                predicates.add(criteriaBuilder.equal(truckJoin.get("id"), truckId));
            }

            // Destination filter
            if (destinationId != null) {
                Join<TruckReport, DestinationSetting> destJoin = root.join("destination");
                predicates.add(criteriaBuilder.equal(destJoin.get("id"), destinationId));
            }

            // Scale station filter (through selectedScales)
            if (scaleStationId != null) {
                Join<TruckReport, TruckReportScale> scalesJoin = root.join("selectedScales");
                Join<TruckReportScale, ScaleStation> scaleStationJoin = scalesJoin.join("scaleStation");
                predicates.add(criteriaBuilder.equal(scaleStationJoin.get("id"), scaleStationId));
            }

            // Port filter (through selectedPorts)
            if (portId != null) {
                Join<TruckReport, TruckReportPort> portsJoin = root.join("selectedPorts");
                Join<TruckReportPort, Port> portJoin = portsJoin.join("port");
                predicates.add(criteriaBuilder.equal(portJoin.get("id"), portId));
            }

            // Scale fee range filter
            if (minScaleFee != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("scaleFee"), minScaleFee));
            }
            if (maxScaleFee != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("scaleFee"), maxScaleFee));
            }

            // Total weight range filter
            if (minTotalWeight != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalWeight"), minTotalWeight));
            }
            if (maxTotalWeight != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalWeight"), maxTotalWeight));
            }

            // Ensure distinct results (important when joining collections)
            criteriaQuery.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // DTO classes for responses
    public static class ExportReportDTO {
        private Long id;
        private LocalDate reportDate;
        private String licensePlate;
        private String destination;
        private BigDecimal cargoWeight;
        private BigDecimal totalWeight;
        private BigDecimal scaleFee;
        private BigDecimal portFee;
        private BigDecimal otherExpense;
        private BigDecimal totalExpense;
        private String note;
        private String selectedScales;
        private String selectedPorts;

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public LocalDate getReportDate() {
            return reportDate;
        }

        public void setReportDate(LocalDate reportDate) {
            this.reportDate = reportDate;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public void setLicensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public BigDecimal getCargoWeight() {
            return cargoWeight;
        }

        public void setCargoWeight(BigDecimal cargoWeight) {
            this.cargoWeight = cargoWeight;
        }

        public BigDecimal getTotalWeight() {
            return totalWeight;
        }

        public void setTotalWeight(BigDecimal totalWeight) {
            this.totalWeight = totalWeight;
        }

        public BigDecimal getScaleFee() {
            return scaleFee;
        }

        public void setScaleFee(BigDecimal scaleFee) {
            this.scaleFee = scaleFee;
        }

        public BigDecimal getPortFee() {
            return portFee;
        }

        public void setPortFee(BigDecimal portFee) {
            this.portFee = portFee;
        }

        public BigDecimal getOtherExpense() {
            return otherExpense;
        }

        public void setOtherExpense(BigDecimal otherExpense) {
            this.otherExpense = otherExpense;
        }

        public BigDecimal getTotalExpense() {
            return totalExpense;
        }

        public void setTotalExpense(BigDecimal totalExpense) {
            this.totalExpense = totalExpense;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getSelectedScales() {
            return selectedScales;
        }

        public void setSelectedScales(String selectedScales) {
            this.selectedScales = selectedScales;
        }

        public String getSelectedPorts() {
            return selectedPorts;
        }

        public void setSelectedPorts(String selectedPorts) {
            this.selectedPorts = selectedPorts;
        }
    }

    public static class StatisticsDTO {
        private long totalReports;
        private BigDecimal totalScaleFees;
        private BigDecimal totalPortFees;
        private BigDecimal totalOtherExpenses;
        private BigDecimal totalExpenses;
        private BigDecimal totalWeight;

        // Getters and setters
        public long getTotalReports() {
            return totalReports;
        }

        public void setTotalReports(long totalReports) {
            this.totalReports = totalReports;
        }

        public BigDecimal getTotalScaleFees() {
            return totalScaleFees;
        }

        public void setTotalScaleFees(BigDecimal totalScaleFees) {
            this.totalScaleFees = totalScaleFees;
        }

        public BigDecimal getTotalPortFees() {
            return totalPortFees;
        }

        public void setTotalPortFees(BigDecimal totalPortFees) {
            this.totalPortFees = totalPortFees;
        }

        public BigDecimal getTotalOtherExpenses() {
            return totalOtherExpenses;
        }

        public void setTotalOtherExpenses(BigDecimal totalOtherExpenses) {
            this.totalOtherExpenses = totalOtherExpenses;
        }

        public BigDecimal getTotalExpenses() {
            return totalExpenses;
        }

        public void setTotalExpenses(BigDecimal totalExpenses) {
            this.totalExpenses = totalExpenses;
        }

        public BigDecimal getTotalWeight() {
            return totalWeight;
        }

        public void setTotalWeight(BigDecimal totalWeight) {
            this.totalWeight = totalWeight;
        }
    }
}
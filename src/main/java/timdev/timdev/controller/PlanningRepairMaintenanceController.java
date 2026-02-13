package timdev.timdev.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.PlanningRepairMaintenance;
import timdev.timdev.entity.Truck;
import timdev.timdev.enums.IntervalType;
import timdev.timdev.enums.PlanningStatus;
import timdev.timdev.repository.PlanningRepairMaintenanceRepository;
import timdev.timdev.repository.TruckRepository;
import timdev.timdev.service.PlanningRepairMaintenanceService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/maintenance")
public class PlanningRepairMaintenanceController {

    private final PlanningRepairMaintenanceRepository planningRepo;
    private final TruckRepository truckRepo;
    private final PlanningRepairMaintenanceService planningService;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "from", required = false) LocalDate from,
                       @RequestParam(value = "to", required = false) LocalDate to,
                       @RequestParam(value = "due", required = false) String due,
                       @RequestParam(value = "uptoToday", required = false) Boolean uptoToday,
                       Model model) {
        List<PlanningStatus> activeStatuses = List.of(PlanningStatus.PLANNED, PlanningStatus.IN_PROGRESS);
        LocalDate today = LocalDate.now();
        if (due != null && !due.isBlank()) {
            String key = due.trim().toUpperCase();
            if ("TODAY".equals(key)) {
                from = today;
                to = today;
            } else if ("WEEK".equals(key)) {
                from = today;
                to = today.plusDays(6);
            } else if ("MONTH".equals(key)) {
                from = today.withDayOfMonth(1);
                to = today.withDayOfMonth(today.lengthOfMonth());
            } else if ("OVERDUE".equals(key)) {
                from = null;
                to = today.minusDays(1);
            }
            uptoToday = null;
        }
        if (from == null && to == null) {
            from = today.withDayOfMonth(1);
            to = today.withDayOfMonth(today.lengthOfMonth());
        }
        if (Boolean.TRUE.equals(uptoToday)) {
            to = today;
        }
        List<PlanningRepairMaintenance> plans = planningRepo.searchActive(
            activeStatuses,
            q == null ? "" : q.trim(),
            from,
            to
        );
        model.addAttribute("plans", plans);
        model.addAttribute("q", q);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("due", due);
        model.addAttribute("uptoToday", uptoToday);
        model.addAttribute("today", today);

        LocalDate weekEnd = today.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        List<PlanningRepairMaintenance> dueToday = planningRepo
            .findByStatusInAndPlannedStartDateBetweenOrderByPlannedStartDateAsc(activeStatuses, today, today);
        List<PlanningRepairMaintenance> dueThisWeek = planningRepo
            .findByStatusInAndPlannedStartDateBetweenOrderByPlannedStartDateAsc(activeStatuses, today, weekEnd);
        List<PlanningRepairMaintenance> dueThisMonth = planningRepo
            .findByStatusInAndPlannedStartDateBetweenOrderByPlannedStartDateAsc(activeStatuses, monthStart, monthEnd);
        List<PlanningRepairMaintenance> overdue = planningRepo
            .findByStatusInAndPlannedStartDateBeforeOrderByPlannedStartDateAsc(activeStatuses, today);

        model.addAttribute("dueToday", dueToday);
        model.addAttribute("dueThisWeek", dueThisWeek);
        model.addAttribute("dueThisMonth", dueThisMonth);
        model.addAttribute("overdue", overdue);
        return "maintenance-list";
    }

    @GetMapping("/history")
    public String history(@RequestParam(value = "q", required = false) String q,
                          @RequestParam(value = "from", required = false) LocalDate from,
                          @RequestParam(value = "to", required = false) LocalDate to,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                          @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                          Model model) {
        List<PlanningStatus> statuses = resolveHistoryStatuses(status);
        Page<PlanningRepairMaintenance> plansPage = planningRepo.searchHistoryPage(
            statuses,
            q == null ? "" : q.trim(),
            from,
            to,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );
        model.addAttribute("plans", plansPage.getContent());
        model.addAttribute("page", plansPage);
        model.addAttribute("q", q);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("status", status);
        model.addAttribute("historyTotal", plansPage.getTotalElements());
        model.addAttribute("historyCompleted",
            plansPage.getContent().stream().filter(p -> p.getStatus() == PlanningStatus.COMPLETED).count());
        model.addAttribute("historyCancelled",
            plansPage.getContent().stream().filter(p -> p.getStatus() == PlanningStatus.CANCELLED).count());
        model.addAttribute("pageNumbers", buildPageNumbers(plansPage.getTotalPages(), plansPage.getNumber()));
        return "maintenance-history";
    }

    @GetMapping("/history/export")
    public ResponseEntity<String> exportHistory(@RequestParam(value = "q", required = false) String q,
                                                @RequestParam(value = "from", required = false) LocalDate from,
                                                @RequestParam(value = "to", required = false) LocalDate to,
                                                @RequestParam(value = "status", required = false) String status) {
        List<PlanningStatus> statuses = resolveHistoryStatuses(status);
        List<PlanningRepairMaintenance> plans = planningRepo.searchHistory(
            statuses,
            q == null ? "" : q.trim(),
            from,
            to
        );

        String csv = buildHistoryCsv(plans);
        String filename = "maintenance-history-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.TEXT_PLAIN)
            .body(csv);
    }

    @GetMapping("/history/export-xlsx")
    public ResponseEntity<byte[]> exportHistoryXlsx(@RequestParam(value = "q", required = false) String q,
                                                    @RequestParam(value = "from", required = false) LocalDate from,
                                                    @RequestParam(value = "to", required = false) LocalDate to,
                                                    @RequestParam(value = "status", required = false) String status) {
        List<PlanningStatus> statuses = resolveHistoryStatuses(status);
        List<PlanningRepairMaintenance> plans = planningRepo.searchHistory(
            statuses,
            q == null ? "" : q.trim(),
            from,
            to
        );

        byte[] bytes = buildHistoryXlsx(plans);
        String filename = "maintenance-history-" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bytes);
    }

    private List<PlanningStatus> resolveHistoryStatuses(String status) {
        List<PlanningStatus> statuses = new ArrayList<>();
        if ("COMPLETED".equalsIgnoreCase(status)) {
            statuses.add(PlanningStatus.COMPLETED);
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            statuses.add(PlanningStatus.CANCELLED);
        } else {
            statuses.add(PlanningStatus.COMPLETED);
            statuses.add(PlanningStatus.CANCELLED);
        }
        return statuses;
    }

    private String buildHistoryCsv(List<PlanningRepairMaintenance> plans) {
        StringBuilder sb = new StringBuilder();
        sb.append("planning_id,truck,plan_title,plan_task,actual_task,note,planned_start,planned_end,planned_duration,")
          .append("actual_start,actual_end,interval_type,interval_value,status,created_at,updated_at\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (PlanningRepairMaintenance p : plans) {
            sb.append(csv(p.getPlanningId()))
              .append(",").append(csv(p.getTruck() != null ? p.getTruck().getLicensePlate() : null))
              .append(",").append(csv(p.getPlanTitle()))
              .append(",").append(csv(p.getPlanTaskText()))
              .append(",").append(csv(p.getActualTaskText()))
              .append(",").append(csv(p.getNoteText()))
              .append(",").append(csv(p.getPlannedStartDate() != null ? p.getPlannedStartDate().format(df) : null))
              .append(",").append(csv(p.getPlannedEndDate() != null ? p.getPlannedEndDate().format(df) : null))
              .append(",").append(csv(p.getPlannedDurationDays()))
              .append(",").append(csv(p.getActualStartDate() != null ? p.getActualStartDate().format(dtf) : null))
              .append(",").append(csv(p.getActualEndDate() != null ? p.getActualEndDate().format(dtf) : null))
              .append(",").append(csv(p.getIntervalType() != null ? p.getIntervalType().name() : null))
              .append(",").append(csv(p.getIntervalValue()))
              .append(",").append(csv(p.getStatus() != null ? p.getStatus().name() : null))
              .append(",").append(csv(p.getCreatedAt() != null ? p.getCreatedAt().format(dtf) : null))
              .append(",").append(csv(p.getUpdatedAt() != null ? p.getUpdatedAt().format(dtf) : null))
              .append("\n");
        }
        return sb.toString();
    }

    private byte[] buildHistoryXlsx(List<PlanningRepairMaintenance> plans) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("History");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            String[] cols = {
                "planning_id", "truck", "plan_title", "plan_task", "actual_task", "note",
                "planned_start", "planned_end", "planned_duration", "actual_start", "actual_end",
                "interval_type", "interval_value", "status", "created_at", "updated_at"
            };
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            for (PlanningRepairMaintenance p : plans) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                row.createCell(c++).setCellValue(value(p.getPlanningId()));
                row.createCell(c++).setCellValue(value(p.getTruck() != null ? p.getTruck().getLicensePlate() : null));
                row.createCell(c++).setCellValue(value(p.getPlanTitle()));
                row.createCell(c++).setCellValue(value(p.getPlanTaskText()));
                row.createCell(c++).setCellValue(value(p.getActualTaskText()));
                row.createCell(c++).setCellValue(value(p.getNoteText()));
                row.createCell(c++).setCellValue(value(p.getPlannedStartDate() != null ? p.getPlannedStartDate().format(df) : null));
                row.createCell(c++).setCellValue(value(p.getPlannedEndDate() != null ? p.getPlannedEndDate().format(df) : null));
                row.createCell(c++).setCellValue(value(p.getPlannedDurationDays()));
                row.createCell(c++).setCellValue(value(p.getActualStartDate() != null ? p.getActualStartDate().format(dtf) : null));
                row.createCell(c++).setCellValue(value(p.getActualEndDate() != null ? p.getActualEndDate().format(dtf) : null));
                row.createCell(c++).setCellValue(value(p.getIntervalType() != null ? p.getIntervalType().name() : null));
                row.createCell(c++).setCellValue(value(p.getIntervalValue()));
                row.createCell(c++).setCellValue(value(p.getStatus() != null ? p.getStatus().name() : null));
                row.createCell(c++).setCellValue(value(p.getCreatedAt() != null ? p.getCreatedAt().format(dtf) : null));
                row.createCell(c++).setCellValue(value(p.getUpdatedAt() != null ? p.getUpdatedAt().format(dtf) : null));
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private String value(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private List<Integer> buildPageNumbers(int totalPages, int currentPage) {
        List<Integer> pages = new ArrayList<>();
        if (totalPages <= 1) return pages;
        int start = Math.max(0, currentPage - 2);
        int end = Math.min(totalPages - 1, currentPage + 2);
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }
    private String csv(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        PlanningRepairMaintenance plan = new PlanningRepairMaintenance();
        plan.setPlanTitle("Annual PM Inspection");
        model.addAttribute("plan", plan);
        model.addAttribute("trucks", truckRepo.findAll());
        model.addAttribute("intervalTypes", IntervalType.values());
        model.addAttribute("statuses", PlanningStatus.values());
        return "maintenance-form";
    }

    @GetMapping("/bulk")
    public String bulkForm(Model model) {
        PlanningRepairMaintenance plan = new PlanningRepairMaintenance();
        plan.setPlanTitle("Annual PM Inspection");
        model.addAttribute("plan", plan);
        List<PlanningStatus> activeStatuses = List.of(PlanningStatus.PLANNED, PlanningStatus.IN_PROGRESS);
        model.addAttribute("availableTrucks", truckRepo.findWithoutActivePlans(activeStatuses));
        model.addAttribute("activePlans", planningRepo.findByStatusInOrderByPlannedStartDateAsc(activeStatuses));
        return "maintenance-bulk";
    }

    @GetMapping("/import")
    public String importForm() {
        return "maintenance-import";
    }

    @PostMapping("/import")
    public String importPlans(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please choose an Excel (.xlsx) file to import.");
            return "redirect:/maintenance/import";
        }

        int created = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                String licensePlate = getCellString(row.getCell(0));
                if (licensePlate == null || licensePlate.isBlank()) {
                    skipped++;
                    errors.add("Row " + (i + 1) + ": missing truck_license_plate");
                    continue;
                }

                Truck truck = truckRepo.findByLicensePlate(licensePlate.trim()).orElse(null);
                if (truck == null) {
                    skipped++;
                    errors.add("Row " + (i + 1) + ": truck not found (" + licensePlate + ")");
                    continue;
                }

                String planTitle = getCellString(row.getCell(1));
                String planTask = getCellString(row.getCell(2));
                LocalDate plannedStart = safeParseExcelDate(row.getCell(3));
                LocalDate plannedEnd = safeParseExcelDate(row.getCell(4));
                IntervalType intervalType = parseIntervalType(getCellString(row.getCell(5)));
                Integer intervalValue = parseIntegerCell(row.getCell(6));
                String note = getCellString(row.getCell(7));

                if (plannedStart == null) {
                    skipped++;
                    errors.add("Row " + (i + 1) + ": planned_start_date is required");
                    continue;
                }

                PlanningRepairMaintenance plan = new PlanningRepairMaintenance();
                plan.setTruck(truck);
                plan.setPlanTitle(planTitle);
                plan.setPlanTaskText(planTask);
                plan.setPlannedStartDate(plannedStart);
                plan.setPlannedEndDate(plannedEnd);
                plan.setIntervalType(intervalType);
                plan.setIntervalValue(intervalValue);
                plan.setNoteText(note);
                try {
                    planningService.createPlanning(plan);
                    created++;
                } catch (Exception e) {
                    skipped++;
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to import file: " + e.getMessage());
            return "redirect:/maintenance/import";
        }

        String message = "Imported " + created + " plan(s).";
        if (skipped > 0) {
            message += " Skipped " + skipped + " row(s).";
            message += " " + String.join(" | ", errors.subList(0, Math.min(errors.size(), 5)));
            if (errors.size() > 5) {
                message += " ...";
            }
        }
        redirectAttributes.addFlashAttribute("success", message);
        return "redirect:/maintenance/import";
    }

    @PostMapping("/import/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateImport(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> invalidRows = new ArrayList<>();
        int totalRows = 0;
        int missingTruck = 0;
        int truckNotFound = 0;
        int missingStart = 0;
        int invalidDate = 0;
        int invalidIntervalType = 0;
        int duplicateActive = 0;
        List<PlanningStatus> activeStatuses = List.of(PlanningStatus.PLANNED, PlanningStatus.IN_PROGRESS);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                totalRows++;
                List<String> reasons = new ArrayList<>();

                String licensePlate = getCellString(row.getCell(0));
                Truck truck = null;
                if (licensePlate == null || licensePlate.isBlank()) {
                    missingTruck++;
                    reasons.add("Missing truck");
                } else {
                    truck = truckRepo.findByLicensePlate(licensePlate.trim()).orElse(null);
                    if (truck == null) {
                        truckNotFound++;
                        reasons.add("Truck not found");
                    }
                }

                LocalDate plannedStart = safeParseExcelDate(row.getCell(3));
                if (plannedStart == null) {
                    String rawStart = getCellString(row.getCell(3));
                    if (rawStart == null || rawStart.isBlank()) {
                        missingStart++;
                        reasons.add("Missing planned start date");
                    } else {
                        invalidDate++;
                        reasons.add("Invalid planned start date");
                    }
                }

                String intervalRaw = getCellString(row.getCell(5));
                IntervalType intervalType = parseIntervalType(intervalRaw);
                if (intervalRaw != null && !intervalRaw.isBlank() && intervalType == null) {
                    invalidIntervalType++;
                    reasons.add("Invalid interval type");
                }

                if (truck != null) {
                    boolean hasActive = planningRepo.existsByTruck_IdAndStatusIn(truck.getId(), activeStatuses);
                    if (hasActive) {
                        duplicateActive++;
                        reasons.add("Duplicate active PM plan");
                    }
                }

                if (!reasons.isEmpty()) {
                    Map<String, Object> rowError = new HashMap<>();
                    rowError.put("row", i + 1);
                    rowError.put("reasons", reasons);
                    invalidRows.add(rowError);
                }
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }

        result.put("totalRows", totalRows);
        result.put("missingTruck", missingTruck);
        result.put("truckNotFound", truckNotFound);
        result.put("missingStart", missingStart);
        result.put("invalidDate", invalidDate);
        result.put("invalidIntervalType", invalidIntervalType);
        result.put("duplicateActive", duplicateActive);
        result.put("invalidRows", invalidRows);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("PM Template");
            String[] columns = {
                "truck_license_plate", "plan_title", "plan_task", "planned_start_date",
                "planned_end_date", "interval_type", "interval_value", "note"
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("3B-6180");
            sample.createCell(1).setCellValue("Annual PM Inspection");
            sample.createCell(2).setCellValue("General Service");
            sample.createCell(3).setCellValue("12-FEB-2026");
            sample.createCell(4).setCellValue("19-FEB-2026");
            sample.createCell(5).setCellValue("YEAR");
            sample.createCell(6).setCellValue(1);
            sample.createCell(7).setCellValue("Optional note");

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            byte[] bytes = out.toByteArray();

            String filename = "pm_planning_template.xlsx";
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new byte[0]);
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i <= 7; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            if (value == Math.floor(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }

    private Integer parseIntegerCell(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }
            String s = getCellString(cell);
            if (s == null || s.isBlank()) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private IntervalType parseIntervalType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return IntervalType.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate safeParseExcelDate(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            if (cell.getCellType() == CellType.STRING) {
                String dateStr = cell.getStringCellValue().trim();
                if (dateStr.isEmpty()) return null;
                return parseDateString(dateStr);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseDateString(String dateStr) {
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-uuuu")
                .toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-uu")
                .toFormatter(Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/uuuu"),
            DateTimeFormatter.ofPattern("MM/dd/uuuu")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    @PostMapping("/save")
    public String save(@RequestParam("truckId") Long truckId,
                       PlanningRepairMaintenance plan,
                       RedirectAttributes redirectAttributes) {
        Truck truck = truckRepo.findById(truckId)
            .orElseThrow(() -> new RuntimeException("Truck not found"));
        plan.setTruck(truck);
        planningService.createPlanning(plan);
        redirectAttributes.addFlashAttribute("success", "Planning created");
        return "redirect:/maintenance";
    }

    @PostMapping("/bulk")
    public String bulkCreate(@RequestParam("truckIds") List<Long> truckIds,
                             PlanningRepairMaintenance plan,
                             RedirectAttributes redirectAttributes) {
        int created = planningService.bulkCreate(truckIds, plan);
        redirectAttributes.addFlashAttribute("success", "Bulk created " + created + " plans");
        return "redirect:/maintenance";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        PlanningRepairMaintenance plan = planningRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Planning not found"));
        model.addAttribute("plan", plan);
        model.addAttribute("trucks", truckRepo.findAll());
        model.addAttribute("intervalTypes", IntervalType.values());
        model.addAttribute("statuses", PlanningStatus.values());
        return "maintenance-form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam("truckId") Long truckId,
                         PlanningRepairMaintenance plan,
                         RedirectAttributes redirectAttributes) {
        Truck truck = truckRepo.findById(truckId)
            .orElseThrow(() -> new RuntimeException("Truck not found"));
        plan.setTruck(truck);
        planningService.updatePlanning(id, plan);
        redirectAttributes.addFlashAttribute("success", "Planning updated");
        return "redirect:/maintenance";
    }

    @PostMapping("/start/{id}")
    public String start(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        planningService.startRepair(id);
        redirectAttributes.addFlashAttribute("success", "Repair started");
        return "redirect:/maintenance";
    }

    @PostMapping("/complete/{id}")
    public String complete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        planningService.completeRepair(id);
        redirectAttributes.addFlashAttribute("success", "Repair completed");
        return "redirect:/maintenance";
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        planningService.cancelPlanning(id);
        redirectAttributes.addFlashAttribute("success", "Planning cancelled");
        return "redirect:/maintenance";
    }
}

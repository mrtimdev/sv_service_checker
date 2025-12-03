package timdev.timdev.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.ApproveStatus;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.FuelRequestRequestDTO;
import timdev.timdev.entity.CompanyTruck;
import timdev.timdev.entity.FuelRequest;
import timdev.timdev.entity.SubTruck;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.service.FuelRequestService;
import timdev.timdev.service.NotificationService;
import timdev.timdev.service.PusherBeamsService;
import timdev.timdev.service.TruckService;
import timdev.timdev.service.UserService;

@AllArgsConstructor
@Controller
@RequestMapping("/fuel-requests")
public class FuelRequestController {

    private FuelRequestService service;
    private TruckService truckService;
    private UserService userService;

    @Autowired
    private PusherBeamsService beamsService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping({"", "/reports"})
    public Object list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "truckOwner", required = false) String truckOwner,
            @RequestParam(value = "requester", required = false) String requester,
            @RequestParam(value = "purpose", required = false) String purpose,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "truckId", required = false) Long truckId,
            @RequestParam(value = "status", required = false) ApproveStatus status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
            @RequestParam(value = "export", required = false) String export,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            HttpServletResponse response
    ) throws IOException {

        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }
        
        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        // Fetch filtered list
        Page<FuelRequest> pageResult = service.findFiltered(page, size, requester, position, purpose ,truckId, status, startDate, endDate, null);

        // Export to Excel
        if (export != null && export.equalsIgnoreCase("excel")) {
            exportToExcel(pageResult.getContent(), response);
            return null;
        }

        model.addAttribute("fuelRequests", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("trucks", truckService.getAll());
        
        model.addAttribute("licensePlate", licensePlate);
        model.addAttribute("truckOwner", truckOwner);
        model.addAttribute("requester", requester);
        model.addAttribute("position", position);
        model.addAttribute("purpose", purpose);
        model.addAttribute("truckId", truckId != null ? truckId : null);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ApproveStatus.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        model.addAttribute("pageSizeNumber", size);

        return "fuel-requests/index";
    }

    

    // --- Create Form ---
    @GetMapping("/form")
    public String createForm(@RequestParam(name = "truck_id", required = false) Long truckId, Model model) {
        FuelRequestRequestDTO fuelRequest = new FuelRequestRequestDTO();
        if (truckId != null) {
            fuelRequest.setTruckId(truckId);
        }

        model.addAttribute("truckDto", fuelRequest);
        model.addAttribute("selectedTruckId", truckId);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("fuelRequest", fuelRequest);
        model.addAttribute("statuses", ApproveStatus.values());
        return "fuel-requests/form";
    }

    // --- Edit Form ---
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        FuelRequest fuelRequest = service.findById(id);
        if (fuelRequest == null) {
            redirectAttributes.addFlashAttribute("error", "FuelRequest not found!");
            return "redirect:/fuel-requests";
        }

        FuelRequestRequestDTO dto = service.convertToDto(fuelRequest);

        model.addAttribute("truckDto", dto);
        model.addAttribute("selectedTruckId", dto.getTruckId());
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", fuelRequest.getDate());
        model.addAttribute("fuelRequest", dto);
        model.addAttribute("statuses", ApproveStatus.values());
        return "fuel-requests/form";
    }

    @GetMapping("/clone/{id}")
    public String cloneForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        FuelRequest fuelRequest = service.findById(id);
        if (fuelRequest == null) {
            redirectAttributes.addFlashAttribute("error", "FuelRequest not found!");
            return "redirect:/fuel-requests";
        }

        FuelRequestRequestDTO dto = service.convertToDto(fuelRequest);

        dto.setId(null);

        model.addAttribute("truckDto", dto);
        model.addAttribute("selectedTruckId", null);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", fuelRequest.getDate());
        model.addAttribute("fuelRequest", dto);
        model.addAttribute("isClone", true);
        model.addAttribute("statuses", ApproveStatus.values());
        return "fuel-requests/form";
    }

    

    @PostMapping("/save")
    public String save(
            @ModelAttribute FuelRequestRequestDTO fuelRequest,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam(value = "action", required = false) String action
    ) {
        if (result.hasErrors()) {
            model.addAttribute("truckDto", fuelRequest);
            model.addAttribute("selectedTruckId", fuelRequest.getTruckId());
            model.addAttribute("trucks", truckService.getAll());
            model.addAttribute("currentDate", LocalDate.now());
            model.addAttribute("fuelRequest", fuelRequest);
            model.addAttribute("statuses", ApproveStatus.values());
            return "fuel-requests/form";
        }

        Truck truck = null;

        if (fuelRequest.getTruckId() != null) {
            truck = truckService.findById(fuelRequest.getTruckId()).orElse(null);
        }

        // ------------------------------
        //   EXIST CHECK ONLY WHEN TRUCK EXISTS
        // ------------------------------
        if (truck != null) {
            boolean exists = service.isExistsByTruckAndDate(truck, fuelRequest.getDate());

            if (exists) {
                FuelRequest existing = service.findByTruckAndDate(truck, fuelRequest.getDate());

                boolean isNew = fuelRequest.getId() == null;
                if (isNew || !existing.getId().equals(fuelRequest.getId())) {

                    redirectAttributes.addFlashAttribute(
                            "error",
                            "ឡានលេខ " + truck.getLicensePlate() +
                            " មានរបាយការណ៍សម្រាប់ថ្ងៃ " +
                            fuelRequest.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                            " រួចហើយ!"
                    );

                    if (isNew) {
                        return "redirect:/fuel-requests/form?truckId=" + truck.getId();
                    } else {
                        return "redirect:/fuel-requests/edit/" + fuelRequest.getId() +
                                "?truckId=" + truck.getId();
                    }
                }
            }
        }

        try {
            boolean isNew = fuelRequest.getId() == null;
            service.saveFromDto(fuelRequest);

            String plate = (truck != null) ? truck.getLicensePlate() : "No Truck";

            if (isNew) {
                redirectAttributes.addFlashAttribute("success", "Fuel Request successfully created!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Fuel Request successfully updated!");
            }

            String act = (action != null) ? action : "default";

            switch (act) {
                case "submit" -> {
                    redirectAttributes.addFlashAttribute("success",
                            " Fuel Request submitted successfully.");
                    return "redirect:/fuel-requests";
                }
                case "save_continue" -> {
                    redirectAttributes.addFlashAttribute("success",
                            " Fuel Request saved. Continue more entry.");
                    return "redirect:/fuel-requests/form";
                }
                case "clone" -> {
                    redirectAttributes.addFlashAttribute("success",
                            " cloned successfully.");
                    return "redirect:/fuel-requests";
                }
                default -> {
                    redirectAttributes.addFlashAttribute("success",
                            " Fuel Request saved successfully.");
                    return "redirect:/fuel-requests";
                }
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving FuelRequest: " + e.getMessage());
            return "redirect:/fuel-requests";
        }
    }



    // user owner record view
    @GetMapping("/own-record")
    public Object listByUser(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "truckOwner", required = false) String truckOwner,
            @RequestParam(value = "requester", required = false) String requester,
            @RequestParam(value = "purpose", required = false) String purpose,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "truckId", required = false) Long truckId,
            @RequestParam(value = "status", required = false) ApproveStatus status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
            @RequestParam(value = "export", required = false) String export,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {

        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }
        
        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        User user = currentUser.getUser();

        // Fetch filtered list
        Page<FuelRequest> pageResult = service.findFiltered(page, size, requester, position, purpose ,truckId, status, startDate, endDate, user.getId());

        // Export to Excel
        if (export != null && export.equalsIgnoreCase("excel")) {
            exportToExcel(pageResult.getContent(), response);
            return null;
        }

        model.addAttribute("fuelRequests", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("trucks", truckService.getAll());
        
        model.addAttribute("licensePlate", licensePlate);
        model.addAttribute("truckOwner", truckOwner);
        model.addAttribute("requester", requester);
        model.addAttribute("position", position);
        model.addAttribute("purpose", purpose);
        model.addAttribute("truckId", truckId != null ? truckId : null);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ApproveStatus.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        return "fuel-requests/index_by_user";
    }

    

    // --- Create Form ---
    @GetMapping("/own-record/form")
    public String createFormByUser(@RequestParam(name = "truck_id", required = false) Long truckId, Model model) {
        FuelRequestRequestDTO fuelRequest = new FuelRequestRequestDTO();
        if (truckId != null) {
            fuelRequest.setTruckId(truckId);
        }

        model.addAttribute("truckDto", fuelRequest);
        model.addAttribute("selectedTruckId", truckId);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("fuelRequest", fuelRequest);
        model.addAttribute("statuses", ApproveStatus.values());
        return "fuel-requests/form_by_user";
    }

    // --- Edit Form ---
    @GetMapping("/own-record/edit/{id}")
    public String editFormByUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        FuelRequest fuelRequest = service.findById(id);
        if (fuelRequest == null) {
            redirectAttributes.addFlashAttribute("error", "FuelRequest not found!");
            return "redirect:/fuel-requests/own-record";
        }

        FuelRequestRequestDTO dto = service.convertToDto(fuelRequest);

        model.addAttribute("truckDto", dto);
        model.addAttribute("selectedTruckId", dto.getTruckId());
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", fuelRequest.getDate());
        model.addAttribute("fuelRequest", dto);
        model.addAttribute("statuses", ApproveStatus.values());
        return "fuel-requests/form_by_user";
    }

    @GetMapping("/own-record/clone/{id}")
    public String cloneFormByUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        FuelRequest fuelRequest = service.findById(id);
        if (fuelRequest == null) {
            redirectAttributes.addFlashAttribute("error", "FuelRequest not found!");
            return "redirect:/fuel-requests/own-record";
        }

        FuelRequestRequestDTO dto = service.convertToDto(fuelRequest);

        dto.setId(null);

        model.addAttribute("truckDto", dto);
        model.addAttribute("selectedTruckId", null);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", fuelRequest.getDate());
        model.addAttribute("fuelRequest", dto);
        model.addAttribute("isClone", true);
        model.addAttribute("statuses", ApproveStatus.values());
        return "fuel-requests/form_by_user";
    }

    

    @PostMapping("/own-record/save")
    public String saveByUser(
            @ModelAttribute FuelRequestRequestDTO fuelRequest,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam(value = "action", required = false) String action
    ) {
        if (result.hasErrors()) {
            model.addAttribute("truckDto", fuelRequest);
            model.addAttribute("selectedTruckId", fuelRequest.getTruckId());
            model.addAttribute("trucks", truckService.getAll());
            model.addAttribute("currentDate", LocalDate.now());
            model.addAttribute("fuelRequest", fuelRequest);
            model.addAttribute("statuses", ApproveStatus.values());
            return "fuel-requests/form_by_user";
        }

        Truck truck = null;

        if (fuelRequest.getTruckId() != null) {
            truck = truckService.findById(fuelRequest.getTruckId()).orElse(null);
        }

        // ------------------------------
        //   EXIST CHECK ONLY WHEN TRUCK EXISTS
        // ------------------------------
        if (truck != null) {
            boolean exists = service.isExistsByTruckAndDate(truck, fuelRequest.getDate());

            if (exists) {
                FuelRequest existing = service.findByTruckAndDate(truck, fuelRequest.getDate());

                boolean isNew = fuelRequest.getId() == null;
                if (isNew || !existing.getId().equals(fuelRequest.getId())) {

                    redirectAttributes.addFlashAttribute(
                            "error",
                            "ឡានលេខ " + truck.getLicensePlate() +
                            " មានរបាយការណ៍សម្រាប់ថ្ងៃ " +
                            fuelRequest.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                            " រួចហើយ!"
                    );

                    if (isNew) {
                        return "redirect:/fuel-requests/own-record/form?truckId=" + truck.getId();
                    } else {
                        return "redirect:/fuel-requests/own-record/edit/" + fuelRequest.getId() +
                                "?truckId=" + truck.getId();
                    }
                }
            }
        }

        try {
            boolean isNew = fuelRequest.getId() == null;
            service.saveFromDto(fuelRequest);

            String plate = (truck != null) ? truck.getLicensePlate() : "No Truck";

            if (isNew) {
                redirectAttributes.addFlashAttribute("success", "Fuel Request successfully created!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Fuel Request successfully updated!");
            }

            String act = (action != null) ? action : "default";

            switch (act) {
                case "submit" -> {
                    redirectAttributes.addFlashAttribute("success",
                            " Fuel Request submitted successfully.");
                    return "redirect:/fuel-requests/own-record";
                }
                case "save_continue" -> {
                    redirectAttributes.addFlashAttribute("success",
                            " Fuel Request saved. Continue more entry.");
                    return "redirect:/fuel-requests/own-record/form";
                }
                case "clone" -> {
                    redirectAttributes.addFlashAttribute("success",
                            " cloned successfully.");
                    return "redirect:/fuel-requests/own-record";
                }
                default -> {
                    redirectAttributes.addFlashAttribute("success",
                            " Fuel Request saved successfully.");
                    return "redirect:/fuel-requests/own-record";
                }
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving FuelRequest: " + e.getMessage());
            return "redirect:/fuel-requests/own-record";
        }
    }


    // --- Delete ---
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        FuelRequest fuelRequest = service.findById(id);
        if (fuelRequest == null) {
            redirectAttributes.addFlashAttribute("error", "FuelRequest not found!");
            return "redirect:/fuel-requests";
        } else {
            if(ApproveStatus.APPROVED.equals(fuelRequest.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Can not delete with Approved status!");
                return "redirect:/fuel-requests";
            }
        }
        service.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "FuelRequest successfully deleted!");
        return "redirect:/fuel-requests";
    }

    // --- Approve / Reject ---
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id, @RequestParam("status") ApproveStatus status, RedirectAttributes redirectAttributes) {
        FuelRequest fuelRequest = service.findById(id);
        if (fuelRequest == null) {
            redirectAttributes.addFlashAttribute("error", "FuelRequest not found!");
            return "redirect:/fuel-requests";
        }
        User currentUser = userService.getCurrentUser();
        fuelRequest.setStatus(status);
        fuelRequest.setApprovedAt(LocalDateTime.now());
        fuelRequest.setApprovedBy(currentUser);
        service.update(fuelRequest); 
        redirectAttributes.addFlashAttribute("success", "Status changed to "+ status);

        // beamsService.sendToInterest(
        //         "HELLO",
        //         "🚀 Test Notification",
        //         "Your Spring Boot + Pusher Beams setup works!");

        // notificationService.sendNotification("Fuel updated for truck ID: " + id);

        return "redirect:/fuel-requests";
    }

    // excel
    private void exportToExcel(List<FuelRequest> list, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Fuel-Requests.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fuel Requests");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle pendingStyle = createStatusStyle(workbook, IndexedColors.YELLOW1);
            CellStyle approvedStyle = createStatusStyle(workbook, IndexedColors.GREEN);
            CellStyle defaultStyle = createDefaultStyle(workbook);

            // Create header row
            Row header = sheet.createRow(0);
            String[] columns = {"No", "Date", "Requester", "Position", "Oil Qty", "Status", "Approved By", "Approved At", "Purpose",  "Note"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Create data rows
            int rowIdx = 1;
            for (int i = 0; i < list.size(); i++) {
                FuelRequest st = list.get(i);
                Row row = sheet.createRow(rowIdx++);

                // No
                createCell(row, 0, i + 1, defaultStyle);

                // Date
                if (st.getDate() != null) {
                    createCell(row, 1, st.getDate(), dateStyle);
                } else {
                    createCell(row, 1, "", defaultStyle);
                }

                // License Plate
                createCell(row, 2, st.getRequester(), defaultStyle);

                // Truck Owner
                createCell(row, 3, st.getPosition(), defaultStyle);

                // Oil Qty
                if (st.getOilsQuantity() != null) {
                    createCell(row, 4, st.getOilsQuantity(), numberStyle);
                } else {
                    createCell(row, 4, 0.0, numberStyle);
                }

                // Status with conditional styling
                Cell statusCell = row.createCell(5);
                statusCell.setCellValue(st.getStatus().name());
                if (st.getStatus() == ApproveStatus.PENDING) {
                    statusCell.setCellStyle(pendingStyle);
                } else {
                    statusCell.setCellStyle(approvedStyle);
                }

                // Approved By
                createCell(row, 6, st.getApprovedBy() != null ? st.getApprovedBy().fullName() : "", defaultStyle);

                // Approved At
                if (st.getApprovedAt() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
                    String formatted = st.getApprovedAt().format(formatter);

                    createCell(row, 7, formatted, dateStyle);
                } else {
                    createCell(row, 7, "", defaultStyle);
                }

                // Note
                createCell(row, 8, st.getPurpose() != null ? st.getPurpose() : "", defaultStyle);

                createCell(row, 9, st.getNote() != null ? st.getNote() : "", defaultStyle);
            }

            // Auto-size all columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add some additional formatting
            sheet.setColumnWidth(8, 50 * 256); 
            sheet.setColumnWidth(9, 50 * 256); 

            workbook.write(response.getOutputStream());
        }
    }

    // Helper method to create cells with proper typing
    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof LocalDate) {
            cell.setCellValue((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue((LocalDateTime) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }
        
        cell.setCellStyle(style);
    }

    // Style creation methods
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Background color
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        
        // Alignment
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Data format
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("MMM dd, yyyy"));
        
        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Data format
        style.setDataFormat((short) BuiltinFormats.getBuiltinFormat("#,##0.00"));
        
        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Alignment
        style.setAlignment(HorizontalAlignment.RIGHT);
        
        return style;
    }

    private CellStyle createStatusStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        
        // Background color
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Font
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        // Alignment
        style.setAlignment(HorizontalAlignment.CENTER);
        
        return style;
    }

    private CellStyle createDefaultStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }



    // for user update fuel quantity
    @GetMapping({"/user-record"})
    public Object listApprovedForUser(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "truckOwner", required = false) String truckOwner,
            @RequestParam(value = "requester", required = false) String requester,
            @RequestParam(value = "purpose", required = false) String purpose,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "truckId", required = false) Long truckId,
            @RequestParam(value = "status", required = false) ApproveStatus status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
            @RequestParam(value = "export", required = false) String export,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            HttpServletResponse response
    ) throws IOException {

        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }
        
        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        status = ApproveStatus.APPROVED;

        // Fetch filtered list
        Page<FuelRequest> pageResult = service.findFiltered(page, size, requester, position, purpose ,truckId, status, startDate, endDate, null);

        // Export to Excel
        if (export != null && export.equalsIgnoreCase("excel")) {
            exportToExcel(pageResult.getContent(), response);
            return null;
        }

        model.addAttribute("fuelRequests", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("trucks", truckService.getAll());
        
        model.addAttribute("licensePlate", licensePlate);
        model.addAttribute("truckOwner", truckOwner);
        model.addAttribute("requester", requester);
        model.addAttribute("position", position);
        model.addAttribute("purpose", purpose);
        model.addAttribute("truckId", truckId != null ? truckId : null);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ApproveStatus.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        return "fuel-requests/index_for_user";
    }

    @GetMapping("/update-fuel-quantity")
    public String changeOilStatus(
            @RequestParam("id") Long id,
            @RequestParam("backUrl") String backUrl,
            @RequestParam(required = true) Double oilsQuantity,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        FuelRequest fuelRequest = service.findById(id);
        if (fuelRequest == null) {
            redirectAttributes.addFlashAttribute("error", "Request not found");
            return "redirect:"+backUrl;
        }
        User user = userDetails.getUser();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String dateString = fuelRequest.getDate().format(formatter);

        fuelRequest.setOilsQuantity(oilsQuantity);
        fuelRequest.setChangedAt(LocalDateTime.now());
        fuelRequest.setChangedBy(user);

        service.save(fuelRequest);

        redirectAttributes.addFlashAttribute("success", "Record on "+ dateString+ ", " + fuelRequest.getRequester() + " fuel quantity updated to "+ oilsQuantity);
        
        return "redirect:"+backUrl;
    }
}

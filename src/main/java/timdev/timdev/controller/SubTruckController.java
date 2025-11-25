package timdev.timdev.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
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
import timdev.timdev.dto.RequestStatus;
import timdev.timdev.dto.Status;
import timdev.timdev.dto.SubTruckRequestDTO;
import timdev.timdev.entity.CompanyTruck;
import timdev.timdev.entity.SubTruck;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.service.SubTruckService;
import timdev.timdev.service.TruckService;
import timdev.timdev.service.UserService;

@AllArgsConstructor
@Controller
@RequestMapping("/sub-trucks")
public class SubTruckController {

    private SubTruckService service;
    private TruckService truckService;
    private UserService userService;

    @GetMapping({"", "/reports"})
    public Object list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "truckOwner", required = false) String truckOwner,
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
        Page<SubTruck> pageResult = service.findFiltered(page, size, licensePlate, truckOwner, truckId, status, startDate, endDate);

        // Export to Excel
        if (export != null && export.equalsIgnoreCase("excel")) {
            exportToExcel(pageResult.getContent(), response);
            return null;
        }

        model.addAttribute("subTrucks", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("trucks", truckService.getAll());
        
        model.addAttribute("licensePlate", licensePlate);
        model.addAttribute("truckOwner", truckOwner);
        model.addAttribute("truckId", truckId != null ? truckId : null);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ApproveStatus.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        return "sub-trucks/index";
    }

    

    // --- Create Form ---
    @GetMapping("/form")
    public String createForm(@RequestParam(name = "truck_id", required = false) Long truckId, Model model, Authentication authentication) {
        SubTruckRequestDTO subTruck = new SubTruckRequestDTO();
        if (truckId != null) {
            subTruck.setTruckId(truckId);
        }

        boolean isUser = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        if (isUser) {
            // Same as !hasRole('USER')
            return "settings";
        }

        model.addAttribute("truckDto", subTruck);
        model.addAttribute("selectedTruckId", truckId);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("subTruck", subTruck);
        model.addAttribute("statuses", ApproveStatus.values());
        return "sub-trucks/form";
    }

    // --- Edit Form ---
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        SubTruck subTruck = service.findById(id);
        if (subTruck == null) {
            redirectAttributes.addFlashAttribute("error", "SubTruck not found!");
            return "redirect:/sub-trucks";
        }

        SubTruckRequestDTO dto = service.convertToDto(subTruck);

        model.addAttribute("truckDto", dto);
        model.addAttribute("selectedTruckId", dto.getTruckId());
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", subTruck.getDate());
        model.addAttribute("subTruck", dto);
        model.addAttribute("statuses", ApproveStatus.values());
        return "sub-trucks/form";
    }

    @GetMapping("/clone/{id}")
    public String cloneForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        SubTruck subTruck = service.findById(id);
        if (subTruck == null) {
            redirectAttributes.addFlashAttribute("error", "SubTruck not found!");
            return "redirect:/sub-trucks";
        }

        SubTruckRequestDTO dto = service.convertToDto(subTruck);

        dto.setId(null);

        model.addAttribute("truckDto", dto);
        model.addAttribute("selectedTruckId", dto.getTruckId());
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", subTruck.getDate());
        model.addAttribute("subTruck", dto);
        model.addAttribute("isClone", true);
        model.addAttribute("statuses", ApproveStatus.values());
        return "sub-trucks/form";
    }

    // --- Save / Update ---
    @PostMapping("/save")
    public String save(@ModelAttribute SubTruckRequestDTO subTruck, 
        BindingResult result, Model model, 
        RedirectAttributes redirectAttributes,
        @RequestParam(value = "action", required = false) String action
    ) {
        if (result.hasErrors()) {
            model.addAttribute("truckDto", subTruck);
            model.addAttribute("selectedTruckId", subTruck.getTruckId());
            model.addAttribute("trucks", truckService.getAll());
            model.addAttribute("currentDate", LocalDate.now());
            model.addAttribute("subTruck", subTruck);
            model.addAttribute("statuses", ApproveStatus.values());
            return "sub-trucks/form";
        }
        

        Truck truck = truckService.findById(subTruck.getTruckId()).orElse(null);

        boolean exists = service.isExistsByTruckAndDate(truck, subTruck.getDate());

        if (exists) {
            SubTruck existing = service.findByTruckAndDate(truck, subTruck.getDate());

            boolean isNew = subTruck.getId() == null;

            // If creating a new one OR editing but another record exists for same date
            if (isNew || !existing.getId().equals(subTruck.getId())) {

                redirectAttributes.addFlashAttribute(
                    "error",
                    "ឡានលេខ " + truck.getLicensePlate() + 
                    " មានរបាយការណ៍សម្រាប់ថ្ងៃ " +
                    subTruck.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                    " រួចហើយ!"
                );

                if (isNew) {
                    // redirect to CREATE form (no id)
                    return "redirect:/sub-trucks/form?truckId=" + truck.getId();
                } else {
                    // redirect to EDIT (has id)
                    return "redirect:/sub-trucks/edit/" + subTruck.getId() +
                        "?truckId=" + truck.getId();
                }
            }
        }


        if (truck == null) {
            redirectAttributes.addFlashAttribute("error", "Truck not found.!");
            return "redirect:/sub-trucks";
        }

        try {
            boolean isNew = subTruck.getId() == null; 
            service.saveFromDto(subTruck);

            if (isNew) {
                redirectAttributes.addFlashAttribute("success", "Sub Truck successfully created!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Sub Truck successfully updated!");
            }

            String act = (action != null) ? action : "default";

            switch (act) {
                case "submit" -> {
                    redirectAttributes.addFlashAttribute("success",
                            truck.getLicensePlate() + " Sub Truck submitted successfully.");
                    return "redirect:/sub-trucks";
                }
                case "save_continue" -> {
                    redirectAttributes.addFlashAttribute("success",
                            truck.getLicensePlate() + " Sub Truck saved. Continue more entry.");
                    return "redirect:/sub-trucks/form";
                }
                case "clone" -> {
                    redirectAttributes.addFlashAttribute("success",
                            truck.getLicensePlate() + " cloned successfully.");
                    return "redirect:/sub-trucks";
                }
                default -> {
                    redirectAttributes.addFlashAttribute("success",
                            truck.getLicensePlate() + " Sub Truck saved successfully.");
                    return "redirect:/sub-trucks";
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving SubTruck: " + e.getMessage());
            return "redirect:/sub-trucks";
        }

    }

    // --- Delete ---
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        SubTruck subTruck = service.findById(id);
        if (subTruck == null) {
            redirectAttributes.addFlashAttribute("error", "SubTruck not found!");
            return "redirect:/sub-trucks";
        } else {
            if(ApproveStatus.APPROVED.equals(subTruck.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Can not delete with Approved status!");
                return "redirect:/sub-trucks";
            }
        }
        service.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "SubTruck successfully deleted!");
        return "redirect:/sub-trucks";
    }

    // --- Approve / Reject ---
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id, @RequestParam("status") ApproveStatus status, RedirectAttributes redirectAttributes) {
        SubTruck subTruck = service.findById(id);
        if (subTruck == null) {
            redirectAttributes.addFlashAttribute("error", "SubTruck not found!");
            return "redirect:/sub-trucks";
        }

        User currentUser = userService.getCurrentUser();
        subTruck.setStatus(status);
        subTruck.setApprovedAt(LocalDateTime.now());
        subTruck.setApprovedBy(currentUser);
        service.update(subTruck); 
        redirectAttributes.addFlashAttribute("success", "Status changed to "+ status);
        return "redirect:/sub-trucks";
    }

    // excel
    private void exportToExcel(List<SubTruck> list, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=sub_trucks.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sub Trucks");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle pendingStyle = createStatusStyle(workbook, IndexedColors.YELLOW1);
            CellStyle approvedStyle = createStatusStyle(workbook, IndexedColors.GREEN);
            CellStyle defaultStyle = createDefaultStyle(workbook);

            // Create header row
            Row header = sheet.createRow(0);
            String[] columns = {"No", "Date", "License Plate", "Truck Owner", "Oil Qty", "Status", "Approved By", "Approved At", "Note"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Create data rows
            int rowIdx = 1;
            for (int i = 0; i < list.size(); i++) {
                SubTruck st = list.get(i);
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
                createCell(row, 2, st.getTruck() != null ? st.getTruck().getLicensePlate() : "", defaultStyle);

                // Truck Owner
                createCell(row, 3, st.getTruckOwner(), defaultStyle);

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
                createCell(row, 6, st.getApprovedBy() != null ? st.getApprovedBy() : "", defaultStyle);

                // Approved At
                if (st.getApprovedAt() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
                    String formatted = st.getApprovedAt().format(formatter);
                    createCell(row, 7, formatted, dateStyle);
                } else {
                    createCell(row, 7, "", defaultStyle);
                }

                // Note
                createCell(row, 8, st.getNote() != null ? st.getNote() : "", defaultStyle);
            }

            // Auto-size all columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add some additional formatting
            sheet.setColumnWidth(8, 50 * 256); // Make note column wider

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

    // for users if admin approved
    @GetMapping({"/user-record"})
    public Object listApprovedForUser(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "truckOwner", required = false) String truckOwner,
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

        if (status == null) {
            status = ApproveStatus.APPROVED;
        }

        // Fetch filtered list
        Page<SubTruck> pageResult = service.findFiltered(page, size, licensePlate, truckOwner, truckId, status, startDate, endDate);

        // Export to Excel
        if (export != null && export.equalsIgnoreCase("excel")) {
            exportToExcel(pageResult.getContent(), response);
            return null;
        }

        model.addAttribute("subTrucks", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("trucks", truckService.getAll());
        
        model.addAttribute("licensePlate", licensePlate);
        model.addAttribute("truckOwner", truckOwner);
        model.addAttribute("truckId", truckId != null ? truckId : null);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ApproveStatus.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        return "sub-trucks/index_for_user";
    }


    @GetMapping("/update-fuel-quantity")
    public String changeOilStatus(
            @RequestParam("id") Long id,
            @RequestParam("backUrl") String backUrl,
            @RequestParam(required = true) Double oilsQuantity,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        SubTruck subTruck = service.findById(id);
        if (subTruck == null) {
            redirectAttributes.addFlashAttribute("error", "Sub Truck not found");
            return "redirect:"+backUrl;
        }
        User user = userDetails.getUser();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String dateString = subTruck.getDate().format(formatter);

        subTruck.setOilsQuantity(oilsQuantity);
        subTruck.setChangedAt(LocalDateTime.now());
        subTruck.setChangedBy(user);

        service.save(subTruck);

        redirectAttributes.addFlashAttribute("success", "Record on "+ dateString+ ", " + subTruck.getTruck().getLicensePlate() + " fuel quantity updated to "+ oilsQuantity);
        
        return "redirect:"+backUrl;
    }




}

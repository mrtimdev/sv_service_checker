package timdev.timdev.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.SubTruckRequestDTO;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckDistance;
import timdev.timdev.entity.User;
import timdev.timdev.service.DestinationSettingService;
import timdev.timdev.service.PermissionChecker;


@RequestMapping("/destination-settings")
@AllArgsConstructor
@Controller
public class DestinationSettingController {
    
    private DestinationSettingService service;

    private PermissionChecker permissionChecker;





    // @PreAuthorize("hasRole('ADMIN') or hasAuthority('DESTINATION_SETTINGS')")
    // @GetMapping
    // public Object index(
    //     @RequestParam(value = "search", defaultValue = "") String search,
    //     @RequestParam(value = "page", defaultValue = "0") int page,
    //     @RequestParam(value = "size", defaultValue = "200") String sizeParam,
    //     @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
    //     @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
    //     @RequestParam(value = "order", defaultValue = "desc") String order,
    //     @RequestParam(value = "export", required = false) String export,
    //     HttpServletResponse response,
    //     Model model) throws IOException 
    // {
    //     List<DestinationSetting> destinationPage;
    //     int totalPages = 1;
    //     int size;
        
    //     if ("all".equalsIgnoreCase(sizeParam)) {
    //         size = Integer.MAX_VALUE;
    //     } else {
    //         size = Integer.parseInt(sizeParam); 
    //     }

    //     Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

    //     String sortField;
    //     sortField = switch (sortBy) {
    //         case "code" -> "code";
    //         case "id" -> "id";
    //         case "name" -> "name";
    //         case "distance" -> "distance";
    //         default -> "id";
    //     };

    //     Sort sort = Sort.by(direction, sortField);
        
    //     if (showAll) {
    //         destinationPage = service.getAllFiltered(search, sort);
    //     } else {
    //         Pageable pageable = PageRequest.of(page, size, sort);
    //         Page<DestinationSetting> withPage = service.getAllWithPageable(search, pageable, sort);
    //         destinationPage = withPage.getContent();
    //         totalPages = withPage.getTotalPages();
    //     }

    //     // put everything into model
    //     model.addAttribute("data", destinationPage);
    //     model.addAttribute("totalElements", destinationPage.getTotalElements()); 
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", totalPages);
    //     model.addAttribute("pageSize", sizeParam);
    //     model.addAttribute("pageSizeNumber", size);
    //     model.addAttribute("search", search);

    //     model.addAttribute("showAll", showAll);

    //     model.addAttribute("sortBy", sortBy);
    //     model.addAttribute("order", order);

    //     return "destination-settings/index";
    // }


    private long calculateEndIndex(int currentPage, int pageSizeNumber, long totalElements) {
        long endIndex = (long) currentPage * pageSizeNumber + pageSizeNumber;
        return Math.min(endIndex, totalElements);
    }


    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DESTINATION_SETTINGS')")
    @GetMapping
    public Object index(
        @RequestParam(value = "search", defaultValue = "") String search,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "200") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "sortBy", defaultValue = "code") String sortBy,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        HttpServletRequest request,
        Model model) throws IOException 
    {
        // Handle export request first
        if ("selected".equals(export)) {
            return handleExportSelected(request, response);
        } else if ("all".equals(export)) {
            return handleExportAll(response);
        }
        
        List<DestinationSetting> destinationPage;
        int totalPages = 1;
        long totalElements = 0;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        String sortField;
        sortField = switch (sortBy) {
            case "code" -> "code";
            case "id" -> "id";
            case "name" -> "name";
            case "distance" -> "distance";
            default -> "id";
        };


        Sort sort = Sort.by(direction, sortField);
        
        if (showAll) {
            destinationPage = service.getAllFiltered(search, sort);
            totalElements = destinationPage.size();
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DestinationSetting> withPage = service.getAllWithPageable(search, pageable, sort);
            destinationPage = withPage.getContent();
            totalPages = withPage.getTotalPages();
            totalElements = withPage.getTotalElements(); 
        }
        
        // put everything into model
        model.addAttribute("data", destinationPage);
        model.addAttribute("totalElements", totalElements); 
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("search", search);
        model.addAttribute("showAll", showAll);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        model.addAttribute("endIndex", calculateEndIndex(page, size, totalElements));
        model.addAttribute("startIndex", (long) page * size + 1);

        return "destination-settings/index";
    }

    // Export selected items
    @PostMapping("/export-selected")
    @ResponseBody
    public ResponseEntity<?> exportSelected(@RequestParam("selectedIds") String selectedIds, HttpServletResponse response) {
        try {
            List<Long> ids = Arrays.stream(selectedIds.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            List<DestinationSetting> destinations = service.findAllById(ids);
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Selected Destinations");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Code", "Destination", "Distance", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Fill data
            int rowNum = 1;
            for (DestinationSetting destination : destinations) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(destination.getId());
                row.createCell(1).setCellValue(destination.getCode());
                row.createCell(2).setCellValue(destination.getName());
                row.createCell(3).setCellValue(destination.getDistance());
                
                row.createCell(5).setCellValue(destination.getCreatedAt() != null ? 
                    destination.getCreatedAt().toString() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to response
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] bytes = outputStream.toByteArray();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=selected_destinations.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error exporting data: " + e.getMessage());
        }
    }

    // Export all items
    @GetMapping("/export-all")
    public void exportAll(HttpServletResponse response) throws IOException {
        List<DestinationSetting> destinations = service.getAllFiltered("", Sort.by("id").ascending());
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=all_destinations.xlsx");
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("All Destinations");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Code", "Destination", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // Fill data
        int rowNum = 1;
        for (DestinationSetting destination : destinations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(destination.getId());
            row.createCell(1).setCellValue(destination.getCode());
            row.createCell(2).setCellValue(destination.getName());
            row.createCell(3).setCellValue(destination.getDistance());
            row.createCell(5).setCellValue(destination.getCreatedAt() != null ? 
                destination.getCreatedAt().toString() : "");
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    
    // Private helper methods for export
    private ResponseEntity<?> handleExportSelected(HttpServletRequest request, HttpServletResponse response) {
        String selectedIds = request.getParameter("selectedIds");
        if (selectedIds == null || selectedIds.isEmpty()) {
            return ResponseEntity.badRequest().body("No items selected for export");
        }
        
        // You can reuse the exportSelected method logic here
        // For simplicity, I'll redirect to the POST method
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Use POST method for export");
    }

    private ResponseEntity<?> handleExportAll(HttpServletResponse response) throws IOException {
        exportAll(response);
        return null; // Response already handled
    }



    @GetMapping("/form")
    public String create(Model model, RedirectAttributes redirectAttributes) {
        if (!permissionChecker.has("DESTINATION_SETTINGS_CREATE")) {
            redirectAttributes.addFlashAttribute("error", "Oop!, You do not have permission to create a new destinations setting.");
            return "redirect:/destination-settings";
        }

        model.addAttribute("destination", new DestinationSetting());
        return "destination-settings/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        DestinationSetting setting = service.findById(id);
        if(setting != null && !setting.getDestinations().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Destination Setting " + setting.getCode() +" and " + setting.getName() +" can not editable .!");
            return "redirect:/destination-settings";
        }
        model.addAttribute("destination", setting);
        return "destination-settings/form";
    }


    @PostMapping("/store")
    public String store(
            @ModelAttribute DestinationSetting destination,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        DestinationSetting existingData = null;
        User user = userDetails.getUser();

        if (destination.getId() != null) {
            existingData = service.findById(destination.getId());
            existingData.setUpdatedBy(user);
        } 

        // ✅ Check duplicate code
        DestinationSetting byCode = service.findByCode(destination.getCode());
        if (byCode != null && (existingData == null || !byCode.getId().equals(existingData.getId()))) {
            redirectAttributes.addFlashAttribute("error", "Destination code already exists!");
            redirectAttributes.addFlashAttribute("destination", destination);
            return "redirect:/destination-settings/form";
        }

        // ✅ Check duplicate name
        DestinationSetting byName = service.findByName(destination.getName());
        if (byName != null && (existingData == null || !byName.getId().equals(existingData.getId()))) {
            redirectAttributes.addFlashAttribute("error", "Destination name already exists!");
            redirectAttributes.addFlashAttribute("destination", destination);
            return "redirect:/destination-settings/form";
        }

        DestinationSetting ds = (existingData != null) ? existingData : new DestinationSetting();

        ds.setCode(destination.getCode());
        ds.setName(destination.getName());
        ds.setDistance(destination.getDistance());

        ds.setCreatedBy(user);

        service.save(ds);

        redirectAttributes.addFlashAttribute("success", "Destination saved successfully!");
        return "redirect:/destination-settings";
    }



    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        DestinationSetting setting = service.findById(id);
        if(setting != null && !setting.getDestinations().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Destination Setting " + setting.getCode() +" and " + setting.getName() +" can not deletable .!");
            return "redirect:/destination-settings";
        } else {
            service.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Destination Setting " + setting.getCode() +" and " + setting.getName() +" successfully deleted.!");
        }
        
        return "redirect:/destination-settings";
    }



}

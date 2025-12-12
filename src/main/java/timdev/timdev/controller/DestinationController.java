package timdev.timdev.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.CompanyTruckRequestDTO;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.ExcelImportResult;
import timdev.timdev.dto.Status;
import timdev.timdev.dto.SubTruckRequestDTO;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckDistance;
import timdev.timdev.service.DestinationService;
import timdev.timdev.service.DestinationSettingService;
import timdev.timdev.service.TruckService;


@RequestMapping("/destinations")
@AllArgsConstructor
@Controller
public class DestinationController {
    
    private DestinationService service;
    private DestinationSettingService destinationSettingService;
    private TruckService truckService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Object index(
        @RequestParam(value = "query", defaultValue = "") String query,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "200") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes,
        Model model) throws IOException 
    {
        List<Destination> destinationPage;
        int totalPages = 1;
        
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/destinations";
        }

        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

        // build Sort dynamically
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // map frontend sortBy values to entity fields
        String sortField;
        switch (sortBy) {
            case "code":
                sortField = "code"; 
                break;
            case "id":
                sortField = "id";
                break;
            case "name":
                sortField = "name";
                break;
            case "distance":
                sortField = "distance";
                break;
            default:
                sortField = "id";
                break;
        }

        Sort sort = Sort.by(direction, sortField);
        destinationPage = service.findByFilterQueriesWithList(query, startDate, endDate, sort);
        
        if (showAll) {
            // fetch all reports with filter
            destinationPage = service.getAllFiltered(query, sort);
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Destination> withPage = service.findByFilterQueriesWithPage(query, startDate, endDate, pageable);
            destinationPage = withPage.getContent();
            totalPages = withPage.getTotalPages();
        }

        if ("excel".equalsIgnoreCase(export)) {
            service.exportExcel(destinationPage, response);
            return null;
        }
        if ("excel-for-company-truck".equalsIgnoreCase(export)) {
            service.exportExcelForImportCompanyTruck(destinationPage, response);
            return null;
        }

        // put everything into model
        model.addAttribute("data", destinationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("query", query);

        model.addAttribute("showAll", showAll);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "destinations/index";
    }

    // own record for user destinations
    @PreAuthorize("hasAuthority('DESTINATION_VIEW')")
    @GetMapping({"u", "/own-records"})
    public Object indexOwnRecords(
        @RequestParam(value = "query", defaultValue = "") String query,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "200") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes,
        Model model) throws IOException 
    {
        List<Destination> destinationPage;
        int totalPages = 1;
        
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/u";
        }

        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

        // build Sort dynamically
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // map frontend sortBy values to entity fields
        String sortField;
        switch (sortBy) {
            case "code":
                sortField = "code"; 
                break;
            case "id":
                sortField = "id";
                break;
            case "name":
                sortField = "name";
                break;
            case "distance":
                sortField = "distance";
                break;
            default:
                sortField = "id";
                break;
        }

        List<Status> statuses = List.of(Status.PENDING);
        Sort sort = Sort.by(direction, sortField);
        destinationPage = service.findByFilterQueriesAndSortWithStatus(startDate, endDate, query, statuses, sort);
        
        if (showAll) {
            // fetch all reports with filter
            destinationPage = service.getAllFiltered(query, sort);
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Destination> withPage = service.findByFilterQueriesWithStatusAndPage(query, startDate, endDate, pageable, statuses);
            destinationPage = withPage.getContent();
            totalPages = withPage.getTotalPages();
        }

        if ("excel".equalsIgnoreCase(export)) {
            service.exportExcel(destinationPage, response);
            return null;
        }

        // put everything into model
        model.addAttribute("data", destinationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("query", query);

        model.addAttribute("showAll", showAll);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "destinations/index_own_records";
    }

    @PreAuthorize("hasAuthority('DESTINATION_VIEW')")
    @GetMapping({"u/report", "/own-records/report"})
    public Object indexOwnRecordsReport(
        @RequestParam(value = "query", defaultValue = "") String query,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "200") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes,
        Model model) throws IOException 
    {
        List<Destination> destinationPage;
        int totalPages = 1;
        
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/u/report";
        }

        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

        // build Sort dynamically
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // map frontend sortBy values to entity fields
        String sortField;
        switch (sortBy) {
            case "code":
                sortField = "code"; 
                break;
            case "id":
                sortField = "id";
                break;
            case "name":
                sortField = "name";
                break;
            case "distance":
                sortField = "distance";
                break;
            default:
                sortField = "id";
                break;
        }

        List<Status> statuses = List.of(Status.PENDING, Status.COMPLETED);
        Sort sort = Sort.by(direction, sortField);
        destinationPage = service.findByFilterQueriesAndSortWithStatus(startDate, endDate, query, statuses, sort);
        
        if (showAll) {
            // fetch all reports with filter
            destinationPage = service.getAllFiltered(query, sort);
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Destination> withPage = service.findByFilterQueriesWithStatusAndPage(query, startDate, endDate, pageable, statuses);
            destinationPage = withPage.getContent();
            totalPages = withPage.getTotalPages();
        }

        if ("excel".equalsIgnoreCase(export)) {
            service.exportExcel(destinationPage, response);
            return null;
        }

        // put everything into model
        model.addAttribute("data", destinationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("query", query);

        model.addAttribute("showAll", showAll);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "destinations/index_own_records_report";
    }



    @GetMapping("/form")
    public String create(Model model) {
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("destination", new Destination());
        return "destinations/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Destination destination = service.findById(id);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("destination", destination);
        model.addAttribute("currentDate", destination.getDate());
        return "destinations/form";
    }


    @PostMapping("/store")
    public String store(
            @ModelAttribute Destination destination,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {

        Destination existingData = null;

        if (destination.getId() != null) {
            existingData = service.findById(destination.getId());
        }

        DestinationSetting setting = destinationSettingService.findByCode(destination.getCode());
        if (setting == null) {
            redirectAttributes.addFlashAttribute("error", "Destination Code '" + destination.getCode() + "' not found in Destination Settings");
            redirectAttributes.addFlashAttribute("destination", destination);
            model.addAttribute("trucks", truckService.getAll());
            return "redirect:/destinations/form";
        }
        Truck truck = destination.getTruck();
        // Validate distance against DestinationSettings
        if (destination.getDistance() > setting.getDistance()) {
            String message = truck.getLicensePlate() + " Distance " + destination.getDistance() + 
                            " exceeds maximum allowed distance " + setting.getDistance() + 
                            " for destination '" + destination.getCode() + "'";
            redirectAttributes.addFlashAttribute("error", message);
            redirectAttributes.addFlashAttribute("destination", destination);
            model.addAttribute("trucks", truckService.getAll());
            return "redirect:/destinations/form";
        }

        Destination ds = (existingData != null) ? existingData : new Destination();

        ds.setDate(destination.getDate());
        ds.setCode(destination.getCode());
        ds.setName(destination.getName());
        ds.setDistance(destination.getDistance());
        ds.setTruck(destination.getTruck());

        service.save(ds);

        redirectAttributes.addFlashAttribute("success", "Destination saved successfully!");
        return "redirect:/destinations";
    }



    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Truck truck = truckService.findById(id).orElse(null);
        service.deleteById(id);
        redirectAttributes.addFlashAttribute("success", truck.getLicensePlate()+ ": Destination deleted successfully!");
        return "redirect:/destinations";
    }
    @GetMapping("/u/delete/{id}")
    public String deleteByUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Truck truck = truckService.findById(id).orElse(null);
        service.deleteById(id);
        redirectAttributes.addFlashAttribute("success", truck.getLicensePlate()+ ": Destination deleted successfully!");
        return "redirect:/destinations/u";
    }


    @GetMapping("/import")
    public String showImportForm() {
        return "destinations/import"; 
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Validate file type
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to import");
            return "redirect:/destinations/import";
        }

        if (!file.getOriginalFilename().endsWith(".xlsx") &&
            !file.getOriginalFilename().endsWith(".xls")) {

            redirectAttributes.addFlashAttribute("error",
                    "Please upload an Excel file (.xlsx or .xls)");
            return "redirect:/destinations/import";
        }

        try {
            // Step 1: Read Excel (includes excel errors)
            ExcelImportResult excelResult = service.readExcelFile(file);

            // Step 2: Validate & Save to DB (NO error merging)
            List<String> dbErrors = service.validateAndSaveDestinations(excelResult, userDetails);

            int totalRows = excelResult.getDestinations().size();
            int successCount = totalRows - dbErrors.size();
            if(successCount > 0) {
                redirectAttributes.addFlashAttribute("success",
                    "Imported successfully: " + successCount + " rows");
            }
            

            // Collect Excel + DB errors separately (NOT merged)
            List<String> allErrors = new ArrayList<>();
            allErrors.addAll(excelResult.getErrorMessages()); // Excel errors
            allErrors.addAll(dbErrors);                       // DB errors only

            if (!allErrors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorCount", allErrors.size());
                redirectAttributes.addFlashAttribute("errorDetails", allErrors);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Import failed: " + e.getMessage());
        }

        return "redirect:/destinations/import";
    }




    
}

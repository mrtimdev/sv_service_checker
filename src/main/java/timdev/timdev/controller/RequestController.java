package timdev.timdev.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.entity.Request;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.enums.RoleType;
import timdev.timdev.service.RequestService;

@Controller
@RequestMapping("/requests")
public class RequestController {
    
    @Autowired
    private RequestService requestService;

    @GetMapping()
    public String index(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return "redirect:/requests/list";
    }

    @GetMapping("/list")
    public String listRequests(Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam(value = "status", required = false) ApprovalStatus status,
                            @RequestParam(value = "urgency", required = false) String urgency,
                            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        User user = userDetails.getUser();
        List<Request> requests;

        if (!RoleType.REPAIRMAN.equals(user.getRole())) {
            requests = requestService.findAllRequests();
        } else {
            requests = requestService.findByUser(user);
        }

        // Apply filters
        if (status != null) {
            requests = requests.stream()
                    .filter(request -> request.getStatus() == status)
                    .toList();
        }

        if (urgency != null && !urgency.isEmpty()) {
            requests = requests.stream()
                    .filter(request -> urgency.equalsIgnoreCase(request.getUrgencyLevel()))
                    .toList();
        }

        if (dateFrom != null) {
            requests = requests.stream()
                    .filter(r -> !r.getCreatedAt().toLocalDate().isBefore(dateFrom))
                    .toList();
        }

        if (dateTo != null) {
            requests = requests.stream()
                    .filter(r -> !r.getCreatedAt().toLocalDate().isAfter(dateTo))
                    .toList();
        }

        model.addAttribute("requests", requests);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedUrgency", urgency);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);

        return "requests/list";
    }

    
    @GetMapping("/filters")
    @ResponseBody
    public Map<String, Object> getRequestsAjax(
            @RequestParam(value = "status", required = false) ApprovalStatus status,
            @RequestParam(value = "urgency", required = false) String urgency,
            @RequestParam(required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
            @RequestParam(value = "draw", defaultValue = "0") int draw,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "length", defaultValue = "10") int length,
            @RequestParam(value = "search[value]", defaultValue = "") String searchValue,
            @RequestParam(value = "order[0][column]", defaultValue = "0") int orderColumn,
            @RequestParam(value = "order[0][dir]", defaultValue = "asc") String orderDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // 1. Fetch all requests (admin or user)
        User user = userDetails.getUser();
        List<Request> requests;
        if(user.getRole().equals(RoleType.REPAIRMAN)) {
            requests = requestService.findByUser(user);
        } else {
            requests = requestService.findAllRequests(); 
        }
        

        // 2. Apply filters
        if (status != null) {
            requests = requests.stream()
                    .filter(r -> r.getStatus() == status)
                    .toList();
        }
        if (urgency != null && !urgency.isEmpty()) {
            requests = requests.stream()
                    .filter(r -> urgency.equalsIgnoreCase(r.getUrgencyLevel()))
                    .toList();
        }
        if (fromDate != null) {
            requests = requests.stream()
                    .filter(r -> !r.getCreatedAt().toLocalDate().isBefore(fromDate))
                    .toList();
        }
        if (toDate != null) {
            requests = requests.stream()
                    .filter(r -> !r.getCreatedAt().toLocalDate().isAfter(toDate))
                    .toList();
        }

        // 3. Apply search filter
        if (!searchValue.isEmpty()) {
            String lowerSearch = searchValue.toLowerCase();
            requests = requests.stream()
                    .filter(r -> r.getTitle().toLowerCase().contains(lowerSearch) ||
                                r.getDescription().toLowerCase().contains(lowerSearch))
                    .toList();
        }

        int totalRecords = requests.size();

        // 4. Apply sorting
        requests = sortRequests(requests, orderColumn, orderDirection);

        // 5. Apply pagination
        int pageLength = length == -1 ? totalRecords : length;
        List<Request> paginated = requests.stream()
                                  .skip(start)
                                  .limit(pageLength)
                                  .toList();


        // 6. Prepare response for DataTables
        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", totalRecords);
        response.put("recordsFiltered", totalRecords);
        response.put("data", paginated.stream().map(this::convertToDataTableRow).toList());

        return response;
    }

    private List<Request> sortRequests(List<Request> requests, int orderColumn, String orderDir) {
        Comparator<Request> comparator = switch (orderColumn) {
            case 0 -> Comparator.comparing(Request::getId);
            case 1 -> Comparator.comparing(Request::getTitle, String.CASE_INSENSITIVE_ORDER);
            case 2 -> Comparator.comparing(Request::getDate);
            case 3 -> Comparator.comparing(Request::getRequestBy, String.CASE_INSENSITIVE_ORDER);
            case 4 -> Comparator.comparing(Request::getItems, String.CASE_INSENSITIVE_ORDER);
            case 5 -> Comparator.comparing(Request::getDescription, String.CASE_INSENSITIVE_ORDER);
            case 6 -> Comparator.comparing(Request::getUrgencyLevel, String.CASE_INSENSITIVE_ORDER);
            case 7 -> Comparator.comparing(Request::getApprovalLevel);
            case 8 -> Comparator.comparing(Request::getStatus);
            case 9 -> Comparator.comparing(Request::getCreatedAt);
            default -> Comparator.comparing(Request::getId);
        };

        if ("desc".equalsIgnoreCase(orderDir)) {
            comparator = comparator.reversed();
        }

        return requests.stream().sorted(comparator).toList();
    }



    private Map<String, Object> convertToDataTableRow(Request r) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", r.getId());
        row.put("title", r.getTitle());
        row.put("date", r.getDate());
        row.put("requestBy", r.getRequestBy());
        row.put("items", r.getItems());
        row.put("description", r.getDescription());
        row.put("urgency", r.getUrgencyLevel());
        row.put("approveLevel", r.getApprovalLevel());
        row.put("status", r.getStatus());
        row.put("createdAt", r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // Actions column (optional, can also render in JS)
        StringBuilder actions = new StringBuilder();
        actions.append("<a href='/requests/").append(r.getId()).append("' class='action-btn btn-view mr-2'><i class='fas fa-eye'></i> View</a>");
        if (ApprovalStatus.PENDING.equals(r.getStatus())) {
            actions.append("<a href='/requests/edit/").append(r.getId()).append("' class='action-btn btn-edit mr-2'><i class='fas fa-edit'></i> Edit</a>");
            actions.append("<a href='/requests/").append(r.getId()).append("/cancel' class='action-btn btn-delete' onclick=\"return confirm('Are you sure you want to cancel this request?');\"><i class='fas fa-times-circle'></i> Cancel</a>");
        }
        row.put("actions", actions.toString());

        return row;
    }


 



    
    @GetMapping("/create")
    public String showCreateForm(Model model) {

        Request request = new Request();
        request.setDate(LocalDate.now());
        model.addAttribute("request", request);
        return "requests/create";
    }
    
    @PostMapping(value="/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createRequest(
        @Valid @ModelAttribute("request") Request request,
        BindingResult result,
        @RequestParam(value = "attachedFile", required = false) MultipartFile attachedFile,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model) throws IOException {
        if (result.hasErrors()) {
            return "requests/create";
        }
        
        try {
            if (attachedFile != null && !attachedFile.isEmpty()) {
                // File size check (5 MB max)
                if (attachedFile.getSize() > 5 * 1024 * 1024) {
                    model.addAttribute("error", "File size must not exceed 5 MB");
                    return "requests/create";
                }

                // File type check (only images or pdf)
                String contentType = attachedFile.getContentType();
                if (contentType == null || 
                    !(contentType.startsWith("image/") || contentType.equals("application/pdf"))) {
                    model.addAttribute("error", "Only images and PDF files are allowed");
                    return "requests/create";
                }

                // Save file (example: uploads/ directory)
                Path uploadDir = Paths.get("uploads");
                Files.createDirectories(uploadDir);

                String fileName = System.currentTimeMillis() + "_" + attachedFile.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);
                Files.copy(attachedFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Save filename to entity
                request.setAttachment(fileName);
            }
            request.setCurrentLevel(ApprovalLevel.LEVEL_1);
            request.setApprovalLevel(ApprovalLevel.LEVEL_0);
            requestService.createRequest(request, userDetails.getUser());
            return "redirect:/requests/list?success=created";
        } catch (IOException e) {
            model.addAttribute("error", "Failed to create request: " + e.getMessage());
            return "requests/create";
        }
    }
    
    @GetMapping("/{id}")
    public String viewRequest(@PathVariable Long id, Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            
            model.addAttribute("approvalStatus", ApprovalStatus.values());
            model.addAttribute("request", request);
            return "requests/detail";
        }
        return "redirect:/requests/list?error=not_found";
    }
    @GetMapping("/{id}/clone")
    public String cloneRequest(@PathVariable Long id, Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
        
            model.addAttribute("request", request);
            return "requests/clone";
        }
        return "redirect:/requests/list?error=not_found";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            
            // Check if user has access to edit this request
            if (!userDetails.getUser().getRole().equals(RoleType.ADMIN) && 
                !request.getCreatedBy().getId().equals(userDetails.getUser().getId())) {
                return "redirect:/requests/list?error=access_denied";
            }
            
            // Check if request can be edited (only pending requests)
            if (request.getStatus() != ApprovalStatus.PENDING) {
                return "redirect:/requests/" + id + "?error=cannot_edit";
            }
            
            model.addAttribute("request", request);
            return "requests/edit";
        }
        return "redirect:/requests/list?error=not_found";
    }
    
    @PostMapping("/edit/{id}")
    public String updateRequest(@PathVariable Long id,
                              @Valid @ModelAttribute("request") Request requestDetails,
                              BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        if (result.hasErrors()) {
            return "requests/edit";
        }
        
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            
            // Check if user has access to edit this request
            if (!userDetails.getUser().getRole().equals(RoleType.ADMIN) && 
                !request.getCreatedBy().getId().equals(userDetails.getUser().getId())) {
                return "redirect:/requests/list?error=access_denied";
            }
            
            // Check if request can be edited (only pending requests)
            if (request.getStatus() != ApprovalStatus.PENDING) {
                return "redirect:/requests/" + id + "?error=cannot_edit";
            }
            
            // Update request details
            request.setTitle(requestDetails.getTitle());
            request.setDescription(requestDetails.getDescription());
            request.setItems(requestDetails.getItems());
            request.setRequestBy(requestDetails.getRequestBy());
            request.setDate(requestDetails.getDate());
            request.setUrgencyLevel(requestDetails.getUrgencyLevel());
            
            try {
                requestService.updateRequest(request);
                return "redirect:/requests/list?success=updated";
            } catch (Exception e) {
                model.addAttribute("error", "Failed to update request: " + e.getMessage());
                return "requests/edit";
            }
        }
        return "redirect:/requests/list?error=not_found";
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelRequest(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            
            // Check if user has permission to cancel this request
            if (request.getCreatedBy().getId().equals(userDetails.getUser().getId()) ||
                userDetails.getUser().getRole().equals(RoleType.ADMIN)) {
                
                // Check if request can be cancelled (only pending requests)
                if (request.getStatus() != ApprovalStatus.PENDING) {
                    return "redirect:/requests/" + id + "?error=cannot_cancel";
                }
                
                requestService.changeStatus(id, ApprovalStatus.CANCELLED);
                return "redirect:/requests/list?success=cancelled";
            }
        }
        return "redirect:/requests/list?error=cancel_failed";
    }

    // @DeleteMapping("/{id}")
    // public String deleteRequest(@PathVariable Long id) {
    //     requestService.deleteRequest(id);
    //     return "redirect:/requests/list";
    // }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long id) {
        return requestService.findById(id)
                .map(request -> {
                    if (request.getStatus() == ApprovalStatus.APPROVED) {
                        // ❌ Do not allow delete if approved
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Approved requests cannot be deleted"
                        ));
                    }

                    requestService.deleteRequest(id);

                    return ResponseEntity.noContent().build(); // ✅ 204 No Content
                })
                .orElseGet(() -> ResponseEntity.notFound().build()); // ✅ 404 if not found
    }



    @GetMapping("/export")
    public void exportRequests(
            @RequestParam(required = false, defaultValue = "false") boolean asExcel,
            @RequestParam(required = false, defaultValue = "false") boolean asPdf,
            @RequestParam List<Long> ids,
            HttpServletResponse response
    ) throws Exception {

        List<Request> requests = requestService.findByIds(ids);
        

        if (asExcel) {
            exportAsExcel(requests, response);
        } else if (asPdf) {
            exportAsPdf(requests, response);
        } 
    }

    private void exportAsExcel(List<Request> requests, HttpServletResponse response) throws IOException {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=Request-reports.xlsx");

    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Request Reports");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Title", "Date", "Requested By", "Items", "Description",
                "Urgency", "Approval Level", "Status", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Data rows
        int rowNum = 1;
        for (Request r : requests) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getId());
            row.createCell(1).setCellValue(r.getTitle());
            row.createCell(2).setCellValue(r.getDate() != null ? r.getDate().toString() : "");
            row.createCell(3).setCellValue(r.getRequestBy());
            row.createCell(4).setCellValue(r.getItems());
            row.createCell(5).setCellValue(r.getDescription());
            row.createCell(6).setCellValue(r.getUrgencyLevel());
            row.createCell(7).setCellValue(r.getApprovalLevel().toString());
            row.createCell(8).setCellValue(r.getStatus().toString());
            row.createCell(9).setCellValue(r.getCreatedAt() != null ?
                    r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
        }

        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write workbook to response
        workbook.write(response.getOutputStream());
        response.getOutputStream().flush();
    }
}


    private void exportAsPdf(List<Request> requests, HttpServletResponse response) throws Exception {
    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=requests.pdf");

    Document document = new Document();
    PdfWriter.getInstance(document, response.getOutputStream());
    document.open();

    // Title
    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    Paragraph title = new Paragraph("Requests Export", titleFont);
    title.setAlignment(Element.ALIGN_CENTER);
    title.setSpacingAfter(20);
    document.add(title);

    // Table
    PdfPTable table = new PdfPTable(10);
    table.setWidthPercentage(100);

    String[] headers = {"ID", "Title", "Date", "Requested By", "Items", "Description",
            "Urgency", "Approval Level", "Status", "Created At"};

    for (String header : headers) {
        PdfPCell cell = new PdfPCell(new Phrase(header));
        cell.setBackgroundColor(new BaseColor(220, 220, 220));
        cell.setPadding(5);
        table.addCell(cell);
    }

    for (Request r : requests) {
        table.addCell(String.valueOf(r.getId()));
        table.addCell(r.getTitle());
        table.addCell(r.getDate() != null ? r.getDate().toString() : "");
        table.addCell(r.getRequestBy());
        table.addCell(r.getItems());
        table.addCell(r.getDescription());
        table.addCell(r.getUrgencyLevel());
        table.addCell(r.getApprovalLevel().toString());
        table.addCell(r.getStatus().toString());
        table.addCell(r.getCreatedAt() != null ?
                r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
    }

    document.add(table);
    document.close();
}

    
}
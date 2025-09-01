package timdev.timdev.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import timdev.timdev.dto.ApprovalDTO;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.entity.Approval;
import timdev.timdev.entity.Request;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.enums.ApproveOrRejectRequestDTO;
import timdev.timdev.enums.RoleType;
import timdev.timdev.service.ApprovalService;
import timdev.timdev.service.RequestService;

@Controller
@RequestMapping("/approvals")
public class ApprovalController {
    
    @Autowired
    private RequestService requestService;
    @Autowired
    private ApprovalService approvalService;

    @GetMapping("/requests/pending")
    public String listRequests(Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam(value = "status", required = false) ApprovalStatus status,
                            @RequestParam(value = "urgency", required = false) String urgency,
                            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        User user = userDetails.getUser();
        List<Request> requests;

        if (RoleType.ADMIN.equals(user.getRole())) {
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

        return "approvals/pending";
    }

    @GetMapping("/requests/pending/ajax")
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

        ApprovalLevel level = userDetails.getUser().getApprovalLevel();
        
        List<Request> requests = requestService.findByStatusInAndCurrentLevel(List.of(ApprovalStatus.PENDING, ApprovalStatus.INREVIEW), level); 
        // List<Request> requests = requestService.findAllPendingRequests(); 

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
        response.put("data", paginated.stream().map(this::convertRequestToDataTableRow).toList());

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


    private Map<String, Object> convertRequestToDataTableRow(Request r) {
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



    // approved request by approver

    @GetMapping("/requests/approved")
    public String listApprovedRequests(Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam(value = "status", required = false) ApprovalStatus status,
                            @RequestParam(value = "urgency", required = false) String urgency,
                            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        User approver = userDetails.getUser();
        List<Approval> approvals = approvalService.findByApprover(approver);
        model.addAttribute("approvals", approvals);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedUrgency", urgency);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);

        return "approvals/approved";
    }

    @GetMapping("/requests/approved/ajax")
    @ResponseBody
    public Map<String, Object> getApprovedRequestsAjax(
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
        User approver = userDetails.getUser();
        List<Approval> approvals = approvalService.findByApprover(approver);

        int totalRecords = approvals.size();

        // Apply sorting
        approvals = sortApprovalRequests(approvals, orderColumn, orderDirection);

        // Apply pagination
        int pageLength = length == -1 ? totalRecords : length;
        List<Approval> paginated = approvals.stream()
                .skip(start)
                .limit(pageLength)
                .toList();

        // Convert to DTOs
        List<ApprovalDTO> dtoList = paginated.stream()
                .map(this::convertToDTO)
                .toList();

        // Prepare response for DataTables
        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", totalRecords);
        response.put("recordsFiltered", totalRecords);
        response.put("data", dtoList);

        return response;
    }


    private List<Approval> sortApprovalRequests(List<Approval> approvals, int orderColumn, String orderDirection) {
        Comparator<Approval> comparator;

        switch (orderColumn) {
            case 0 -> comparator = Comparator.comparing(a -> a.getRequest().getTitle(), Comparator.nullsLast(String::compareToIgnoreCase));
            case 1 -> comparator = Comparator.comparing(Approval::getLevel, Comparator.nullsLast(Comparator.naturalOrder()));
            case 2 -> comparator = Comparator.comparing(Approval::getStatus, Comparator.nullsLast(Comparator.naturalOrder()));
            case 3 -> comparator = Comparator.comparing(a -> a.getApprover().fullName());
            case 4 -> comparator = Comparator.comparing(Approval::getApprovalDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case 5 -> comparator = Comparator.comparing(Approval::getComments, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> comparator = Comparator.comparing(Approval::getId); // fallback
        }

        if ("desc".equalsIgnoreCase(orderDirection)) {
            comparator = comparator.reversed();
        }

        return approvals.stream()
                .sorted(comparator)
                .toList();
    }



    private ApprovalDTO convertToDTO(Approval approval) {
        ApprovalDTO dto = new ApprovalDTO();
        dto.setId(approval.getId());
        dto.setRequestId(approval.getRequest().getId());
        dto.setRequestTitle(approval.getRequest().getTitle()); // assuming Request has a title
        dto.setLevel(approval.getLevel());
        dto.setStatus(approval.getStatus());
        dto.setApproverId(approval.getApprover() != null ? approval.getApprover().getId() : null);
        dto.setApproverName(approval.getApprover() != null ? approval.getApprover().fullName() : null); // assuming User has fullName()
        dto.setComments(approval.getComments());
        dto.setApprovalDate(approval.getApprovalDate());
        return dto;
    }





        // approve and reject requests
    @PostMapping("/approve-or-reject")
    public ResponseEntity<?> approveOrReject(
        @RequestBody ApproveOrRejectRequestDTO dto, 
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        dto.setApproverId(userDetails.getUser().getId());
        Approval saved = approvalService.approveOrRejectRequest(dto);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Approval saved successfully"
        ));
    }

    @PostMapping("/delete/approval")
    public ResponseEntity<?> deleteApproval(
        @RequestBody Map<String, Long> request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long id = request.get("id");
        approvalService.deleteAndCheckRequestLevel(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Approval deleted successfully"
        ));
    }

}

package timdev.timdev.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.AllArgsConstructor;
import timdev.timdev.dto.AssignedVehicleDTO;
import timdev.timdev.dto.ExternalDriverDTO;
import timdev.timdev.dto.TruckStatsByType;
import timdev.timdev.dto.TruckTypeStats;
import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.service.DriverProxyService;
import timdev.timdev.service.ServiceCheckerService;

@AllArgsConstructor
@Controller
public class AdminController {

    private final ServiceCheckerService serviceCheckerService;
    private final DriverProxyService driverProxyService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<ServiceChecker> allChecks = serviceCheckerService.getAll();
        
        long totalIssuesCount = allChecks.stream()
            .mapToLong(sc -> sc.getNotCheckedCount() > 0 ? sc.getNotCheckedCount() : 0)
            .sum();
        // Basic statistics
        model.addAttribute("totalChecks", allChecks.size());
        model.addAttribute("completedChecks", allChecks.stream()
                .filter(sc -> "Checked".equals(sc.issuesStatus())).count());
        long unCheckedCount = allChecks.stream().filter(sc -> "Unchecked".equals(sc.issuesStatus())).count();

        model.addAttribute("unCheckedCount", unCheckedCount);
        model.addAttribute("issuesCount", totalIssuesCount);

        TruckTypeStats truckStats = getTruckTypeStats(allChecks);

        // Chart data
        model.addAttribute("statusData", getStatusDistribution(allChecks));
        model.addAttribute("trendData", getWeeklyIssuesWithCheckedData(allChecks));
        model.addAttribute("trendLabels", getWeeklyLabels());
        List<String> truckTypeLabels = Arrays.asList("Big", "Small");
        List<Long> truckTypeData = Arrays.asList(truckStats.getTotalBigTruck(), truckStats.getTotalSmallTruck());

        model.addAttribute("truckTypeLabels", truckTypeLabels);
        model.addAttribute("truckTypeData", truckTypeData);
        model.addAttribute("performanceData", getPerformanceData(allChecks));


        Map<String, TruckStatsByType> truckStats_ = getTruckTypeStatsMap(allChecks);
        model.addAttribute("truckStats_", truckStats_);

        // Recent checks (last 10)
        List<Map<String, Object>> recentChecks = serviceCheckerService.convertToDataTablesFormat(
            allChecks.stream()
                .sorted(Comparator.comparingLong(ServiceChecker::getNotCheckedCount).reversed())
                .limit(10)
                .collect(Collectors.toList())
        );
        model.addAttribute("recentChecks", recentChecks);

        return "admin/dashboard";
    }

    private List<Long> getStatusDistribution(List<ServiceChecker> checks) {
        long totalChecked = checks.stream().filter(sc -> "Checked".equals(sc.issuesStatus())).count();
        long totalUnchecked = checks.stream().filter(sc -> "Unchecked".equals(sc.issuesStatus())).count();
        
        long total = checks.size();
        long totalIssuesItemCount = checks.stream()
            .mapToLong(sc -> sc.getNotCheckedCount() > 0 ? sc.getNotCheckedCount() : 0)
            .sum();
        
        return Arrays.asList(total, totalChecked, totalIssuesItemCount);
    }

    private Map<String, List<Long>> getWeeklyIssuesWithCheckedData(List<ServiceChecker> checks) {
        LocalDate today = LocalDate.now();
        List<Long> issues = new ArrayList<>();
        List<Long> checked = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            
            long issuesCount = checks.stream()
                .filter(sc -> sc.getDate() != null && sc.getDate().equals(date))
                .mapToLong(sc -> sc.getNotCheckedCount() > 0 ? sc.getNotCheckedCount() : 0)
                .sum();
            issues.add(issuesCount);

            long checkedCount = checks.stream()
                .filter(sc -> sc.getDate() != null && sc.getDate().equals(date))
                .filter(sc -> "Checked".equals(sc.issuesStatus())) // use getter
                .count();
            checked.add(checkedCount);
        }

        Map<String, List<Long>> result = new HashMap<>();
        result.put("issues", issues);
        result.put("checked", checked);
        return result;
    }


    private List<String> getWeeklyLabels() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        LocalDate today = LocalDate.now();
        List<String> labels = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            labels.add(today.minusDays(i).format(formatter));
        }
        return labels;
    }


    private Map<String, TruckStatsByType> getTruckTypeStatsMap(List<ServiceChecker> checks) {
        long totalBigTruck = 0;
        long totalSmallTruck = 0;
        // count of checked
        long totalBigTruckCheckedCount = 0;
        long totalSmallTruckCheckedCount = 0;
        // count of unchecked
        long totalBigTruckUnCheckedCount = 0;
        long totalSmallTruckUnCheckedCount = 0;
        // count of issue items
        long totalBigTruckIssueCount = 0;
        long totalSmallTruckIssueCount = 0;
        

        for (ServiceChecker sc : checks) {
            ExternalDriverDTO exDriver = sc.getExDriver() != null 
                ? driverProxyService.getDriverById(sc.getExDriver().getId()) 
                : null;
            sc.setExDriver(exDriver);

            String truckType = Optional.ofNullable(sc.getExDriver())
                .map(ExternalDriverDTO::getAssignedVehicle)
                .map(AssignedVehicleDTO::getTruckSize)
                .orElse("Unknown");

           

            if ("BIG_TRUCK".equals(truckType)) {
                totalBigTruck++;
                if ("Checked".equals(sc.issuesStatus())) totalBigTruckCheckedCount++;
                if ("Unchecked".equals(sc.issuesStatus())) totalBigTruckUnCheckedCount++;
                totalBigTruckIssueCount += sc.getNotCheckedCount();
            } else if ("SMALL_VAN".equals(truckType)) {
                totalSmallTruck++;
                if ("Checked".equals(sc.issuesStatus())) totalSmallTruckCheckedCount++;
                if ("Unchecked".equals(sc.issuesStatus())) totalSmallTruckUnCheckedCount++;
                totalSmallTruckIssueCount += sc.getNotCheckedCount();
            }
        }

        Map<String, TruckStatsByType> statsMap = new HashMap<>();
        statsMap.put("Big", new TruckStatsByType(totalBigTruck, totalBigTruckCheckedCount, totalBigTruckUnCheckedCount, totalBigTruckIssueCount));
        statsMap.put("Small", new TruckStatsByType(totalSmallTruck, totalSmallTruckCheckedCount, totalSmallTruckUnCheckedCount, totalSmallTruckIssueCount));

        return statsMap;
    }


    private TruckTypeStats getTruckTypeStats(List<ServiceChecker> checks) {
        long totalBigTruck = 0;
        long totalSmallTruck = 0;
        long totalBigTruckCheckedCount = 0;
        long totalSmallTruckCheckedCount = 0;
        long totalBigTruckIssueCount = 0;
        long totalSmallTruckIssueCount = 0;

        for (ServiceChecker sc : checks) {
            ExternalDriverDTO exDriver = sc.getExDriver() != null 
                ? driverProxyService.getDriverById(sc.getExDriver().getId()) 
                : null;
            sc.setExDriver(exDriver);

            String truckType = Optional.ofNullable(sc.getExDriver())
                .map(ExternalDriverDTO::getAssignedVehicle)
                .map(AssignedVehicleDTO::getTruckSize)
                .orElse("Unknown");

            if ("BIG_TRUCK".equals(truckType)) {
                totalBigTruck++;
                if ("Checked".equals(sc.issuesStatus())) totalBigTruckCheckedCount++;
                totalBigTruckIssueCount += sc.getNotCheckedCount();
            } else if ("SMALL_VAN".equals(truckType)) {
                totalSmallTruck++;
                if ("Checked".equals(sc.issuesStatus())) totalSmallTruckCheckedCount++;
                totalSmallTruckIssueCount += sc.getNotCheckedCount();
            }
        }

        return new TruckTypeStats(
            totalBigTruck,
            totalSmallTruck,
            totalBigTruckCheckedCount,
            totalSmallTruckCheckedCount,
            totalBigTruckIssueCount,
            totalSmallTruckIssueCount
        );
    }


    private List<String> getTruckTypeLabels(List<ServiceChecker> checks) {
        
        return Arrays.asList("'Big'","'Small'");
    }

    private List<Long> getPerformanceData(List<ServiceChecker> checks) {
        // Mock performance metrics - replace with actual calculations
        return Arrays.asList(85L, 92L, 78L, 88L, 75L);
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, @AuthenticationPrincipal UserDetails user) {
        return "redirect:/dashboard";
    }
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Settings");
        return "admin/settings";
    }
}
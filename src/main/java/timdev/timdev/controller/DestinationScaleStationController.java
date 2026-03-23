package timdev.timdev.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.DestinationPortDTO;
import timdev.timdev.entity.Average;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationPort;
import timdev.timdev.entity.Measurement;
import timdev.timdev.entity.Port;
import timdev.timdev.entity.ScaleStation;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.DestinationScaleStation;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.repository.MeasurementRepository;
import timdev.timdev.repository.PortRepository;
import timdev.timdev.repository.ScaleStationRepository;
import timdev.timdev.repository.DestinationPortRepository;
import timdev.timdev.repository.DestinationScaleStationRepository;
import timdev.timdev.service.AverageService;
import timdev.timdev.service.DestinationScaleStationService;
import timdev.timdev.service.DestinationService;
import timdev.timdev.service.DestinationSettingService;
import timdev.timdev.service.PermissionChecker;
import timdev.timdev.service.TruckService;

@Controller
@AllArgsConstructor
@RequestMapping("/scale-stations-destination")
public class DestinationScaleStationController {

    private TruckService truckService;
    private DestinationScaleStationRepository destinationScaleStationRepository;
    private ScaleStationRepository scaleStationRepository;
    private DestinationSettingService destinationSettingService;
    private DestinationScaleStationService destinationtScaleStationService;

    private DestinationSettingService service;

    private PortRepository portRepository;

    private DestinationPortRepository destinationPortRepository;

    private PermissionChecker permissionChecker;

    private long calculateEndIndex(int currentPage, int pageSizeNumber, long totalElements) {
        long endIndex = (long) currentPage * pageSizeNumber + pageSizeNumber;
        return Math.min(endIndex, totalElements);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DESTINATION_SETTINGS')")
    @GetMapping
    public String index(
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "200") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "sortBy", defaultValue = "code") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order,
            Model model) {

        // ====== Pagination & Sorting ======
        int size = "all".equalsIgnoreCase(sizeParam) ? Integer.MAX_VALUE : Integer.parseInt(sizeParam);
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = switch (sortBy) {
            case "code" -> Sort.by(direction, "code");
            case "name" -> Sort.by(direction, "name");
            case "id" -> Sort.by(direction, "id");
            default -> Sort.by(direction, "id");
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DestinationSetting> destinationPage = showAll
                ? new PageImpl<>(service.getAllFiltered(search, sort))
                : service.getAllWithPageable(search, pageable, sort);

        List<DestinationSetting> destinations = destinationPage.getContent();

        // ====== Fetch scale stations ======
        List<ScaleStation> scaleStations = scaleStationRepository.findAll(Sort.by("id"));
        List<Port> ports = portRepository.findAll(Sort.by("id"));

        // ====== Fetch only needed DestinationScaleStations ======
        List<DestinationScaleStation> mappings = destinationScaleStationRepository
                .findByDestinationIn(destinations);

        List<DestinationPort> portMappings = destinationPortRepository
                .findByDestinationIn(destinations);

        // ====== Build a safe amount map ======
        Map<String, BigDecimal> amountMap = new HashMap<>();
        for (DestinationScaleStation dss : mappings) {
            if (dss.getDestination() != null && dss.getScaleStation() != null) {
                String key = dss.getDestination().getId() + "_" + dss.getScaleStation().getId();
                amountMap.put(key, dss.getAmount());
            }
        }

        Map<String, BigDecimal> portAmountMap = new HashMap<>();
        for (DestinationPort dss : portMappings) {
            if (dss.getDestination() != null && dss.getPort() != null) {
                String key = dss.getDestination().getId() + "_" + dss.getPort().getId();
                portAmountMap.put(key, dss.getAmount());
            }
        }

        // ====== Add attributes for Thymeleaf ======
        model.addAttribute("data", destinations);
        model.addAttribute("scaleStations", scaleStations);
        model.addAttribute("amountMap", amountMap);
        model.addAttribute("ports", ports);
        model.addAttribute("portAmountMap", portAmountMap);

        model.addAttribute("totalElements", destinationPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", destinationPage.getTotalPages());
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("search", search);
        model.addAttribute("showAll", showAll);
        model.addAttribute("endIndex", (int) Math.min((page + 1) * size, destinationPage.getTotalElements()));
        model.addAttribute("startIndex", page * size + 1);

        return "truck-scale-stations/index";
    }

    // for ports
    @GetMapping("form/port")
    public String viewPortForm(Model model, @PathVariable(required = false) Long id) {

        List<DestinationSetting> settings = destinationSettingService.findSettingsIsNotInPort(null);
        List<Port> ports = portRepository.findAll();

        model.addAttribute("settings", settings);
        model.addAttribute("ports", ports);

        return "truck-scale-stations/port-form";
    }

    @PostMapping("/save-multiple/port")
    public String saveMultiplePorts(
            @RequestParam Long destinationSettingId,
            @RequestParam Map<String, String> params,
            @RequestParam(value = "action", required = false) String action,
            RedirectAttributes ra) {
        try {
            DestinationSetting destinationSetting = destinationSettingService.findById(destinationSettingId);
            destinationtScaleStationService.saveMultiplePorts(destinationSettingId, params);
            ra.addFlashAttribute("success",
                    "destination with ports for " + destinationSetting.getName()
                            + " saved successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving destination: " + e.getMessage());
            return "redirect:/truck-scale-stations/port-form";
        }
        if ("save_continue".equals(action)) {
            return "redirect:/truck-scale-stations/port-form";
        }
        return "redirect:/scale-stations-destination";
    }

    @GetMapping("/edit/{id}/ports-amount")
    public String formEditPortAmount(Model model,
            @PathVariable(required = true, value = "id") Long destinationSettingId) {

        DestinationPort destinaionPort;
        if (destinationSettingId != null) {
            destinaionPort = destinationPortRepository.findById(destinationSettingId).orElse(null);
        } else {
            destinaionPort = new DestinationPort();
            destinaionPort.setAmount(null);
        }
        List<DestinationSetting> settings = destinationSettingService
                .findSettingsIsNotInScaleStation(destinationSettingId);

        DestinationSetting destinationSetting = destinationSettingService.findById(destinationSettingId);
        List<Port> ports = portRepository.findAll();

        Map<Long, BigDecimal> amounts = destinationtScaleStationService
                .getPortAmountsByDestinationId(destinationSettingId);

        model.addAttribute("amounts", amounts);

        model.addAttribute("settings", null);
        model.addAttribute("destinationSetting", destinationSetting);
        model.addAttribute("ports", ports);
        model.addAttribute("destinaionPort", destinaionPort);

        return "truck-scale-stations/port-form";
    }

    // Show form for new or edit Average
    @GetMapping("form")
    public String viewForm(Model model, @PathVariable(required = false) Long id) {

        List<DestinationSetting> settings = destinationSettingService.findSettingsIsNotInScaleStation(null);
        List<ScaleStation> scaleStations = scaleStationRepository.findAll();

        model.addAttribute("settings", settings);
        model.addAttribute("scaleStations", scaleStations);

        return "truck-scale-stations/create-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(Model model, @PathVariable(required = true, value = "id") Long destinationSettingId) {

        DestinationScaleStation destinaionScaleStation;
        if (destinationSettingId != null) {
            destinaionScaleStation = destinationScaleStationRepository.findById(destinationSettingId).orElse(null);
        } else {
            destinaionScaleStation = new DestinationScaleStation();
            destinaionScaleStation.setAmount(null);
        }
        List<DestinationSetting> settings = destinationSettingService
                .findSettingsIsNotInScaleStation(destinationSettingId);

        DestinationSetting destinationSetting = destinationSettingService.findById(destinationSettingId);
        List<ScaleStation> scaleStations = scaleStationRepository.findAll();

        Map<Long, BigDecimal> amounts = destinationtScaleStationService.getAmountsByDestinationId(destinationSettingId);

        model.addAttribute("amounts", amounts);

        model.addAttribute("settings", null);
        model.addAttribute("destinationSetting", destinationSetting);
        model.addAttribute("scaleStations", scaleStations);
        model.addAttribute("destinaionScaleStation", destinaionScaleStation);

        return "truck-scale-stations/create-form";
    }

    @PostMapping("/save-multiple")
    public String saveMultiple(
            @RequestParam Long destinationSettingId,
            @RequestParam Map<String, String> params,
            @RequestParam(value = "action", required = false) String action,
            RedirectAttributes ra) {
        try {
            DestinationSetting destinationSetting = destinationSettingService.findById(destinationSettingId);
            destinationtScaleStationService.saveMultiple(destinationSettingId, params);
            ra.addFlashAttribute("success",
                    "destination with scales for " + destinationSetting.getName()
                            + " saved successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving destination: " + e.getMessage());
            return "redirect:/truck-scale-stations/create-form";
        }
        if ("save_continue".equals(action)) {
            return "redirect:/truck-scale-stations/create-form";
        }
        return "redirect:/scale-stations-destination";
    }

    // // Save or Update Average
    // @PostMapping("/save")
    // public String saveAverage(
    // @RequestParam(required = false) Long id,
    // @RequestParam Long destinationSettingId,
    // @RequestParam Long measurementId,
    // @RequestParam double value,
    // RedirectAttributes ra) {
    // try {
    // if (id != null) {
    // // update existing
    // averageService.updateAverage(id, destinationSettingId, measurementId, value);
    // ra.addFlashAttribute("success", "Average updated successfully!");
    // } else {
    // // create new
    // averageService.addAverage(destinationSettingId, measurementId, value);
    // ra.addFlashAttribute("success", "Average saved successfully!");
    // }
    // } catch (Exception e) {
    // ra.addFlashAttribute("error", e.getMessage());
    // }
    // return "redirect:/averages";
    // }

    // // Delete average
    // @GetMapping("/delete/{id}")
    // public String deleteAverage(@PathVariable Long id, RedirectAttributes ra) {
    // try {
    // averageService.deleteById(id);
    // ra.addFlashAttribute("success", "Average deleted successfully!");
    // } catch (Exception e) {
    // ra.addFlashAttribute("error", e.getMessage());
    // }
    // return "redirect:/truck-scale-stations/list";
    // }

    // @ResponseBody
    // @GetMapping(value = "/ajax/measurements/by-truck", produces =
    // "application/json")
    // public List<Map<String, Object>> getMeasurementsWithAverage(
    // @RequestParam("destinationSettingId") Long destinationSettingId) {

    // // Get all measurements with their average for this truck
    // return averageService.getMeasurementsByTruck(destinationSettingId)
    // .stream()
    // .map(m -> {
    // Map<String, Object> map = new HashMap<>();

    // // Get average for this truck + measurement
    // Double avgValue = averageService.getAverageValue(destinationSettingId,
    // m.getId());

    // map.put("id", m.getId());
    // map.put("name", m.getName());
    // map.put("text", m.getName() + " (" + m.getNativeName() + ")" +
    // (avgValue != null ? " - Avg: " + avgValue : ""));
    // map.put("average", avgValue);

    // return map;
    // })
    // .toList();
    // }
}

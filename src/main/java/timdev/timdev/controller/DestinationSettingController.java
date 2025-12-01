package timdev.timdev.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import timdev.timdev.dto.SubTruckRequestDTO;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckDistance;
import timdev.timdev.service.DestinationSettingService;


@RequestMapping("/destination-settings")
@AllArgsConstructor
@Controller
public class DestinationSettingController {
    
    private DestinationSettingService service;




    @GetMapping
    public Object index(
        @RequestParam(value = "search", defaultValue = "") String search,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "200") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        Model model) throws IOException 
    {
        List<DestinationSetting> destinationPage;
        int totalPages = 1;
        // int size = "all".equalsIgnoreCase(sizeParam) ? Integer.MAX_VALUE : Integer.parseInt(sizeParam);

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
        
        if (showAll) {
            // fetch all reports with filter
            destinationPage = service.getAllFiltered(search, sort);
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DestinationSetting> withPage = service.getAllWithPageable(search, pageable, sort);
            destinationPage = withPage.getContent();
            totalPages = withPage.getTotalPages();
        }

        // put everything into model
        model.addAttribute("data", destinationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("search", search);

        model.addAttribute("showAll", showAll);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        return "destination-settings/index";
    }



    @GetMapping("/form")
    public String create(Model model) {
        model.addAttribute("destination", new DestinationSetting());
        return "destination-settings/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        DestinationSetting destination = service.findById(id);
        model.addAttribute("destination", destination);
        return "destination-settings/form";
    }


    @PostMapping("/store")
    public String store(
            @ModelAttribute DestinationSetting destination,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {

        DestinationSetting existingData = null;

        if (destination.getId() != null) {
            existingData = service.findById(destination.getId());
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

        service.save(ds);

        redirectAttributes.addFlashAttribute("success", "Destination saved successfully!");
        return "redirect:/destination-settings";
    }



    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {

        service.deleteById(id);
        return "redirect:/destination-settings";
    }



}

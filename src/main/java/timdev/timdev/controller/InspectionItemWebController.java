package timdev.timdev.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import timdev.timdev.entity.InspectionCategory;
import timdev.timdev.entity.InspectionItem;
import timdev.timdev.service.InspectionCategoryService;
import timdev.timdev.service.InspectionItemService;

@Controller
@AllArgsConstructor
@RequestMapping("/admin/items")
public class InspectionItemWebController {
    private final InspectionItemService itemService;
    private final InspectionCategoryService categoryService;

    // List all items
    @GetMapping
    public String listItems(Model model) {
        Map<String, List<InspectionItem>> groupedItems = itemService.getAllItemsGroupByCategory("id");

        model.addAttribute("groupedItems", groupedItems);
        return "items/list";
    }

    // Show form for adding a new item
    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<InspectionCategory> categories = categoryService.getAllCategories();
        model.addAttribute("item", new InspectionItem());
        model.addAttribute("categories", categories);
        return "items/form";
    }

    // Process form for adding a new item
    @PostMapping("/add")
    public String addItem(@Valid @ModelAttribute("item") InspectionItem item,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            List<InspectionCategory> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            return "items/form";
        }

        try {
            itemService.saveItem(item);
            redirectAttributes.addFlashAttribute("success", "Inspection item created successfully!");
            return "redirect:/admin/items";
        } catch (Exception e) {
            List<InspectionCategory> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", e.getMessage());
            return "items/form";
        }
    }

    // Show form for editing an existing item
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<InspectionItem> item = itemService.getItemById(id);
            List<InspectionCategory> categories = categoryService.getAllCategories();

            if (item.isPresent()) {
                model.addAttribute("item", item.get());
                model.addAttribute("categories", categories);
                return "items/form";
            } else {
                redirectAttributes.addFlashAttribute("error", "Inspection item not found with id: " + id);
                return "redirect:/admin/items";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading inspection item: " + e.getMessage());
            return "redirect:/admin/items";
        }
    }

    // Process form for editing an existing item
    @PostMapping("/edit/{id}")
    public String updateItem(@PathVariable Long id,
            @Valid @ModelAttribute("item") InspectionItem item,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            List<InspectionCategory> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            return "items/form";
        }

        try {
            item.setId(id);
            itemService.saveItem(item);
            redirectAttributes.addFlashAttribute("success", "Inspection item updated successfully!");
            return "redirect:/admin/items";
        } catch (Exception e) {
            List<InspectionCategory> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", e.getMessage());
            return "items/form";
        }
    }

    // Delete an item
    @DeleteMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            itemService.deleteItem(id);
            redirectAttributes.addFlashAttribute("success", "Inspection item deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/items";
    }
}

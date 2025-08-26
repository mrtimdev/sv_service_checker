package timdev.timdev.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import timdev.timdev.entity.InspectionCategory;
import timdev.timdev.service.InspectionCategoryService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryWebController {
    
    private final InspectionCategoryService categoryService;
    
    public CategoryWebController(InspectionCategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    // List all categories
    @GetMapping
    public String listCategories(Model model) {
        try {
            List<InspectionCategory> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            return "categories/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading categories: " + e.getMessage());
            return "categories/list";
        }
    }
    
    // Show form for adding a new category
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new InspectionCategory());
        return "categories/form";
    }
    
    // Process form for adding a new category
    @PostMapping("/add")
    public String addCategory(@Valid @ModelAttribute("category") InspectionCategory category, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes,
                             Model model) {
        
        if (result.hasErrors()) {
            return "categories/form";
        }
        
        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category created successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "categories/form";
        }
    }
    
    // Show form for editing an existing category
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<InspectionCategory> category = categoryService.getCategoryById(id);
            
            if (category.isPresent()) {
                model.addAttribute("category", category.get());
                return "categories/form";
            } else {
                redirectAttributes.addFlashAttribute("error", "Category not found with id: " + id);
                return "redirect:/admin/categories";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading category: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }
    
    // Process form for editing an existing category
    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id,
                                @Valid @ModelAttribute("category") InspectionCategory category,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        if (result.hasErrors()) {
            return "categories/form";
        }
        
        try {
            category.setId(id); // Ensure the ID is set
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "categories/form";
        }
    }
    
    // Delete a category
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
}
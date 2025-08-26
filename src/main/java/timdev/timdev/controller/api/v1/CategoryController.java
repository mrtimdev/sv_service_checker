package timdev.timdev.controller.api.v1;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import timdev.timdev.dto.CategoryDTO;
import timdev.timdev.dto.ItemDTO;
import timdev.timdev.entity.InspectionCategory;
import timdev.timdev.entity.InspectionItem;
import timdev.timdev.exception.ResourceNotFoundException;
import timdev.timdev.service.InspectionCategoryService;

@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
public class CategoryController {
    
    private final InspectionCategoryService categoryService;
    
    
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithItems() {
        List<InspectionCategory> categories = categoryService.getAllCategoriesWithItems();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categoryDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryWithItems(@PathVariable Long id) {
        InspectionCategory category = categoryService.getCategoryWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        return ResponseEntity.ok(new CategoryDTO(category));
    }
    
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody InspectionCategory category) {
        InspectionCategory createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoryDTO(createdCategory));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody InspectionCategory categoryDetails) {
        InspectionCategory updatedCategory = categoryService.updateCategory(id, categoryDetails);
        return ResponseEntity.ok(new CategoryDTO(updatedCategory));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{categoryId}/items")
    public ResponseEntity<ItemDTO> addItemToCategory(@PathVariable Long categoryId, @RequestBody InspectionItem item) {
        InspectionItem addedItem = categoryService.addItemToCategory(categoryId, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ItemDTO(addedItem));
    }
    
    @DeleteMapping("/{categoryId}/items/{itemId}")
    public ResponseEntity<?> removeItemFromCategory(@PathVariable Long categoryId, @PathVariable Long itemId) {
        categoryService.removeItemFromCategory(categoryId, itemId);
        return ResponseEntity.ok().build();
    }
}
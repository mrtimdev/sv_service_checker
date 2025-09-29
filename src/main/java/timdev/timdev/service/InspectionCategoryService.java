package timdev.timdev.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import timdev.timdev.entity.InspectionCategory;
import timdev.timdev.entity.InspectionItem;
import timdev.timdev.exception.ResourceNotFoundException;
import timdev.timdev.repository.InspectionCategoryRepository;
import timdev.timdev.repository.ServiceCheckerItemRepository;

@Service
@Transactional
@AllArgsConstructor
public class InspectionCategoryService {

    private final InspectionCategoryRepository categoryRepository;
    private final InspectionItemService itemService;

    private final ServiceCheckerItemRepository serviceCheckerItemRepository;


    /**
     * Get all categories
     * @return List of all InspectionCategory entities
     */
    public List<InspectionCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Get category by ID
     * @param id Category ID
     * @return Optional containing the category if found
     */
    public Optional<InspectionCategory> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Save or update a category
     * @param category Category to save or update
     * @return Saved category entity
     */
    public InspectionCategory saveCategory(InspectionCategory category) {
        // Check if category with the same name already exists (excluding current category for updates)
        if (category.getId() == null) {
            // For new categories, check if name already exists
            Optional<InspectionCategory> existingCategory = categoryRepository.findByName(category.getName());
            if (existingCategory.isPresent()) {
                throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
            }
        } else {
            // For updates, check if another category has the same name
            Optional<InspectionCategory> existingCategory = categoryRepository.findByNameAndIdNot(category.getName(), category.getId());
            if (existingCategory.isPresent()) {
                throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
            }
        }
        
        return categoryRepository.save(category);
    }

    /**
     * Delete category by ID
     * @param id Category ID to delete
     */
    public void deleteCategoryWithItem(Long id) {
        // Check if category exists
        InspectionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // First, delete all associated items (which will handle their notes)
        if (category.getItems() != null && !category.getItems().isEmpty()) {
            // Create a copy to avoid ConcurrentModificationException
            List<InspectionItem> items = new ArrayList<>(category.getItems());
            for (InspectionItem item : items) {
                itemService.deleteItem(item.getId());
            }
        }
        
        // Then delete the category
        categoryRepository.deleteById(id);
    }

    /**
     * Check if category exists by name
     * @param name Category name
     * @return true if category exists
     */
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    /**
     * Check if category exists by name excluding a specific ID
     * @param name Category name
     * @param id Category ID to exclude
     * @return true if category exists
     */
    public boolean existsByNameAndIdNot(String name, Long id) {
        return categoryRepository.existsByNameAndIdNot(name, id);
    }



    // api

    public List<InspectionCategory> getAllCategoriesWithItems() {
        return categoryRepository.findAllWithItems();
    }
    
    public Optional<InspectionCategory> getCategoryWithItems(Long id) {
        return categoryRepository.findByIdWithItems(id);
    }
    
    public InspectionCategory createCategory(InspectionCategory category) {
        return categoryRepository.save(category);
    }
    
    public InspectionCategory updateCategory(Long id, InspectionCategory categoryDetails) {
        InspectionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        category.setName(categoryDetails.getName());
        category.setKhmerName(categoryDetails.getKhmerName());
        
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long id) {
        InspectionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        categoryRepository.delete(category);
    }

    @Transactional
    public void deleteCategoryWithItems(Long id) {
        // delete items first
        serviceCheckerItemRepository.deleteByCategoryId(id);

        // then delete category
        categoryRepository.deleteById(id);
    }
    
    public InspectionItem addItemToCategory(Long categoryId, InspectionItem item) {
        InspectionCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        item.setCategory(category);
        category.getItems().add(item);
        
        categoryRepository.save(category);
        return item;
    }
    
    public void removeItemFromCategory(Long categoryId, Long itemId) {
        InspectionCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        InspectionItem item = category.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));
        
        category.getItems().remove(item);
        item.setCategory(null);
        
        categoryRepository.save(category);
    }
}
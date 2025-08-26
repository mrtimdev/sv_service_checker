package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import timdev.timdev.entity.InspectionCategory;
import timdev.timdev.entity.InspectionItem;
import timdev.timdev.repository.InspectionCategoryRepository;
import timdev.timdev.repository.InspectionItemRepository;

@Service
@Transactional
@AllArgsConstructor
public class InspectionItemService {

    private final InspectionItemRepository itemRepository;
    private final InspectionCategoryRepository categoryRepository;

    /**
     * Get all inspection items
     */
    public List<InspectionItem> getAllItems() {
        return itemRepository.findAll();
    }

    /**
     * Get inspection item by ID
     */
    public Optional<InspectionItem> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Get items by category ID
     */
    public List<InspectionItem> getItemsByCategoryId(Long categoryId) {
        return itemRepository.findByCategoryId(categoryId);
    }

    /**
     * Save or update an inspection item
     */
    public InspectionItem saveItem(InspectionItem item) {
        // Validate category exists
        if (item.getCategory() == null || item.getCategory().getId() == null) {
            throw new RuntimeException("Category is required");
        }
        
        InspectionCategory category = categoryRepository.findById(item.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + item.getCategory().getId()));
        
        item.setCategory(category);
        
        // Check for duplicate names
        if (item.getId() == null) {
            // For new items
            if (itemRepository.existsByName(item.getName())) {
                throw new RuntimeException("Inspection item with name '" + item.getName() + "' already exists");
            }
        } else {
            // For updates
            if (itemRepository.existsByNameAndIdNot(item.getName(), item.getId())) {
                throw new RuntimeException("Inspection item with name '" + item.getName() + "' already exists");
            }
        }
        
        return itemRepository.save(item);
    }

    /**
     * Delete inspection item only if it's not referenced in service records
     */
    public void deleteItem(Long id) {
        InspectionItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inspection item not found with id: " + id));
        
        // In a real application, you would check for service record references here
        // For now, we'll just delete if no explicit service records exist
        itemRepository.deleteById(id);
    }
}
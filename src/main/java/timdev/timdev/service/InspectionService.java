package timdev.timdev.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import timdev.timdev.entity.InspectionCategory;
import timdev.timdev.entity.InspectionItem;
import timdev.timdev.repository.InspectionCategoryRepository;
import timdev.timdev.repository.InspectionItemRepository;

@Service
public class InspectionService {
    private final InspectionCategoryRepository categoryRepository;
    private final InspectionItemRepository itemRepository;
    
    public InspectionService(InspectionCategoryRepository categoryRepository,
                           InspectionItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }
    
    public List<InspectionCategory> getAllCategoriesWithItems() {
        return categoryRepository.findAll().stream()
            .peek(category -> category.setItems(
                itemRepository.findByCategoryId(category.getId())))
            .collect(Collectors.toList());
    }
    
    public List<InspectionItem> getAllItems() {
        return itemRepository.findAll();
    }
}

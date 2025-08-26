package timdev.timdev.dto.api;

import java.util.List;

public class CategoryItemRequest {
    private Long categoryId;
    private List<ItemNoteRequest> items;
    
    // Constructors
    public CategoryItemRequest() {}
    
    // Getters and setters
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public List<ItemNoteRequest> getItems() { return items; }
    public void setItems(List<ItemNoteRequest> items) { this.items = items; }
}

package timdev.timdev.dto.api;

import java.util.List;
import java.util.stream.Collectors;

import timdev.timdev.dto.CategoryDTO;
import timdev.timdev.entity.ServiceCheckerItem;

public class ServiceCheckerWithNoDriverItemDTO {
    private Long id;
    private CategoryDTO category;
    private List<ServiceCheckerItemNoteDTO> notes;
    
    // Constructors
    public ServiceCheckerWithNoDriverItemDTO() {}
    
    public ServiceCheckerWithNoDriverItemDTO(ServiceCheckerItem item) {
        this.id = item.getId();
        this.category = new CategoryDTO(item.getCategory());
        this.notes = item.getNotes().stream()
                .map(ServiceCheckerItemNoteDTO::new)
                .collect(Collectors.toList());
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public CategoryDTO getCategory() { return category; }
    public void setCategory(CategoryDTO category) { this.category = category; }
    
    public List<ServiceCheckerItemNoteDTO> getNotes() { return notes; }
    public void setNotes(List<ServiceCheckerItemNoteDTO> notes) { this.notes = notes; }
}

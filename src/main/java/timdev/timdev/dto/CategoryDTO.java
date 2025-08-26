package timdev.timdev.dto;

import java.util.List;
import java.util.stream.Collectors;

import timdev.timdev.entity.InspectionCategory;

public class CategoryDTO {
    private Long id;
    private String name;
    private String khmerName;
    private List<ItemDTO> items;
    
    // Constructors
    public CategoryDTO() {}

    public CategoryDTO(InspectionCategory category) {
        this.id = category.getId();
        this.khmerName = category.getKhmerName();
        this.items = category.getItems().stream()
                .map(item -> new ItemDTO(item))
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKhmerName() {
        return khmerName;
    }

    public void setKhmerName(String khmerName) {
        this.khmerName = khmerName;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }
}

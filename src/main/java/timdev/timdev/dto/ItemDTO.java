package timdev.timdev.dto;

import timdev.timdev.entity.InspectionItem;

public class ItemDTO {
    private Long id;
    private String name;
    private String khmerName;
    
    // Constructors
    public ItemDTO() {}
    
    public ItemDTO(InspectionItem item) {
        this.id = item.getId();
        this.name = item.getName();
        this.khmerName = item.getKhmerName();
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
}

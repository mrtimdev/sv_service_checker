package timdev.timdev.dto.api;

import timdev.timdev.dto.ItemDTO;
import timdev.timdev.dto.ItemNoteDTO;
import timdev.timdev.entity.ServiceCheckerItemNote;

public class ServiceCheckerItemNoteDTO {
    private Long id;
    private ItemDTO inspectionItem;
    private Boolean passed;
    private String note;
    
    // Constructors
    public ServiceCheckerItemNoteDTO() {}
    
    public ServiceCheckerItemNoteDTO(ServiceCheckerItemNote note) {
        this.id = note.getId();
        this.inspectionItem = new ItemDTO(note.getInspectionItem());
        this.passed = note.isPassed();
        this.note = note.getNote();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ItemDTO getInspectionItem() { return inspectionItem; }
    public void setInspectionItem(ItemDTO inspectionItem) { this.inspectionItem = inspectionItem; }
    
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

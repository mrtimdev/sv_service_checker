package timdev.timdev.dto.api;

public class ItemNoteRequest {
    private Long itemId;
    private Boolean passed;
    private String note;
    
    // Constructors
    public ItemNoteRequest() {}
    
    public ItemNoteRequest(Long itemId, Boolean passed, String note) {
        this.itemId = itemId;
        this.passed = passed;
        this.note = note;
    }
    
    // Getters and setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
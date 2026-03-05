package timdev.timdev.dto.api;

public class ItemNoteRequest {
    private Long itemId;
    private Boolean passed;
    private String note;
    private Boolean isRequired;

    // Constructors
    public ItemNoteRequest() {
    }

    public ItemNoteRequest(Long itemId, Boolean passed, String note, Boolean isRequired) {
        this.itemId = itemId;
        this.passed = passed;
        this.note = note;
        this.isRequired = isRequired;
    }

    // Getters and setters
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }
}
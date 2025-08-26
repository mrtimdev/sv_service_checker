package timdev.timdev.dto;

public class ItemNoteDTO {
    
    private Long itemId;
    private boolean passed;
    private String note;

    public ItemNoteDTO(Long itemId, boolean passed, String note) {
        this.itemId = itemId;
        this.passed = passed;
        this.note = note;
    }
    public Long getItemId() {
        return itemId;
    }
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    public boolean isPassed() {
        return passed;
    }
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
}

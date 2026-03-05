package timdev.timdev.dto.api;

public class ChecklistItemDTO {

    private Long id;
    private String name;
    private String khmerName;
    private boolean passed;
    private String note;
    private Long categoryId;
    private String categoryName;
    private boolean isRequired;

    public ChecklistItemDTO() {
    }

    public ChecklistItemDTO(Long id, String name, String khmerName, boolean passed, String note,
            Long categoryId, String categoryName, boolean isRequired) {
        this.id = id;
        this.name = name;
        this.khmerName = khmerName;
        this.passed = passed;
        this.note = note;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.isRequired = isRequired;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }
}

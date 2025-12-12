package timdev.timdev.dto;


import jakarta.validation.constraints.NotBlank;

public class QuestionDTO {
    
    private String id;
    
    @NotBlank(message = "Question text is required")
    private String text;
    
    private String description;
    
    private int orderIndex;
    
    private boolean required = true;
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
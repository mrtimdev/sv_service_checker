package timdev.timdev.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.entity.Survey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SurveyFormDTO {
    
    private String id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Scale description is required")
    private String scaleDescription;
    
    @NotNull
    private boolean active = true;
    
    private LocalDateTime expiresAt;
    
    @NotEmpty(message = "At least one question group is required")
    private List<QuestionGroupDTO> questionGroups = new ArrayList<>();
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getScaleDescription() { return scaleDescription; }
    public void setScaleDescription(String scaleDescription) { this.scaleDescription = scaleDescription; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public List<QuestionGroupDTO> getQuestionGroups() { return questionGroups; }
    public void setQuestionGroups(List<QuestionGroupDTO> questionGroups) { this.questionGroups = questionGroups; }
    
    // Convert to Entity
    public Survey toEntity() {
        Survey survey = new Survey();
        survey.setTitle(this.title);
        survey.setDescription(this.description);
        survey.setScaleDescription(this.scaleDescription);
        survey.setActive(this.active);
        survey.setExpiresAt(this.expiresAt);
        return survey;
    }
}
package timdev.timdev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "survey")
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String scaleDescription;
    
    @OneToMany(mappedBy = "survey", 
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private final List<QuestionGroup> questionGroups = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @DateTimeFormat(pattern = "MMM dd, yyyy HH:mm")
    @JsonFormat(pattern = "MMM dd, yyyy HH:mm")
    private LocalDateTime expiresAt;
    private boolean active = true;
    
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResponse> responses = new ArrayList<>();
    
    // For generating unique access links
    private String accessCode;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (accessCode == null) {
            accessCode = UUID.randomUUID().toString().substring(0, 8);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for managing bidirectional relationships
    public void addQuestionGroup(QuestionGroup group) {
        questionGroups.add(group);
        group.setSurvey(this);
    }
    
    public void removeQuestionGroup(QuestionGroup group) {
        questionGroups.remove(group);
        group.setSurvey(null);
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getScaleDescription() { return scaleDescription; }
    public void setScaleDescription(String scaleDescription) { this.scaleDescription = scaleDescription; }
    
    public List<QuestionGroup> getQuestionGroups() { return questionGroups; }
    public void setQuestionGroups(List<QuestionGroup> questionGroups) { 
        if (this.questionGroups != null) {
            // Clear existing collection
            this.questionGroups.clear();
            if (questionGroups != null) {
                // Add new items
                this.questionGroups.addAll(questionGroups);
                for (QuestionGroup group : questionGroups) {
                    group.setSurvey(this);
                }
            }
        }
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public List<SurveyResponse> getResponses() { return responses; }
    public void setResponses(List<SurveyResponse> responses) { this.responses = responses; }
    
    public String getAccessCode() { return accessCode; }
    public void setAccessCode(String accessCode) { this.accessCode = accessCode; }
}
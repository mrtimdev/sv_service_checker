package timdev.timdev.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(columnDefinition = "TEXT")
    private String text;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_group_id")
    // @JsonIgnore
    private QuestionGroup questionGroup;
    
    private int orderIndex;
    private boolean required = true;
    
    // Pre-remove to handle relationships
    @PreRemove
    private void preRemove() {
        if (questionGroup != null) {
            questionGroup.getQuestions().remove(this);
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public QuestionGroup getQuestionGroup() { return questionGroup; }
    public void setQuestionGroup(QuestionGroup questionGroup) { this.questionGroup = questionGroup; }
    
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
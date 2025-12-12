package timdev.timdev.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class QuestionGroupDTO {
    
    private String id;
    
    @NotBlank(message = "Group title is required")
    private String title;
    
    private String code;
    
    private int orderIndex;
    
    @NotEmpty(message = "At least one question is required")
    private List<QuestionDTO> questions = new ArrayList<>();
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    
    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
}
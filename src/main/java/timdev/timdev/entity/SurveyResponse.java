package timdev.timdev.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class SurveyResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "survey_id")
    private Survey survey;
    
    @ElementCollection
    @CollectionTable(name = "response_answers", 
                     joinColumns = @JoinColumn(name = "response_id"))
    @MapKeyColumn(name = "question_id")
    @Column(name = "answer_value")
    private Map<String, Integer> answers = new HashMap<>();
    
    @Column(columnDefinition = "TEXT")
    private String comments;
    
    @OneToOne(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private DriverInfo driverInfo;
    
    private LocalDateTime submittedAt;
    private String ipAddress;
    private String userAgent;
    
    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
    
    public Map<String, Integer> getAnswers() { return answers; }
    public void setAnswers(Map<String, Integer> answers) { this.answers = answers; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public DriverInfo getDriverInfo() { return driverInfo; }
    public void setDriverInfo(DriverInfo driverInfo) { this.driverInfo = driverInfo; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
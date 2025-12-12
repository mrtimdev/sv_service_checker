package timdev.timdev.entity;


import jakarta.persistence.*;

@Entity
public class DriverInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String truckNumber;
    private String fullName;
    private String phoneNumber;
    private String email;
    
    @OneToOne
    @JoinColumn(name = "survey_response_id")
    private SurveyResponse surveyResponse;
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTruckNumber() { return truckNumber; }
    public void setTruckNumber(String truckNumber) { this.truckNumber = truckNumber; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public SurveyResponse getSurveyResponse() { return surveyResponse; }
    public void setSurveyResponse(SurveyResponse surveyResponse) { this.surveyResponse = surveyResponse; }
}
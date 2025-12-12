package timdev.timdev.config;



import timdev.timdev.entity.Survey;
import timdev.timdev.service.QuestionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class DataInitializer {
    
    @Autowired
    private QuestionService questionService;
    
    @PostConstruct
    public void init() {
        // Create a default survey if none exists
        // This would normally be done through a database check
        System.out.println("SV TRUCKING Survey System initialized");
        System.out.println("Visit: http://localhost:8080");
        System.out.println("Admin: http://localhost:8080/admin");
    }
}
package timdev.timdev.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Value("${pusher.beams.instance-id}")
    private String beamsInstanceId;

    @Value("${pusher.beams.secret-key}")
    private String beamsSecretKey;
    
    @GetMapping({"", "/"})
    public String handleRootRequest(Authentication authentication, HttpSession session) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard/maintenance";
        }
        // Store Pusher Beams info in session
        session.setAttribute("instanceId", beamsInstanceId);
        session.setAttribute("userId", "1"); // you can change to dynamic user ID if needed
        session.setAttribute("secretKey", beamsSecretKey); 
        return "redirect:/auth/login";
    }

}

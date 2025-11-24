package timdev.timdev.controller;


import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

@Controller
public class NotificationController {

    @MessageMapping("/sendMessage") 
    @SendTo("/topic/notifications") 
    public String sendMessage(String message) throws Exception {
        System.out.println("Received message: " + message); // Debugging log
        return message; // Broadcast the message
    }


    @MessageMapping("/hello")
    @SendToUser("/queue/reply") // sends to the specific user
    public String greet(String message, Principal principal) {
        return "Hello, " + principal.getName() + "! You sent: " + message;
    }

    @GetMapping("/test-ws")
    public String testPage() {
        return "ws-test";
    }

}
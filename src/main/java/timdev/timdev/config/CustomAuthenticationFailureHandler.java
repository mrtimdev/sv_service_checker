package timdev.timdev.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       AuthenticationException exception) throws IOException, ServletException {
        
        String errorMessage = "Invalid username or password";
        String errorDetail = null;
        
        // Determine the specific error
        if (exception instanceof BadCredentialsException) {
            errorMessage = "Invalid username or password";
            errorDetail = "Please check your credentials and try again.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Account deactivated";
            errorDetail = "Your account has been deactivated. Please contact administrator.";
        } else if (exception instanceof AccountExpiredException) {
            errorMessage = "Account expired";
            errorDetail = "Your account has expired. Please contact administrator.";
        } else if (exception instanceof CredentialsExpiredException) {
            errorMessage = "Password expired";
            errorDetail = "Your password has expired. Please reset your password.";
        } else if (exception instanceof LockedException) {
            errorMessage = "Account locked";
            errorDetail = "Your account is locked due to multiple failed attempts. Please try again later or contact administrator.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "User not found";
            errorDetail = "No account found with this username/email.";
        } else if (exception instanceof AuthenticationServiceException) {
            errorMessage = "Authentication error";
            errorDetail = "System error occurred. Please try again later.";
        }
        
        // Store error message in session
        HttpSession session = request.getSession();
        session.setAttribute("error", errorMessage);
        session.setAttribute("errorDetail", errorDetail);
        
        // Determine if it's a warning (not a credentials error)
        if (exception instanceof DisabledException || 
            exception instanceof AccountExpiredException || 
            exception instanceof LockedException) {
            session.setAttribute("errorType", "warning");
        } else {
            session.setAttribute("errorType", "error");
        }
        
        // Store the username for redisplay
        String username = request.getParameter("username");
        if (username != null && !username.trim().isEmpty()) {
            session.setAttribute("lastUsername", username);
        }
        
        // Redirect to login page
        response.sendRedirect("/auth/login?error=true");
    }
}
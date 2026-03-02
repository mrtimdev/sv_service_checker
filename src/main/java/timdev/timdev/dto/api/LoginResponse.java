package timdev.timdev.dto.api;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;
import timdev.timdev.entity.User;
import timdev.timdev.enums.RoleType;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private boolean success;
    private String token;
    private String message;
    private String refreshToken;
    private String username;
    private RoleType role;
    private User user;
    private String identifyError;
    private String passwordError;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIdentifyError() {
        return identifyError;
    }

    public void setIdentifyError(String identifyError) {
        this.identifyError = identifyError;
    }

    public String getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(String passwordError) {
        this.passwordError = passwordError;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    // Static factory methods for responses
    public static LoginResponse success(String token, String refreshToken, User user) {
        LoginResponse response = new LoginResponse(true, "Login successful");
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUser(user);
        return response;
    }

    public static LoginResponse failure(String identifyError, String passwordError) {
        LoginResponse response = new LoginResponse(false, "Login failed");
        response.setIdentifyError(identifyError);
        response.setPasswordError(passwordError);
        return response;
    }
}

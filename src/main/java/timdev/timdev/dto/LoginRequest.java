package timdev.timdev.dto;

import timdev.timdev.validation.ValidIdentifier;

@ValidIdentifier
public class LoginRequest {
    

    private String identifier;
    private String password;

    // Getters and setters
    public String getIdentifier() { return identifier; }
    public String getPassword() { return password; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public void setPassword(String password) { this.password = password; }
}

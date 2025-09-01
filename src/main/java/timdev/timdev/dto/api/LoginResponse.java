package timdev.timdev.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import timdev.timdev.enums.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String token;
    private String username;
    private RoleType role;
    private String identifyError;
    private String passwordError;

    // Success factory method
    public static LoginResponse success(String token, String username, RoleType role) {
        return new LoginResponse(true, token, username, role, null, null);
    }

    // Failure factory method
    public static LoginResponse failure(String identifyError, String passwordError) {
        return new LoginResponse(false, null, null, null, identifyError, passwordError);
    }
}

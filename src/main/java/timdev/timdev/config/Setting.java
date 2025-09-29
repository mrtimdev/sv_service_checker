package timdev.timdev.config;

import org.springframework.stereotype.Component;

import timdev.timdev.enums.RoleType;
import timdev.timdev.enums.ServiceCheckerStatus;

@Component("setting")
public class Setting {

    public String formatStatus(Enum<?> value) {
        if (value == null) return "";

        // Handle RoleType enum
        if (value instanceof RoleType) {
            RoleType roleType = (RoleType) value;
            switch (roleType) {
                case USER:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-blue-500 rounded-md">
                            User
                        </span>
                    """;
                case SUPERVISOR:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-md">
                            Supervisor
                        </span>
                    """;
                case MANAGER:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-md">
                            Manager
                        </span>
                    """;
                case ADMIN:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-md">
                            Admin
                        </span>
                    """;
                default:
                    break;
            }
        }

        // Handle ServiceCheckerStatus enum
        if (value instanceof ServiceCheckerStatus) {
            ServiceCheckerStatus approvalStatus = (ServiceCheckerStatus) value;
            switch (approvalStatus) {
                case CHECKED:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-md">
                            Checked
                        </span>
                    """;
                case UNCHECKED:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-md">
                            Unchecked
                        </span>
                    """;
                case CHECKING:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-md">
                            Checking
                        </span>
                    """;
                default:
                    break;
            }
        }

        // Fallback for any other enum
        return String.format("""
            <span class="px-2 py-1 text-xs font-medium text-gray-700 bg-gray-200 rounded-md">
                %s
            </span>
        """, value.name());
        
    }
    
}
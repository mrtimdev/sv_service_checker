package timdev.timdev.config;

import org.springframework.stereotype.Component;

import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.enums.RoleType;

@Component("setting")
public class Setting {

    public String formatEnum(Enum<?> value) {
        if (value == null) return "";

        // Handle RoleType enum
        if (value instanceof RoleType) {
            RoleType roleType = (RoleType) value;
            switch (roleType) {
                case REPAIRMAN:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-blue-500 rounded-full">
                            Repairman
                        </span>
                    """;
                case SUPERVISOR:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-full">
                            Supervisor
                        </span>
                    """;
                case MANAGER:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-full">
                            Manager
                        </span>
                    """;
                case ADMIN:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-full">
                            Admin
                        </span>
                    """;
                default:
                    break;
            }
        }

        // Handle ApprovalStatus enum
        if (value instanceof ApprovalStatus) {
            ApprovalStatus approvalStatus = (ApprovalStatus) value;
            switch (approvalStatus) {
                case APPROVED:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-full">
                            Approved
                        </span>
                    """;
                case REJECTED:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-full">
                            Rejected
                        </span>
                    """;
                case PENDING:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-full">
                            Pending
                        </span>
                    """;
                case CANCELLED:
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-gray-400 rounded-full">
                            Cancelled
                        </span>
                    """;
                default:
                    break;
            }
        }

        // Fallback for any other enum
        return String.format("""
            <span class="px-2 py-1 text-xs font-medium text-gray-700 bg-gray-200 rounded-full">
                %s
            </span>
        """, value.name());
    }
}
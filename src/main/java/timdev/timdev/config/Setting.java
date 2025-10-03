package timdev.timdev.config;

import org.springframework.stereotype.Component;

import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.enums.InspectionStatus;
import timdev.timdev.enums.OilStatus;
import timdev.timdev.enums.RoleType;

@Component("setting")
public class Setting {

    public String formatStatus(Enum<?> value) {
        if (value == null) return "";

        // Handle RoleType enum
        if (value instanceof RoleType roleType) {
            switch (roleType) {
                case REPAIRMAN -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-blue-500 rounded-md">
                                                       Repairman
                                                   </span>
                                               """;
                }
                case SUPERVISOR -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-md">
                                                       Supervisor
                                                   </span>
                                               """;
                }
                case MANAGER -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-md">
                                                       Manager
                                                   </span>
                                               """;
                }
                case ADMIN -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-md">
                                                       Admin
                                                   </span>
                                               """;
                }
                default -> {
                }
            }
        }

        // Handle ApprovalStatus enum
        if (value instanceof ApprovalStatus approvalStatus) {
            switch (approvalStatus) {
                case APPROVED -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-md">
                                                       Approved
                                                   </span>
                                               """;
                }
                case REJECTED -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-md">
                                                       Rejected
                                                   </span>
                                               """;
                }
                case PENDING -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-md">
                                                       Pending
                                                   </span>
                                               """;
                }
                case CANCELLED -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-gray-400 rounded-md">
                                                       Cancelled
                                                   </span>
                                               """;
                }
                default -> {
                }
            }
        }


        if (value instanceof OilStatus oilStatus) {
            switch (oilStatus) {
                case NOT_CHANGED -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-blue-500 rounded-md">
                                                       Not Changed
                                                   </span>
                                               """;
                }
                case COMPLETED -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-md">
                                                       Completed
                                                   </span>
                                               """;
                }
                case PENDING -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-md">
                                                       Pending
                                                   </span>
                                               """;
                }
                case OVERDUE -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-md">
                                                       Over Due
                                                   </span>
                                               """;
                }
                default -> {
                }
            }
        }

        if (value instanceof InspectionStatus status) {
            switch (status) {
                case COMPLETED -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-md">
                                                       Completed
                                                   </span>
                                               """;
                }
                case ACTIVE -> {
                    return """
                                                   <span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-md">
                                                       Active
                                                   </span>
                                               """;
                }
                case EXPIRED -> {
                    return """
                        <span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-md">
                            Expired
                        </span>
                    """;
                }
                    
                default -> {
                    break;
                }
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
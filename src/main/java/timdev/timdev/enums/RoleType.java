package timdev.timdev.enums;

public enum RoleType {
    REPAIRMAN("Repairman"),
    SUPERVISOR("Supervisor"),
    MANAGER("Manager"),
    ADMIN("Admin");

    private final String label;

    RoleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package timdev.timdev.enums;

public enum RoleType {
    SUPERVISOR("Supervisor"),
    MANAGER("Manager"),
    ADMIN("Admin"),
    USER("User");

    private final String label;

    RoleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

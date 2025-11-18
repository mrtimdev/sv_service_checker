package timdev.timdev.enums;

public enum RoleType {
    USER("User"),
    ADMIN("Admin"),
    MANAGER("Manager");

    private final String label;

    RoleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

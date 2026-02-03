package timdev.timdev.dto;

public enum Measurement {
    SEVERE("ធ្ងន់ធ្ងរ"),
    MODERATE("មធ្យម"),
    MILD("ស្រាល"),
    TRANSFER("ផ្ទេរ");

    private final String label;

    Measurement(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Optional: get enum from name
    public static Measurement fromName(String name) {
        for (Measurement m : values()) {
            if (m.name().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }
}

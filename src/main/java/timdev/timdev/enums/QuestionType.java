package timdev.timdev.enums;

public enum QuestionType {
    SHORT_ANSWER("short_answer"),
    PARAGRAPH("paragraph"),
    DROPDOWN("dropdown"),
    MULTIPLE_CHOICE("multiple_choice"),
    RATING_SCALE("rating_scale");

    private final String value;

    QuestionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static QuestionType fromValue(String value) {
        for (QuestionType type : QuestionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown question type: " + value);
    }
}
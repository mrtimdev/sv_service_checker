package timdev.timdev.exception;

public class DuplicateResourceException extends RuntimeException {
    private final String fieldName;

    public DuplicateResourceException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
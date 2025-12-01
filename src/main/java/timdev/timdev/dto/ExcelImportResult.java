package timdev.timdev.dto;

import java.util.ArrayList;
import java.util.List;

import timdev.timdev.entity.Destination;

public class ExcelImportResult {
    private List<Destination> destinations = new ArrayList<>();
    private List<String> errorMessages = new ArrayList<>();

    public List<Destination> getDestinations() {
        return destinations;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void addError(String msg) {
        errorMessages.add(msg);
    }
}

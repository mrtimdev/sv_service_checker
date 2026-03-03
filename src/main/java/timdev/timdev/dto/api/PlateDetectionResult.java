package timdev.timdev.dto.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class PlateDetectionResult {
    private boolean success;
    private String message;
    private String filename;
    private int platesDetected;
    private int platesRead;
    private List<Map<String, Object>> results;
    private String annotatedImageUrl;
    private double processingTimeMs;

    public static PlateDetectionResult fromMap(Map<String, Object> map) {
        PlateDetectionResult result = new PlateDetectionResult();
        result.setSuccess((Boolean) map.getOrDefault("success", false));
        result.setMessage((String) map.get("message"));
        result.setFilename((String) map.get("filename"));
        result.setPlatesDetected((Integer) map.getOrDefault("plates_detected", 0));
        result.setPlatesRead((Integer) map.getOrDefault("plates_read", 0));
        result.setResults((List<Map<String, Object>>) map.getOrDefault("results", new ArrayList<>()));
        result.setAnnotatedImageUrl((String) map.get("annotated_image_url"));
        result.setProcessingTimeMs((Double) map.getOrDefault("processing_time_ms", 0.0));
        return result;
    }

    public String getAnnotatedImageFileName() {
        if (annotatedImageUrl != null && annotatedImageUrl.contains("/")) {
            return annotatedImageUrl.substring(annotatedImageUrl.lastIndexOf("/") + 1);
        }
        return null;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getPlatesDetected() {
        return platesDetected;
    }

    public void setPlatesDetected(int platesDetected) {
        this.platesDetected = platesDetected;
    }

    public int getPlatesRead() {
        return platesRead;
    }

    public void setPlatesRead(int platesRead) {
        this.platesRead = platesRead;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public void setResults(List<Map<String, Object>> results) {
        this.results = results;
    }

    public String getAnnotatedImageUrl() {
        return annotatedImageUrl;
    }

    public void setAnnotatedImageUrl(String annotatedImageUrl) {
        this.annotatedImageUrl = annotatedImageUrl;
    }

    public double getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(double processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
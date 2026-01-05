// ImportNotificationDTO.java
package timdev.timdev.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ImportNotificationDTO {
    
    private String importType;
    private int successCount;
    private int errorCount;
    private String user;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime importTime;
    
    private String fileName;
    private String summary;
    
    // Constructors
    public ImportNotificationDTO() {}
    
    public ImportNotificationDTO(String importType, int successCount, int errorCount, 
                                String user, String fileName) {
        this.importType = importType;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.user = user;
        this.importTime = LocalDateTime.now();
        this.fileName = fileName;
        this.summary = generateSummary();
    }
    
    // Getters and Setters
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    
    public LocalDateTime getImportTime() { return importTime; }
    public void setImportTime(LocalDateTime importTime) { this.importTime = importTime; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    private String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Import Type: ").append(importType).append("\n");
        sb.append("File: ").append(fileName).append("\n");
        sb.append("Success: ").append(successCount).append(" rows\n");
        sb.append("Errors: ").append(errorCount).append(" rows\n");
        sb.append("User: ").append(user).append("\n");
        sb.append("Time: ").append(importTime);
        return sb.toString();
    }
    
    public String toTelegramMessage() {
        String statusEmoji = errorCount > 0 ? "⚠️" : "✅";
        String title = statusEmoji + " *" + importType + " Import Completed*";
        
        return String.format(
            "%s\n\n" +
            "📄 *File:* %s\n" +
            "👤 *User:* %s\n" +
            "⏰ *Time:* %s\n\n" +
            "📊 *Results:*\n" +
            "• ✅ Success: %d rows\n" +
            "• ❌ Errors: %d rows\n\n" +
            "_Import completed via Excel upload_",
            title,
            fileName,
            user,
            importTime.format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")),
            successCount,
            errorCount
        );
    }
}
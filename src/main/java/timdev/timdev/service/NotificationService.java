package timdev.timdev.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    @Autowired
    private TelegramSettingService telegramSettingService;
    
    /**
     * Send a simple text notification
     */
    public void sendSimpleNotification(String message) {
        telegramSettingService.sendNotification(message);
    }
    
    /**
     * Send HTML formatted notification
     */
    public void sendHtmlNotification(String title, String content) {
        LocalDateTime currDateTime = java.time.LocalDateTime.now();
        String dateString = currDateTime
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
        String htmlMessage = String.format(
            "<b>%s</b>\n\n%s\n\n<i>Sent at: %s</i>",
            title,
            content,
            dateString
        );
        telegramSettingService.sendFormattedNotification(htmlMessage, "HTML");
    }
    
    /**
     * Send Markdown formatted notification
     */
    public void sendMarkdownNotification(String title, String content) {
        LocalDateTime currDateTime = java.time.LocalDateTime.now();
        String dateString = currDateTime
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
        String markdownMessage = String.format(
            "*%s*\n\n%s\n\n_Sent at: %s_",
            title,
            content,
            dateString
        );
        telegramSettingService.sendFormattedNotification(markdownMessage, "Markdown");
    }
    
    /**
     * Send error notification
     */
    public void sendErrorNotification(String errorMessage, String context) {
        LocalDateTime currDateTime = java.time.LocalDateTime.now();
        String dateString = currDateTime
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
        String errorNotification = String.format(
            "🚨 *Error Alert*\n\n" +
            "Context: %s\n" +
            "Error: %s\n\n" +
            "Time: %s",
            context,
            errorMessage,
            dateString
        );
        telegramSettingService.sendFormattedNotification(errorNotification, "Markdown");
    }
    
    /**
     * Send success notification
     */
    public void sendSuccessNotification(String successMessage, String context) {
        LocalDateTime currDateTime = java.time.LocalDateTime.now();
        String dateString = currDateTime
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
        String successNotification = String.format(
            "✅ *Success Notification*\n\n" +
            "Context: %s\n" +
            "Message: %s\n\n" +
            "Time: %s",
            context,
            successMessage,
            dateString
        );
        telegramSettingService.sendFormattedNotification(successNotification, "Markdown");
    }
    
    /**
     * Send custom notification with custom parse mode
     */
    public void sendCustomNotification(String message, String parseMode) {
        telegramSettingService.sendFormattedNotification(message, parseMode);
    }
}
package timdev.timdev.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import jakarta.annotation.PostConstruct;
import timdev.timdev.entity.TelegramSetting;
import timdev.timdev.service.TelegramBotService;
import timdev.timdev.service.TelegramSettingService;

import java.util.Optional;

@Configuration
public class TelegramBotConfig {
    
    @Autowired
    private TelegramSettingService telegramSettingService;
    
    @Autowired
    private TelegramBotService telegramBotService;
    
    @PostConstruct
    public void init() {
        // Start bot on application startup if there's an active setting
        Optional<TelegramSetting> activeSetting = telegramSettingService.getActiveSetting();
        // activeSetting.ifPresent(this::startOrRestartBot);
    }
    
    public void startOrRestartBot(TelegramSetting setting) {
        try {
            // Reset existing bot if any
            telegramBotService.reset();
            
            // Initialize with new settings
            telegramBotService.initialize(setting.getBotToken(), setting.getBotName());
            
            // Register the bot with Telegram API
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
            
            // Send startup notification if chat ID is configured
            if (setting.getChatId() != null && !setting.getChatId().isEmpty()) {
                try {
                    Long chatId = Long.valueOf(setting.getChatId());
                    String startupMessage = "🚀 Bot started successfully!\n" +
                            "Bot: @" + setting.getBotName() + "\n" +
                            "Time: " + java.time.LocalDateTime.now();
                    telegramBotService.sendMessage(chatId, startupMessage);
                } catch (NumberFormatException e) {
                    // Handle non-numeric chat IDs (like group usernames)
                    telegramBotService.sendMessage(-1L, "Bot started but chat ID format might be incorrect");
                }
            }
            
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to start Telegram bot", e);
        }
    }
}
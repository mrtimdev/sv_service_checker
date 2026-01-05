package timdev.timdev.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import jakarta.transaction.Transactional;
import timdev.timdev.entity.TelegramSetting;
import timdev.timdev.repository.TelegramSettingRepository;

@Service
public class TelegramSettingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramSettingService.class);
    
    @Autowired
    private TelegramSettingRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    
    @Autowired
    private TelegramBotService telegramBotService;
    
    public List<TelegramSetting> getAllSettings() {
        return repository.findAll();
    }
    
    public Optional<TelegramSetting> getSettingById(Long id) {
        return repository.findById(id);
    }

    public TelegramSetting save(TelegramSetting setting) {
        return repository.save(setting);
    }

    
    public Optional<TelegramSetting> getActiveSetting() {
        List<TelegramSetting> activeSettings = repository.findByIsActiveTrue();
        return activeSettings.isEmpty() ? Optional.empty() : Optional.of(activeSettings.get(0));
    }
    
    @Transactional
    public TelegramSetting saveSetting(TelegramSetting setting) {
        // If this is the first setting or being activated, ensure only one active setting
        if (Boolean.TRUE.equals(setting.getIsActive())) {
            deactivateAllSettings();
        }
        return repository.save(setting);
    }
    
    @Transactional
    public TelegramSetting updateSetting(Long id, TelegramSetting settingDetails) {
        TelegramSetting setting = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Telegram setting not found with id: " + id));
        
        // If activating this setting, deactivate all others
        if (Boolean.TRUE.equals(settingDetails.getIsActive()) && 
            !Boolean.TRUE.equals(setting.getIsActive())) {
            deactivateAllSettings();
        }
        
        setting.setBotName(settingDetails.getBotName());
        setting.setBotToken(settingDetails.getBotToken());
        setting.setWebhookUrl(settingDetails.getWebhookUrl());
        setting.setChatId(settingDetails.getChatId());
        setting.setIsActive(settingDetails.getIsActive());
        setting.setDescription(settingDetails.getDescription());
        setting.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(setting);
    }
    
    @Transactional
    public void deleteSetting(Long id) {
        repository.deleteById(id);
    }
    
    @Transactional
    public TelegramSetting activateSetting(Long id, Boolean active) {
        TelegramSetting setting = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Telegram setting not found with id: " + id));
        
        // Deactivate all settings first
        deactivateAllSettings();
        
        // Activate this setting
        setting.setIsActive(active);
        setting.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(setting);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Map<String, Object> verifyTelegramBot(String token) {
        String url = "https://api.telegram.org/bot" + token + "/getMe";

        try {
            ResponseEntity<Map> response =
                    restTemplate.getForEntity(url, Map.class);

            Map<String, Object> body = response.getBody();

            if (body == null || !(Boolean) body.get("ok")) {
                throw new RuntimeException("Invalid bot token");
            }

            return (Map<String, Object>) body.get("result");

        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Telegram bot token", e);
        }
    }
    
    @Transactional
    public void deactivateAllSettings() {
        List<TelegramSetting> activeSettings = repository.findByIsActiveTrue();
        for (TelegramSetting setting : activeSettings) {
            setting.setIsActive(false);
            setting.setUpdatedAt(LocalDateTime.now());
            repository.save(setting);
        }
    }
    
    public boolean testConnection(String botToken, String chatId) {
        try {
            // Create a simple bot instance to test the token
            TestBot bot = new TestBot(new DefaultBotOptions(), botToken);
            
            // Try to send a test message
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Test connection successful! Bot is working properly.");
            
            bot.execute(message);
            return true;
        } catch (TelegramApiException e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean validateBotToken(String botToken) {
        // Simple validation - you can add more complex validation
        return botToken != null && botToken.matches("\\d+:[-_a-zA-Z0-9]+");
    }
    
    // =========== NEW METHODS FOR NOTIFICATION SERVICE ===========
    
    /**
     * Send a simple text notification
     */
    public void sendNotification(String message) {
        if (!telegramBotService.isInitialized()) {
            logger.warn("Bot not initialized, cannot send notification");
            return;
        }
        
        List<TelegramSetting> activeSettings = repository.findByIsActiveTrue();
        for (TelegramSetting setting : activeSettings) {
            if (setting.getChatId() != null && !setting.getChatId().isEmpty()) {
                try {
                    Long chatId = Long.valueOf(setting.getChatId());
                    telegramBotService.sendMessage(chatId, message);
                } catch (NumberFormatException e) {
                    logger.error("Invalid chat ID format for setting {}: {}", setting.getId(), setting.getChatId());
                }
            }
        }
    }
    
    /**
     * Send a formatted notification
     */
    public void sendFormattedNotification(String message, String parseMode) {
        // if (!telegramBotService.isInitialized()) {
        //     logger.warn("Bot not initialized, cannot send notification");
        //     return;
        // }
        
        List<TelegramSetting> activeSettings = repository.findByIsActiveTrue();
        for (TelegramSetting setting : activeSettings) {
            if (setting.getChatId() != null && !setting.getChatId().isEmpty()) {
                try {
                    Long chatId = Long.valueOf(setting.getChatId());
                    telegramBotService.initialize(setting.getBotToken(), setting.getBotName());
                    telegramBotService.sendFormattedMessage(chatId, message, parseMode);
                } catch (NumberFormatException e) {
                    logger.error("Invalid chat ID format for setting {}: {}", setting.getId(), setting.getChatId());
                }
            }
        }
    }

    public long countVerified() {
        return repository.countByIsVerifiedTrue();
    }

    public long countNotVerified() {
        return repository.countByIsVerifiedFalse();
    }

    public long countNotActived() {
        return repository.countByIsActiveTrue();
    }

    public long countActived() {
        return repository.countByIsActiveTrue();
    }
    
    // Inner class for testing connection
    private static class TestBot extends org.telegram.telegrambots.bots.TelegramLongPollingBot {
        private final String botToken;
        
        public TestBot(DefaultBotOptions options, String botToken) {
            super(options, botToken);
            this.botToken = botToken;
        }
        
        @Override
        public String getBotToken() {
            return botToken;
        }
        
        @Override
        public void onUpdateReceived(org.telegram.telegrambots.meta.api.objects.Update update) {
            // Not needed for testing connection
        }
        
        @Override
        public String getBotUsername() {
            return "TestBot";
        }


        
    }
}
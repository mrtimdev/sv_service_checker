package timdev.timdev.service;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TelegramBotService extends TelegramLongPollingBot {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    
    private String botUsername;
    private String botToken;
    private boolean isInitialized = false;
    
    // Initialize the bot with credentials
    public void initialize(String botToken, String botUsername) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.isInitialized = true;
        logger.info("Telegram bot initialized: {}", botUsername);
    }
    
    @Override
    public String getBotUsername() {
        if (!isInitialized) {
            throw new IllegalStateException("Bot not initialized. Call initialize() first.");
        }
        return botUsername;
    }
    
    @Override
    public String getBotToken() {
        if (!isInitialized) {
            throw new IllegalStateException("Bot not initialized. Call initialize() first.");
        }
        return botToken;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (!isInitialized) return;
        
        // Check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();
            
            logger.info("Received message from {} ({}): {}", userName, chatId, messageText);
            
            // Handle commands
            switch (messageText) {
                case "/start" -> sendWelcomeMessage(chatId, userName);
                case "/help" -> sendHelpMessage(chatId);
                case "/status" -> sendStatusMessage(chatId);
                default -> sendReply(chatId, "I received: " + messageText);
            }
        }
    }
    
    // Send a message to a specific chat ID
    public void sendMessage(Long chatId, String text) {
        if (!isInitialized || chatId == null) return;
        
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        
        try {
            execute(message);
            logger.info("Message sent to chat ID {}: {}", chatId, text);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chat ID {}: {}", chatId, e.getMessage());
        }
    }
    
    // Send a formatted message
    public void sendFormattedMessage(Long chatId, String text, String parseMode) {
        if (!isInitialized || chatId == null) return;
        
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode(parseMode); // "HTML" or "Markdown"
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send formatted message: {}", e.getMessage());
        }
    }
    
    // Send welcome message
    private void sendWelcomeMessage(long chatId, String userName) {
        String welcomeMessage = String.format(
            "👋 Welcome *%s*!\n\n" +
            "I'm your notification bot.\n" +
            "Here are available commands:\n" +
            "/start - Show welcome message\n" +
            "/help - Show help\n" +
            "/status - Check bot status\n\n" +
            "Bot: @%s",
            userName, getBotUsername()
        );
        
        sendFormattedMessage(chatId, welcomeMessage, "Markdown");
    }
    
    // Send help message
    private void sendHelpMessage(long chatId) {
        String helpMessage = 
            "📚 *Help Guide*\n\n" +
            "This bot can send notifications from your application.\n\n" +
            "*Setup Instructions:*\n" +
            "1. Add this bot to your chat/group\n" +
            "2. Copy the Chat ID from your application settings\n" +
            "3. Configure the bot in your admin panel\n\n" +
            "*Commands:*\n" +
            "• /start - Welcome message\n" +
            "• /help - This help message\n" +
            "• /status - Bot status";
        
        sendFormattedMessage(chatId, helpMessage, "Markdown");
    }
    
    // Send status message
    private void sendStatusMessage(long chatId) {
        String statusMessage = 
            "✅ *Bot Status*\n\n" +
            "• Status: Active\n" +
            "• Username: @" + getBotUsername() + "\n" +
            "• Initialized: " + (isInitialized ? "Yes" : "No") + "\n\n" +
            "Ready to receive notifications!";
        
        sendFormattedMessage(chatId, statusMessage, "Markdown");
    }
    
    // Send simple reply
    private void sendReply(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send reply: {}", e.getMessage());
        }
    }
    
    // Check if bot is initialized
    public boolean isInitialized() {
        return isInitialized;
    }
    
    // Reset the bot (for changing tokens)
    public void reset() {
        this.botToken = null;
        this.botUsername = null;
        this.isInitialized = false;
        logger.info("Telegram bot reset");
    }
}
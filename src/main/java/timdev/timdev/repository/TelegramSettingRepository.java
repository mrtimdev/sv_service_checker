package timdev.timdev.repository;

import timdev.timdev.entity.TelegramSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelegramSettingRepository extends JpaRepository<TelegramSetting, Long> {
    
    Optional<TelegramSetting> findByBotToken(String botToken);
    
    List<TelegramSetting> findByIsActiveTrue();
    
    Optional<TelegramSetting> findByBotName(String botName);
    
    boolean existsByBotToken(String botToken);
    
    boolean existsByBotName(String botName);
    
    @Query("SELECT t FROM TelegramSetting t WHERE t.isActive = true AND t.chatId IS NOT NULL AND t.chatId != ''")
    List<TelegramSetting> findActiveSettingsWithChatId();


    long countByIsVerifiedTrue();

    long countByIsVerifiedFalse();
    long countByIsActiveTrue();
    long countByIsActiveFalse();
}
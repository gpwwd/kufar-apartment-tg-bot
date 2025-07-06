package com.innowise.aikufarbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.innowise.aikufarbot.bot.KufarBot;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class TelegramBotConfig {
    private String token;

    private String username;

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(KufarBot kufarBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(kufarBot);
        return api;
    }
} 
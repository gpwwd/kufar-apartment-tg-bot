package com.innowise.aikufarbot.bot;

import com.innowise.aikufarbot.config.TelegramBotConfig;
import com.innowise.aikufarbot.model.Apartment;
import com.innowise.aikufarbot.model.Subscription;
import com.innowise.aikufarbot.repository.SubscriptionRepository;
import com.innowise.aikufarbot.service.KufarApiService;
import com.innowise.aikufarbot.service.ApartmentMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class KufarBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(KufarBot.class);
    
    private final TelegramBotConfig config;
    private final KufarApiService kufarService;
    private final SubscriptionRepository subscriptionRepository;
    private final ApartmentMonitoringService apartmentMonitoringService;

    public KufarBot(TelegramBotConfig config, KufarApiService kufarService, SubscriptionRepository subscriptionRepository, ApartmentMonitoringService apartmentMonitoringService) {
        super(config.getToken());
        this.config = config;
        this.kufarService = kufarService;
        this.subscriptionRepository = subscriptionRepository;
        this.apartmentMonitoringService = apartmentMonitoringService;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Received update: {}", update);
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();
                log.info("Received message: {} from chat: {}", messageText, chatId);

                switch (messageText) {
                    case "/start":
                        handleStart(chatId);
                        break;
                    case "/latest":
                        handleLatest(chatId);
                        break;
                    case "/subscribe":
                        handleSubscribe(chatId);
                        break;
                    case "/unsubscribe":
                        handleUnsubscribe(chatId);
                        break;
                    default:
                        handleUnknownCommand(chatId);
                }
            }
        } catch (TelegramApiException e) {
            log.error("Error processing update: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRateString = "${kufar.check-interval}")
    public void checkNewApartments() {
        List<Apartment> newApartments = apartmentMonitoringService.checkNewApartments();
        if (newApartments != null) {
            List<Subscription> activeSubscriptions = subscriptionRepository.findByActiveTrue();
            if (!activeSubscriptions.isEmpty()) {
                sendNewApartmentsNotification(newApartments, activeSubscriptions);
            } else {
                log.info("No active subscribers to notify");
            }
        }
    }

    private void sendNewApartmentsNotification(List<Apartment> newApartments, List<Subscription> subscriptions) {
        for (int i = 0; i < newApartments.size(); i += 5) {
            StringBuilder message = new StringBuilder("🆕 Новые объявления:\n\n");
            int end = Math.min(i + 5, newApartments.size());

            newApartments.subList(i, end).forEach(apartment -> 
                message.append(String.format("🏠 %s\n💰 %s %s\n📍 %s\n🔗 %s\n\n %s\n\n",
                    apartment.getAddress(),
                    apartment.getPriceBYN(),
                    apartment.getPriceUSD(),
                    apartment.getParameters(),
                    apartment.getUrl(),
                    apartment.getPostedDate()
                ))
            );

            String finalMessage = message.toString();
            for (Subscription subscription : subscriptions) {
                try {
                    sendMessage(subscription.getChatId(), finalMessage);
                } catch (TelegramApiException e) {
                    log.error("Error sending notification to chat {}: {}", subscription.getChatId(), e.getMessage());
                }
            }
        }
    }

    private void handleStart(long chatId) throws TelegramApiException {
        String message = "Привет! Я помогу тебе следить за новыми объявлениями об аренде квартир на Kufar.\n\n" +
                "Доступные команды:\n" +
                "/latest - показать последние объявления\n" +
                "/subscribe - подписаться на уведомления\n" +
                "/unsubscribe - отписаться от уведомлений";

        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText(message);
        response.setReplyMarkup(createMainKeyboard());

        execute(response);
    }

    private void handleLatest(long chatId) throws TelegramApiException {
        List<Apartment> apartments = kufarService.getHtmlByUrl();
        
        if (apartments.isEmpty()) {
            sendMessage(chatId, "Не удалось найти объявления 😢");
            return;
        }

        for (int i = 0; i < apartments.size(); i += 5) {
            StringBuilder message = new StringBuilder();
            int end = Math.min(i + 5, apartments.size());
            
            message.append(String.format("Объявления %d-%d из %d:\n\n", i + 1, end, apartments.size()));
            
            apartments.subList(i, end).forEach(apartment -> 
                message.append(String.format("🏠 %s\n💰 %s %s\n📍 %s\n🔗 %s\n\n %s",
                    apartment.getAddress(),
                    apartment.getPriceBYN(),
                    apartment.getPriceUSD(),
                    apartment.getParameters(),
                    apartment.getUrl(),
                    apartment.getPostedDate()
                ))
            );
            
            try {
                sendMessage(chatId, message.toString());
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("Error sending message chunk: " + e.getMessage());
            }
        }
    }

    private void handleSubscribe(long chatId) throws TelegramApiException {
        Optional<Subscription> existingSubscription = subscriptionRepository.findById(chatId);
        
        if (existingSubscription.isPresent()) {
            Subscription subscription = existingSubscription.get();
            if (subscription.isActive()) {
                sendMessage(chatId, "Вы уже подписаны на уведомления!");
            } else {
                subscription.setActive(true);
                subscriptionRepository.save(subscription);
                sendMessage(chatId, "Вы успешно возобновили подписку на уведомления!");
            }
        } else {
            subscriptionRepository.save(new Subscription(chatId));
            sendMessage(chatId, "Вы успешно подписались на уведомления о новых объявлениях!");
        }
    }

    private void handleUnsubscribe(long chatId) throws TelegramApiException {
        Optional<Subscription> subscription = subscriptionRepository.findById(chatId);
        
        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            sub.setActive(false);
            subscriptionRepository.save(sub);
            sendMessage(chatId, "Вы успешно отписались от уведомлений!");
        } else {
            sendMessage(chatId, "Вы не были подписаны на уведомления!");
        }
    }

    private void handleUnknownCommand(long chatId) throws TelegramApiException {
        sendMessage(chatId, "Неизвестная команда. Используйте /start для просмотра доступных команд.");
    }

    public void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(message);
    }

    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setSelective(true);

        List<KeyboardRow> rows = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add("/latest");
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add("/subscribe");
        row2.add("/unsubscribe");

        rows.add(row1);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }
} 
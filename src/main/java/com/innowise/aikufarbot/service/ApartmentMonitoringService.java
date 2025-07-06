package com.innowise.aikufarbot.service;

import com.innowise.aikufarbot.bot.KufarBot;
import com.innowise.aikufarbot.model.Apartment;
import com.innowise.aikufarbot.model.Subscription;
import com.innowise.aikufarbot.repository.ApartmentRepository;
import com.innowise.aikufarbot.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApartmentMonitoringService {

    private final KufarApiService kufarApiService;
    private final ApartmentRepository apartmentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final KufarBot kufarBot;

    @Scheduled(fixedRateString = "${kufar.check-interval}")
    public void checkNewApartments() {
        log.info("Checking for new apartments...");

        List<Apartment> latestApartments = kufarApiService.getHtmlByUrl();
        if (latestApartments == null || latestApartments.isEmpty()) {
            log.warn("No apartments fetched from Kufar");
            return;
        }

        List<String> savedUrls = apartmentRepository.findAll()
                .stream()
                .map(Apartment::getUrl)
                .toList();

        List<Apartment> newApartments = latestApartments.stream()
                .filter(apartment -> !savedUrls.contains(apartment.getUrl()))
                .collect(Collectors.toList());

        if (!newApartments.isEmpty()) {
            log.info("Found {} new apartments", newApartments.size());
            
            apartmentRepository.saveAll(newApartments);

            List<Subscription> activeSubscriptions = subscriptionRepository.findByActiveTrue();
            if (!activeSubscriptions.isEmpty()) {
                sendNewApartmentsNotification(newApartments, activeSubscriptions);
            } else {
                log.info("No active subscribers to notify");
            }
        } else {
            log.info("No new apartments found");
        }
    }

    private void sendNewApartmentsNotification(List<Apartment> newApartments, List<Subscription> subscriptions) {
        StringBuilder message = new StringBuilder("🆕 Новые объявления:\n\n");

        for (Apartment apartment : newApartments) {
            message.append(String.format("🏠 %s\n💰 %s %s\n📍 %s\n🔗 %s\n\n %s\n\n",
                    apartment.getAddress(),
                    apartment.getPriceBYN(),
                    apartment.getPriceUSD(),
                    apartment.getParameters(),
                    apartment.getUrl(),
                    apartment.getPostedDate()
            ));
        }

        String finalMessage = message.toString();
        for (Subscription subscription : subscriptions) {
            try {
                kufarBot.sendMessage(subscription.getChatId(), finalMessage);
            } catch (TelegramApiException e) {
                log.error("Error sending notification to chat {}: {}", subscription.getChatId(), e.getMessage());
            }
        }
    }
} 
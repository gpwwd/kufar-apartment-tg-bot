package com.innowise.aikufarbot.service;

import com.innowise.aikufarbot.model.Apartment;
import com.innowise.aikufarbot.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApartmentMonitoringService {

    private final KufarApiService kufarApiService;
    private final ApartmentRepository apartmentRepository;

    public List<Apartment> checkNewApartments() {
        log.info("Checking for new apartments...");

        List<Apartment> latestApartments = kufarApiService.getHtmlByUrl();
        if (latestApartments == null || latestApartments.isEmpty()) {
            log.warn("No apartments fetched from Kufar");
            return null;
        }

        List<String> savedIds = apartmentRepository.findAll()
                .stream()
                .map(Apartment::getId)
                .toList();

        List<Apartment> newApartments = latestApartments.stream()
                .filter(apartment -> !savedIds.contains(apartment.getId()))
                .collect(Collectors.toList());

        if (!newApartments.isEmpty()) {
            log.info("Found {} new apartments", newApartments.size());
            
            apartmentRepository.saveAll(newApartments);

            return newApartments;
        } else {
            log.info("No new apartments found");
            return null;
        }
    }
} 
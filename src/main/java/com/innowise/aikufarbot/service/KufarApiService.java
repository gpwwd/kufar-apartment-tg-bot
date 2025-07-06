package com.innowise.aikufarbot.service;

import com.innowise.aikufarbot.config.KufarApiConfig;
import com.innowise.aikufarbot.model.Apartment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KufarApiService {
    
    private final ApartmentParser apartmentParser;
    private final KufarApiConfig config;

    public List<Apartment> getHtmlByUrl() {
        try {
            Document document = Jsoup.connect(buildUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            String html = document.html();
            List<Apartment> apartments = apartmentParser.parseApartments(html);
            return apartments;
        } catch (IOException e) {
            log.error("Error fetching HTML from URL: {}", e.getMessage());
            return null;
        }
    }

    private String buildUrl() 
    {
        int lowerPrice = Integer.parseInt(config.getLowerPrice());
        int upperPrice = Integer.parseInt(config.getUpperPrice());
        String rawPriceString = String.format("r:%d,%d", lowerPrice, upperPrice);

        return UriComponentsBuilder.fromUriString(config.getBaseUrl())
                .queryParam("prc", rawPriceString)
                .build()
                .toUriString();
    }
} 
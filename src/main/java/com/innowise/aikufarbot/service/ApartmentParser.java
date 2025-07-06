package com.innowise.aikufarbot.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.innowise.aikufarbot.model.Apartment;
import org.springframework.stereotype.Component;

@Component
public class ApartmentParser {

    public List<Apartment> parseApartments(String html) {
        List<Apartment> apartments = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements apartmentSections = doc.select("div.styles_cards__HMGBx section");

        for (Element section : apartmentSections) {
            String id = null;
            String priceBYN = null;
            String priceUSD = null;
            String parameters = null;
            String address = null;
            String metro = null;
            String description = null;
            String url = null;
            String postedDate = null;

            Element linkElement = section.selectFirst("a.styles_wrapper__Q06m9");
            if (linkElement != null) {
                url = linkElement.attr("href");
                Pattern idPattern = Pattern.compile("vi/minsk/snyat/kvartiru/[a-zA-Z0-9-]+/(\\d+)");
                Matcher matcher = idPattern.matcher(url);
                if (matcher.find()) {
                    id = matcher.group(1);
                } else {
                    idPattern = Pattern.compile("vi/minsk/snyat/kvartiru/(\\d+)");
                    matcher = idPattern.matcher(url);
                    if (matcher.find()) {
                        id = matcher.group(1);
                    }
                }
            }

            Element priceByrElement = section.selectFirst("span.styles_price__byr__lLSfd");
            if (priceByrElement != null) {
                priceBYN = priceByrElement.text().trim();
            }
            Element priceUsdElement = section.selectFirst("span.styles_price__usd__HpXMa");
            if (priceUsdElement != null) {
                priceUSD = priceUsdElement.text().trim();
            }

            Element parametersElement = section.selectFirst("div.styles_parameters__7zKlL");
            if (parametersElement != null) {
                parameters = parametersElement.text().trim();
            }

            Element addressElement = section.selectFirst("span.styles_address__l6Qe_");
            if (addressElement != null) {
                address = addressElement.text().trim();
            }

            Element metroElement = section.selectFirst("div.styles_wrapper__HKXX4 span");
            if (metroElement != null) {
                metro = metroElement.text().trim();
            }

            Element descriptionElement = section.selectFirst("p.styles_body__5BrnC");
            if (descriptionElement != null) {
                description = descriptionElement.text().trim();
            }

            Element dateSpan = section.selectFirst("div.styles_date__SSUVP span");
            if (dateSpan != null) {
                postedDate = dateSpan.text().trim();
            } else {
                postedDate = "N/A";
            }
            if (id != null) {
                apartments.add(new Apartment(id, priceBYN, priceUSD, parameters, address, metro, description, url, postedDate));
            }
        }
        return apartments;
    }
}
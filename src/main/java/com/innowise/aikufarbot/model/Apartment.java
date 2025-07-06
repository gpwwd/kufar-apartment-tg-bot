package com.innowise.aikufarbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "apartments")
public class Apartment {
    @Id
    private String id;
    private String priceBYN;
    private String priceUSD;
    private String parameters;
    private String address;
    private String metro;
    private String description;
    private String url;
    private String postedDate;

    public Apartment(String id, String priceBYN, String priceUSD, String parameters, String address, String metro, String description, String url, String postedDate) {
        this.id = id;
        this.priceBYN = priceBYN;
        this.priceUSD = priceUSD;
        this.parameters = parameters;
        this.address = address;
        this.metro = metro;
        this.description = description;
        this.url = url;
        this.postedDate = postedDate;
    }

    @Override
    public String toString() {
        return "Apartment{" +
                "id='" + id + '\'' +
                ", priceBYN='" + priceBYN + '\'' +
                ", priceUSD='" + priceUSD + '\'' +
                ", parameters='" + parameters + '\'' +
                ", address='" + address + '\'' +
                ", metro='" + metro + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", postedDate='" + postedDate + '\'' + 
                '}';
    }
}
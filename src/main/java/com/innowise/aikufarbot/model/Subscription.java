package com.innowise.aikufarbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "subscriptions")
public class Subscription {
    @Id
    private Long chatId;
    private boolean active;

    public Subscription(Long chatId) {
        this.chatId = chatId;
        this.active = true;
    }
} 
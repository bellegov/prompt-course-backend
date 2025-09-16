package com.promptcourse.user_service.dto;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    // В будущем здесь можно будет передавать тип подписки (месяц, год)
    // Пока для простоты оставляем пустым
    private String subscriptionType;
}
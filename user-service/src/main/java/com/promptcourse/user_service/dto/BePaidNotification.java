package com.promptcourse.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BePaidNotification {
    // bePaid присылает данные о транзакции в корне объекта
    private Transaction transaction;

    @Data
    public static class Transaction {
        @JsonProperty("customer_id")
        private String customerId; // Сюда мы будем класть наш userId
        private String status;
        private String message;
        @JsonProperty("tracking_id")
        private String trackingId; // Уникальный ID платежа в нашей системе
        private Amount amount;
        private String uid; // Уникальный ID транзакции в системе bePaid
    }

    @Data
    public static class Amount {
        private String currency;
        private Integer total; // Сумма в копейках
    }
}

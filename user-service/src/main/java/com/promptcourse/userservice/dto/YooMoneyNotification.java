package com.promptcourse.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class YooMoneyNotification {
    private String type;
    private String event;
    @JsonProperty("object") // "object" - зарезервированное слово, используем аннотацию
    private PaymentObject object;

    @Data
    public static class PaymentObject {
        private String id;
        private String status;
        private Amount amount;
        private Map<String, String> metadata; // Сюда мы будем класть наш userId
        private boolean paid;
        // ... можно добавить другие поля из документации YooMoney, если понадобятся
    }

    @Data
    public static class Amount {
        private String value;
        private String currency;
    }
}

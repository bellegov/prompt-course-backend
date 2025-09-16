package com.promptcourse.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class YooMoneyNotification {
    private String type;
    private String event;
    @JsonProperty("object")
    private PaymentObject object;

    @Data
    public static class PaymentObject {
        private String id;
        private String status;
        private Amount amount;
        private Map<String, String> metadata;
        private boolean paid;

    }

    @Data
    public static class Amount {
        private String value;
        private String currency;
    }
}

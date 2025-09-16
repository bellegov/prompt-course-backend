package com.promptcourse.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponse {
    private String confirmationUrl; // Ссылка на оплату, которую нужно открыть пользователю
    private String paymentId;       // ID платежа в системе YooMoney
}

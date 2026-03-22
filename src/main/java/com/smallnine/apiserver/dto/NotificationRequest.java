package com.smallnine.apiserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank(message = "通知內容不能為空")
    private String message;
}

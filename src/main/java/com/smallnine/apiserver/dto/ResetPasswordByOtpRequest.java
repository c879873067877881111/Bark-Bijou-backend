package com.smallnine.apiserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordByOtpRequest {
    @NotBlank(message = "secret 不能為空")
    private String secret;

    @NotBlank(message = "驗證碼不能為空")
    private String otpToken;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, message = "密碼長度至少6位")
    private String newPassword;
}
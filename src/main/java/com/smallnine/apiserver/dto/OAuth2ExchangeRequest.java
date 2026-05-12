package com.smallnine.apiserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "OAuth2 一次性 code 換 token 請求")
public class OAuth2ExchangeRequest {

    @NotBlank(message = "code 不可為空")
    @Schema(description = "Google 登入後 redirect 帶回的一次性 code", example = "a1b2c3...")
    private String code;
}
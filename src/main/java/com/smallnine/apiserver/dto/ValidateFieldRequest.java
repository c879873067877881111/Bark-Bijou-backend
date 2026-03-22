package com.smallnine.apiserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateFieldRequest {
    @NotBlank(message = "欄位名稱不能為空")
    private String field;

    @NotBlank(message = "欄位值不能為空")
    private String value;
}

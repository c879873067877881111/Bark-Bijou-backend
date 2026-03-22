package com.smallnine.apiserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    private Long productId;

    @NotNull(message = "評分不能為空")
    @Min(value = 1, message = "評分最低1")
    @Max(value = 5, message = "評分最高5")
    private Integer rating;

    @JsonAlias("comment")
    private String content;
}

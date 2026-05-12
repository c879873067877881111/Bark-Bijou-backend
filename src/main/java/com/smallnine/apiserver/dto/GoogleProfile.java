package com.smallnine.apiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleProfile {
    private String sub;
    private String email;
    private String name;
    private String picture;
}
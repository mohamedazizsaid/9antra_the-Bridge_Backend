package com._antra.the_bridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginRequest {
    private String provider;   // "GOOGLE" | "FACEBOOK"
    private String accessToken;
}

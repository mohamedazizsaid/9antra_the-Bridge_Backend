package com._antra.the_bridge.service;

import com._antra.the_bridge.exception.CustomException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to verify OAuth2 access tokens from Google and Facebook
 * by calling their userinfo endpoints. Returns user info map or throws.
 */
@Service
public class OAuthService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Returns a map with: email, firstName, lastName, avatar, providerId
     */
    public Map<String, String> verifyGoogleToken(String accessToken) {
        try {
            String endpoint = "https://www.googleapis.com/oauth2/v3/userinfo";
            JsonNode json = fetchJson(endpoint, accessToken);
            Map<String, String> info = new HashMap<>();
            info.put("email", getTextSafe(json, "email"));
            info.put("firstName", getTextSafe(json, "given_name"));
            info.put("lastName", getTextSafe(json, "family_name"));
            info.put("avatar", getTextSafe(json, "picture"));
            info.put("providerId", getTextSafe(json, "sub"));
            return info;
        } catch (IOException e) {
            throw new CustomException("Token Google invalide: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Returns a map with: email, firstName, lastName, avatar, providerId
     */
    public Map<String, String> verifyFacebookToken(String accessToken) {
        try {
            String endpoint = "https://graph.facebook.com/me?fields=id,first_name,last_name,email,picture.type(large)&access_token="
                    + accessToken;
            // Facebook uses query-param auth, no Bearer header
            JsonNode json = fetchJsonNoAuth(endpoint);
            Map<String, String> info = new HashMap<>();
            info.put("email", getTextSafe(json, "email"));
            info.put("firstName", getTextSafe(json, "first_name"));
            info.put("lastName", getTextSafe(json, "last_name"));
            info.put("providerId", getTextSafe(json, "id"));
            // picture.data.url
            if (json.has("picture") && json.get("picture").has("data")) {
                info.put("avatar", getTextSafe(json.get("picture").get("data"), "url"));
            } else {
                info.put("avatar", "");
            }
            return info;
        } catch (IOException e) {
            throw new CustomException("Token Facebook invalide: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private JsonNode fetchJson(String endpoint, String bearerToken) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        if (conn.getResponseCode() != 200) {
            throw new IOException("HTTP " + conn.getResponseCode() + " from " + endpoint);
        }
        return objectMapper.readTree(conn.getInputStream());
    }

    private JsonNode fetchJsonNoAuth(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        if (conn.getResponseCode() != 200) {
            throw new IOException("HTTP " + conn.getResponseCode() + " from " + endpoint);
        }
        return objectMapper.readTree(conn.getInputStream());
    }

    private String getTextSafe(JsonNode node, String field) {
        if (node == null || !node.has(field))
            return "";
        return node.get(field).asText("");
    }
}

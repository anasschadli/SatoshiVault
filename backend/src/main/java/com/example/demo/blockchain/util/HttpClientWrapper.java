package com.example.demo.blockchain.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class HttpClientWrapper {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Make GET request
     */
    public String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("HTTP Error: " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }

    /**
     * Make POST request
     */
    public String post(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new Exception("HTTP Error: " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }

    /**
     * Parse JSON response
     */
    public JsonObject parseJson(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}

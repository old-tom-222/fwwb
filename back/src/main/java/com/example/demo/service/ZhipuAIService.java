package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ZhipuAIService {
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions"; 
    private static final String API_KEY = "140ffc7983a847b6ba96ce451d79a2fc.jJ3Tshd0XsH4LJDL";
    private final RestTemplate restTemplate;

    public ZhipuAIService() {
        this.restTemplate = new RestTemplate();
    }

    public String chat(String userMessage) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.set("Content-Type", "application/json");

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "glm-4");
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", "使用中文回答\n" + userMessage)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            // 发送请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                API_URL, HttpMethod.POST, entity, Map.class
            );

            // 解析响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    System.out.println("API Response: " + responseBody);
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        if (message != null) {
                            return (String) message.get("content");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "抱歉，我现在无法回答你的问题，请稍后再试。";
    }
}
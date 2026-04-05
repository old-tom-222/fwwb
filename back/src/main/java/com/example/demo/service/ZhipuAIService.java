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

    public String chat(String userMessage, List<String> fileContents) {
        try {
            // 构建消息内容，包含文件内容
            StringBuilder messageContent = new StringBuilder();
            messageContent.append("使用中文回答\n");
            messageContent.append("直接开始正文，不需要任何开场白或引言，例如'这是个...，我将分析它'之类的内容。\n");
            messageContent.append(userMessage);
            
            // 添加文件内容
            if (fileContents != null && !fileContents.isEmpty()) {
                messageContent.append("\n\n以下是上传的文件内容：\n");
                for (int i = 0; i < fileContents.size(); i++) {
                    messageContent.append("文件").append(i + 1).append(":\n");
                    messageContent.append(fileContents.get(i)).append("\n\n");
                }
            }

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.set("Content-Type", "application/json");

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "glm-4");
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", messageContent.toString())
            ));
            requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);

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
    
    // 重载方法，保持向后兼容
    public String chat(String userMessage) {
        return chat(userMessage, null);
    }
}
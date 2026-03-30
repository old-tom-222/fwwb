package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = "message")
public class Message {
    @Id
    private String id;
    
    private String communicationId;
    
    private Integer status;
    
    private String content;
    
    @Field("create_at")
    private Date createdAt;

    public Message() {
    }

    public Message(String communicationId, Integer status, String content) {
        this.communicationId = communicationId;
        this.status = status;
        this.content = content;
        this.createdAt = new Date();
    }
}
package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = "massage")
public class Message {
    @Id
    private String id;

    private String userId;

    private Integer status;

    private String content;

    @Field("create_at")
    private Date createdAt;

    public Message() {
    }

    public Message(String userId, Integer status, String content) {
        this.userId = userId;
        this.status = status;
        this.content = content;
        this.createdAt = new Date();
    }
}

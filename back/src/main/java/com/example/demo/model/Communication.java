package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = "communication")
public class Communication {
    @Id
    private String id;
    
    private String userId;
    
    private String name;
    
    @Field("create_at")
    private Date createdAt;

    public Communication() {
    }

    public Communication(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.createdAt = new Date();
    }
}
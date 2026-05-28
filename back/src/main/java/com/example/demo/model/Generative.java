package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "generative")
public class Generative {
    @Id
    private String id;
    private String userId;
    private String name;
    private String url;
    private String type;
    private Integer fileType;
    private Date createdAt;
    private Date updatedAt;

    public Generative() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Generative(String userId, String name, String url, String type) {
        this.userId = userId;
        this.name = name;
        this.url = url;
        this.type = type;
        if (name != null && name.endsWith(".docx")) {
            this.fileType = 0;
        } else if (name != null && name.endsWith(".xlsx")) {
            this.fileType = 1;
        }
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}

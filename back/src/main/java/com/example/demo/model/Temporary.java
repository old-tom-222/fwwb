package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "temporary")
public class Temporary {
    @Id
    private String id;
    private String userId;
    private String name;
    private String url;

    public Temporary() {
    }

    public Temporary(String userId, String name, String url) {
        this.userId = userId;
        this.name = name;
        this.url = url;
    }
}

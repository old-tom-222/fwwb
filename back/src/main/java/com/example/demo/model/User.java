package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "user")
public class User {
    @Id
    private String id;
    private String name;
    private int age;
    private List<String> hobbies;
    private Address address;

    @Data
    public static class Address {
        private String city;
        private String street;
    }
}
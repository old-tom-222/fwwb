package com.example.demo.repository;

import com.example.demo.model.Generative;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GenerativeRepository extends MongoRepository<Generative, String> {
    List<Generative> findByUserId(String userId);
    Generative findByUserIdAndType(String userId, String type);
}
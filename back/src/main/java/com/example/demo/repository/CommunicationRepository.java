package com.example.demo.repository;

import com.example.demo.model.Communication;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommunicationRepository extends MongoRepository<Communication, String> {
    List<Communication> findByUserId(String userId);
}
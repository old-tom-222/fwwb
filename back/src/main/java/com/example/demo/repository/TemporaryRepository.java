package com.example.demo.repository;

import com.example.demo.model.Temporary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TemporaryRepository extends MongoRepository<Temporary, String> {
    // 根据用户ID查询临时文件列表
    List<Temporary> findByUserId(String userId);
}
package com.example.demo.repository;

import com.example.demo.model.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RepositoryRepository extends MongoRepository<Repository, String> {
    // 根据用户ID查询文件列表
    List<Repository> findByUserId(String userId);
}

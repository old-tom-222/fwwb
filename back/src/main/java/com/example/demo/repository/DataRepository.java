package com.example.demo.repository;

import com.example.demo.model.Data;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DataRepository extends MongoRepository<Data, String> {
    // 根据关键词查询数据
    List<Data> findByKeyword(String keyword);
    
    // 根据文章ID查询数据
    List<Data> findByArticleId(String articleId);
    
    // 根据文章ID删除数据
    void deleteByArticleId(String articleId);
}
package com.example.demo.repository;

import com.example.demo.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // 根据用户 ID 查询所有消息
    List<Message> findByUserId(String userId);

    // 根据用户 ID 和状态查询消息
    List<Message> findByUserIdAndStatus(String userId, Integer status);

    // 根据用户 ID 查询最新消息
    List<Message> findByUserIdOrderByCreatedAtDesc(String userId);

    // 根据状态查询消息
    List<Message> findByStatus(Integer status);

    // 根据时间范围查询
    List<Message> findByCreatedAtBetween(Date startDate, Date endDate);
}

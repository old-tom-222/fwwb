package com.example.demo.controller;

import com.example.demo.model.Communication;
import com.example.demo.repository.CommunicationRepository;
import com.example.demo.repository.MessageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/communications")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "对话管理", description = "对话 CRUD 操作")
public class CommunicationController {
    @Autowired
    private CommunicationRepository communicationRepository;
    
    @Autowired
    private MessageRepository messageRepository;

    @GetMapping
    @Operation(summary = "获取所有对话", description = "返回所有对话列表")
    public List<Communication> getAllCommunications() {
        return communicationRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取对话", description = "根据对话ID返回对话信息")
    public Communication getCommunicationById(@PathVariable String id) {
        return communicationRepository.findById(id).orElse(null);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取对话", description = "根据用户ID返回该用户的所有对话")
    public List<Communication> getCommunicationsByUserId(@PathVariable String userId) {
        return communicationRepository.findByUserId(userId);
    }

    @PostMapping
    @Operation(summary = "创建对话", description = "创建新对话，返回创建后的对话对象")
    public Communication createCommunication(@RequestBody Communication communication) {
        return communicationRepository.save(communication);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新对话", description = "根据对话ID更新对话信息，返回更新后的对话对象")
    public Communication updateCommunication(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // 先从数据库中获取现有的对话对象
        Communication communication = communicationRepository.findById(id).orElse(null);
        if (communication == null) {
            return null;
        }
        
        // 只更新提供的字段
        if (updates.containsKey("name")) {
            communication.setName((String) updates.get("name"));
        }
        if (updates.containsKey("userId")) {
            communication.setUserId((String) updates.get("userId"));
        }
        if (updates.containsKey("createdAt")) {
            communication.setCreatedAt(new Date((long) updates.get("createdAt")));
        }
        
        return communicationRepository.save(communication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除对话", description = "根据对话ID删除对话及相关消息")
    public void deleteCommunication(@PathVariable String id) {
        // 先删除与该对话相关的所有消息
        messageRepository.deleteByCommunicationId(id);
        // 再删除对话
        communicationRepository.deleteById(id);
    }
}
package com.example.demo.controller;

import com.example.demo.model.Communication;
import com.example.demo.repository.CommunicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communications")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "对话管理", description = "对话 CRUD 操作")
public class CommunicationController {
    @Autowired
    private CommunicationRepository communicationRepository;

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
    public Communication updateCommunication(@PathVariable String id, @RequestBody Communication communication) {
        communication.setId(id);
        return communicationRepository.save(communication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除对话", description = "根据对话ID删除对话")
    public void deleteCommunication(@PathVariable String id) {
        communicationRepository.deleteById(id);
    }
}
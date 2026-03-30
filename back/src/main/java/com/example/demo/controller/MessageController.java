package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;
import com.example.demo.service.ZhipuAIService;
import com.example.demo.util.FileParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "消息管理", description = "消息 CRUD 操作")
public class MessageController {
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ZhipuAIService zhipuAIService;

    @GetMapping
    @Operation(summary = "获取所有消息", description = "返回所有消息列表")
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取消息", description = "根据消息ID返回消息信息")
    public Message getMessageById(@PathVariable String id) {
        return messageRepository.findById(id).orElse(null);
    }

    @GetMapping("/communication/{communicationId}")
    @Operation(summary = "根据对话ID获取消息", description = "根据对话ID返回消息列表")
    public List<Message> getMessagesByCommunicationId(@PathVariable String communicationId) {
        return messageRepository.findByCommunicationId(communicationId);
    }

    @PostMapping
    @Operation(summary = "创建消息", description = "创建新消息，返回创建后的消息对象")
    public Message createMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }

    @PostMapping("/chat")
    @Operation(summary = "聊天接口", description = "处理用户聊天请求，返回AI回复")
    public Message chat(@RequestParam("communicationId") String communicationId,
                       @RequestParam("content") String content,
                       @RequestParam(value = "files", required = false) MultipartFile[] files) {
        // 处理上传的文件
        List<String> fileContents = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                // 这里可以处理文件，例如保存到服务器或解析文件内容
                System.out.println("上传的文件: " + file.getOriginalFilename());
                try {
                    // 解析文件内容
                    String fileContent = FileParser.parseFile(file.getOriginalFilename(), file.getInputStream());
                    fileContents.add(fileContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 可以将文件内容添加到content中，或者在调用AI API时一并发送
            }
        }
        
        // 保存用户消息（status=1）
        Message userMessage = new Message();
        userMessage.setCommunicationId(communicationId);
        userMessage.setContent(content);
        userMessage.setStatus(1);
        userMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(userMessage);
        
        // 调用智谱API获取AI回复，传递文件内容
        String aiResponse = zhipuAIService.chat(content, fileContents);
        
        // 保存AI回复（status=0）
        Message aiMessage = new Message();
        aiMessage.setCommunicationId(communicationId);
        aiMessage.setContent(aiResponse);
        aiMessage.setStatus(0);
        aiMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(aiMessage);
        
        return aiMessage;
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新消息", description = "根据消息ID更新消息信息，返回更新后的消息对象")
    public Message updateMessage(@PathVariable String id, @RequestBody Message message) {
        message.setId(id);
        return messageRepository.save(message);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "根据消息ID删除消息")
    public void deleteMessage(@PathVariable String id) {
        messageRepository.deleteById(id);
    }
}
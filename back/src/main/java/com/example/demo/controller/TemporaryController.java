package com.example.demo.controller;

import com.example.demo.model.Data;
import com.example.demo.model.Temporary;
import com.example.demo.repository.DataRepository;
import com.example.demo.repository.TemporaryRepository;
import com.example.demo.service.ZhipuAIService;
import com.example.demo.util.FileParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/temporary")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "Temporary", description = "临时文件管理API")
public class TemporaryController {

    @Autowired
    private TemporaryRepository temporaryRepository;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private ZhipuAIService zhipuAIService;

    @Operation(summary = "获取所有临时文件", description = "获取所有临时文件记录")
    @GetMapping
    public List<Temporary> getAllTemporary() {
        return temporaryRepository.findAll();
    }

    @Operation(summary = "根据ID获取临时文件", description = "根据唯一ID获取临时文件内容")
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getTemporaryById(@PathVariable String id) throws IOException {
        Temporary temporary = temporaryRepository.findById(id).orElse(null);
        if (temporary == null || temporary.getUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        
        File file = new File(temporary.getUrl());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Path path = Paths.get(temporary.getUrl());
        Resource resource = new UrlResource(path.toUri());
        
        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + temporary.getName())
                .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "根据用户ID获取临时文件", description = "根据用户ID查询临时文件记录")
    @GetMapping("/user/{userId}")
    public List<Temporary> getTemporaryByUserId(@PathVariable String userId) {
        return temporaryRepository.findByUserId(userId);
    }

    @Operation(summary = "创建临时文件", description = "创建新的临时文件记录")
    @PostMapping
    public Temporary createTemporary(@RequestBody Temporary temporary) {
        return temporaryRepository.save(temporary);
    }

    @Operation(summary = "更新临时文件", description = "更新现有临时文件记录")
    @PutMapping("/{id}")
    public Temporary updateTemporary(@PathVariable String id, @RequestBody Temporary temporary) {
        Temporary existingTemporary = temporaryRepository.findById(id).orElse(null);
        if (existingTemporary != null) {
            existingTemporary.setUserId(temporary.getUserId());
            existingTemporary.setName(temporary.getName());
            existingTemporary.setUrl(temporary.getUrl());
            return temporaryRepository.save(existingTemporary);
        }
        return null;
    }

    @Operation(summary = "删除临时文件", description = "删除临时文件记录")
    @DeleteMapping("/{id}")
    public void deleteTemporary(@PathVariable String id) {
        // 删除文件
        Temporary temporary = temporaryRepository.findById(id).orElse(null);
        if (temporary != null && temporary.getUrl() != null) {
            File file = new File(temporary.getUrl());
            if (file.exists()) {
                try {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        System.err.println("Failed to delete file: " + temporary.getUrl());
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting file: " + e.getMessage());
                }
            }
            // 删除相关的data记录
            dataRepository.deleteByArticleId(temporary.getId());
        }
        temporaryRepository.deleteById(id);
    }

    @Operation(summary = "上传临时文件", description = "上传文件并进行AI分析")
    @PostMapping("/upload")
    public Temporary uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId) throws IOException {
        // 创建上传目录
        String uploadDir = System.getProperty("user.dir") + "/uploads/temporary/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        // 解析文件内容
        String fileContent = null;
        try (InputStream inputStream = file.getInputStream()) {
            fileContent = FileParser.parseFile(file.getOriginalFilename(), inputStream);
        }

        // 保存文件
        file.transferTo(new File(filePath));

        // 创建临时文件记录
        Temporary temporary = new Temporary();
        temporary.setUserId(userId);
        temporary.setName(file.getOriginalFilename());
        temporary.setUrl(filePath);
        temporary = temporaryRepository.save(temporary);

        // 调用智谱AI分析文件内容，提取关键词和对应的句子
        if (fileContent != null && !fileContent.isEmpty()) {
            List<String> fileContents = new ArrayList<>();
            fileContents.add(fileContent);
            
            // 构建AI分析请求
            String prompt = "请分析以下文件内容，提取出5-10个关键词，并为每个关键词提供一个包含该关键词的句子。\n" +
                          "格式要求：\n" +
                          "关键词1：包含关键词1的句子\n" +
                          "关键词2：包含关键词2的句子\n" +
                          "...\n" +
                          fileContent;
            
            String aiResponse = zhipuAIService.chat(prompt, fileContents);
            
            // 解析AI响应，提取关键词和句子
            parseAIResponse(aiResponse, temporary.getId());
        }

        return temporary;
    }
    
    /**
     * 解析AI响应，提取关键词和句子并保存到data集合
     */
    private void parseAIResponse(String aiResponse, String articleId) {
        if (aiResponse == null || aiResponse.isEmpty()) {
            return;
        }
        
        // 按行解析AI响应
        String[] lines = aiResponse.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            // 匹配"关键词：句子"格式
            int colonIndex = line.indexOf("：");
            if (colonIndex != -1) {
                String keyword = line.substring(0, colonIndex).trim();
                String contextText = line.substring(colonIndex + 1).trim();
                
                // 移除关键词前的序号（如果有）
                keyword = keyword.replaceAll("^\\d+\\.", "").trim();
                
                if (!keyword.isEmpty() && !contextText.isEmpty()) {
                    Data data = new Data();
                    data.setKeyword(keyword);
                    data.setContextText(contextText);
                    data.setArticleId(articleId);
                    dataRepository.save(data);
                }
            }
        }
    }
}
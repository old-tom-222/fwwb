package com.example.demo.controller;

import com.example.demo.model.Repository;
import com.example.demo.repository.RepositoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repository")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "Repository", description = "文件仓库管理API")
public class RepositoryController {

    @Autowired
    private RepositoryRepository repositoryRepository;

    // 文件存储目录
    private static final String UPLOAD_DIR = "uploads";

    // 初始化上传目录
    public RepositoryController() {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Operation(summary = "获取用户文件列表", description = "根据用户ID获取文件仓库中的文件列表")
    @GetMapping("/user/{userId}")
    public List<Repository> getUserFiles(@PathVariable String userId) {
        return repositoryRepository.findByUserId(userId);
    }

    @Operation(summary = "上传文件", description = "上传文件到用户的文件仓库")
    @PostMapping("/upload")
    public Repository uploadFile(@RequestParam("userId") String userId, @RequestParam("file") MultipartFile file) {
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 保存文件到本地
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFilename);
            Files.write(filePath, file.getBytes());

            // 构建文件相对路径
            String relativeUrl = "/" + UPLOAD_DIR + "/" + uniqueFilename;

            // 创建仓库记录
            Repository repository = new Repository(userId, originalFilename, relativeUrl);
            return repositoryRepository.save(repository);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Operation(summary = "删除文件", description = "从用户的文件仓库中删除文件")
    @DeleteMapping("/{id}")
    public void deleteFile(@PathVariable String id) {
        try {
            // 获取文件记录
            Repository repository = repositoryRepository.findById(id).orElse(null);
            if (repository != null) {
                // 删除本地文件
                String url = repository.getUrl();
                if (url != null && !url.isEmpty()) {
                    // 移除开头的斜杠
                    String filePath = url.startsWith("/") ? url.substring(1) : url;
                    Path path = Paths.get(filePath);
                    if (Files.exists(path)) {
                        Files.delete(path);
                    }
                }
                // 删除数据库记录
                repositoryRepository.deleteById(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

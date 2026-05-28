package com.example.demo.controller;

import com.example.demo.model.Generative;
import com.example.demo.repository.GenerativeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/generative")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "Generative", description = "AI生成文件管理API")
public class GenerativeController {

    @Autowired
    private GenerativeRepository generativeRepository;

    private static final String UPLOAD_DIR = "uploads/generative";

    public GenerativeController() {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Operation(summary = "获取用户生成文件列表", description = "根据用户ID获取生成文件列表")
    @GetMapping("/user/{userId}")
    public List<Generative> getUserFiles(@PathVariable String userId) {
        return generativeRepository.findByUserId(userId);
    }

    @Operation(summary = "上传生成文件", description = "上传AI生成的文件到generative集合")
    @PostMapping("/upload")
    public Generative uploadFile(@RequestParam("userId") String userId,
                                @RequestParam("type") String type,
                                @RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return null;
            }
            String extension = validateFileExtension(originalFilename);
            if (extension == null) return null;

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFilename);
            Files.write(filePath, file.getBytes());
            String relativeUrl = "/" + UPLOAD_DIR + "/" + uniqueFilename;

            Generative generative = new Generative(userId, originalFilename, relativeUrl, type);
            return generativeRepository.save(generative);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Operation(summary = "更新生成文件", description = "更新AI生成的文件，保留旧文件")
    @PutMapping("/update")
    public Generative updateFile(@RequestParam("userId") String userId,
                                @RequestParam("type") String type,
                                @RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return null;
            }
            String extension = validateFileExtension(originalFilename);
            if (extension == null) return null;

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFilename);
            Files.write(filePath, file.getBytes());
            String relativeUrl = "/" + UPLOAD_DIR + "/" + uniqueFilename;

            Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, type);
            if (existingGenerative != null) {
                existingGenerative.setName(originalFilename);
                existingGenerative.setUrl(relativeUrl);
                existingGenerative.setFileType(extension.equals(".docx") ? 0 : 1);
                existingGenerative.setUpdatedAt(new Date());
                return generativeRepository.save(existingGenerative);
            } else {
                Generative generative = new Generative(userId, originalFilename, relativeUrl, type);
                return generativeRepository.save(generative);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String validateFileExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.'));
        if (!extension.equals(".docx") && !extension.equals(".xlsx")) {
            return null;
        }
        return extension;
    }

    @Operation(summary = "删除生成文件", description = "删除AI生成的文件")
    @DeleteMapping("/{id}")
    public void deleteFile(@PathVariable String id) {
        try {
            Generative generative = generativeRepository.findById(id).orElse(null);
            if (generative != null) {
                String url = generative.getUrl();
                if (url != null && !url.isEmpty()) {
                    String filePath = url.startsWith("/") ? url.substring(1) : url;
                    Path path = Paths.get(filePath);
                    if (Files.exists(path)) {
                        Files.delete(path);
                    }
                }
                generativeRepository.deleteById(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Operation(summary = "获取生成文件", description = "根据文件ID获取生成文件内容")
    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable String id) throws IOException {
        Generative generative = generativeRepository.findById(id).orElse(null);
        if (generative == null || generative.getUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        String filePath = generative.getUrl().startsWith("/") ? generative.getUrl().substring(1) : generative.getUrl();
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + generative.getName())
                .body(resource);
        } else {
            System.out.println("文件不存在或不可读: " + filePath);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "获取用户特定类型的生成文件", description = "根据用户ID和类型获取生成文件")
    @GetMapping("/user/{userId}/type/{type}")
    public Generative getFileByType(@PathVariable String userId, @PathVariable String type) {
        return generativeRepository.findByUserIdAndType(userId, type);
    }
}

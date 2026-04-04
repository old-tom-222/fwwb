package com.example.demo.controller;

import com.example.demo.model.Data;
import com.example.demo.repository.DataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "Data", description = "数据管理API")
public class DataController {

    @Autowired
    private DataRepository dataRepository;

    @Operation(summary = "获取所有数据", description = "获取所有数据记录")
    @GetMapping
    public List<Data> getAllData() {
        return dataRepository.findAll();
    }

    @Operation(summary = "根据ID获取数据", description = "根据唯一ID获取数据记录")
    @GetMapping("/{id}")
    public Data getDataById(@PathVariable String id) {
        return dataRepository.findById(id).orElse(null);
    }

    @Operation(summary = "根据关键词获取数据", description = "根据关键词查询数据记录")
    @GetMapping("/keyword/{keyword}")
    public List<Data> getDataByKeyword(@PathVariable String keyword) {
        return dataRepository.findByKeyword(keyword);
    }

    @Operation(summary = "根据文章ID获取数据", description = "根据文章ID查询数据记录")
    @GetMapping("/article/{articleId}")
    public List<Data> getDataByArticleId(@PathVariable String articleId) {
        return dataRepository.findByArticleId(articleId);
    }

    @Operation(summary = "创建数据", description = "创建新的数据记录")
    @PostMapping
    public Data createData(@RequestBody Data data) {
        return dataRepository.save(data);
    }

    @Operation(summary = "更新数据", description = "更新现有数据记录")
    @PutMapping("/{id}")
    public Data updateData(@PathVariable String id, @RequestBody Data data) {
        Data existingData = dataRepository.findById(id).orElse(null);
        if (existingData != null) {
            existingData.setKeyword(data.getKeyword());
            existingData.setContextText(data.getContextText());
            existingData.setArticleId(data.getArticleId());
            return dataRepository.save(existingData);
        }
        return null;
    }

    @Operation(summary = "删除数据", description = "删除数据记录")
    @DeleteMapping("/{id}")
    public void deleteData(@PathVariable String id) {
        dataRepository.deleteById(id);
    }
}
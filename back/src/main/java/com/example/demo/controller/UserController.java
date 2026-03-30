package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "用户管理", description = "用户 CRUD 操作")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @Operation(summary = "获取所有用户", description = "返回数据库中所有用户的列表")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID返回单个用户信息，如果不存在返回null")
    public User getUserById(@PathVariable String id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    @Operation(summary = "创建新用户", description = "接收用户数据并保存到数据库，返回保存后的用户对象")
    public User createUser(@RequestBody User user) {
        // create_at 在后端自动赋值为当前时间，避免前端传入不一致
        user.setCreate_at(java.time.LocalDateTime.now().toString());
        return userRepository.save(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息，返回更新后的用户对象")
    public User updateUser(@PathVariable String id, @RequestBody User user) {
        user.setId(id);
        return userRepository.save(user);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据账号和密码验证用户，返回用户信息")
    public User login(@RequestBody Map<String, String> credentials) {
        String account = credentials.get("account");
        String password = credentials.get("password");
        return userRepository.findByAccountAndPassword(account, password).orElse(null);
    }
}
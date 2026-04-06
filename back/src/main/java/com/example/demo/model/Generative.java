package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "generative")
public class Generative {
    @Id
    private String id;
    private String userId;
    private String name;
    private String url;
    private String type; // file or table
    private Integer fileType; // 0 for docx, 1 for xlsx
    private Date createdAt;
    private Date updatedAt;

    public Generative() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Generative(String userId, String name, String url, String type) {
        this.userId = userId;
        this.name = name;
        this.url = url;
        this.type = type;
        // 根据文件扩展名设置fileType
        if (name != null && name.endsWith(".docx")) {
            this.fileType = 0;
        } else if (name != null && name.endsWith(".xlsx")) {
            this.fileType = 1;
        }
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
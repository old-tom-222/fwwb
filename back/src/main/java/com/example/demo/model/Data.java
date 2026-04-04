package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "data")
public class Data {
    @Id
    private String id;
    private String keyword;
    private String contextText;
    private String articleId;

    public Data() {
    }

    public Data(String keyword, String contextText, String articleId) {
        this.keyword = keyword;
        this.contextText = contextText;
        this.articleId = articleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getContextText() {
        return contextText;
    }

    public void setContextText(String contextText) {
        this.contextText = contextText;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
}
package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "data")
public class Data {
    @Id
    private String id;
    private String keyword;
    private String contextText;
    private String articleId;

    public Data(String keyword, String contextText, String articleId) {
        this.keyword = keyword;
        this.contextText = contextText;
        this.articleId = articleId;
    }
}

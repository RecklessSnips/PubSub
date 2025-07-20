package com.example.utils.news;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

// Remember to keep no arg constructor and getters & setters for Jackson to convert
// Implementing the Serializable to Solace's ObjectToBytes to convert
public class NewsApi extends News {

    private String author;
    private Source source;
    private String content;
    @JsonProperty("publishedAt")
    private LocalDateTime localDateTime;

    public NewsApi(){}

    public Source getSource(){
        return this.source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return this.author;
    }

    @Override
    @JsonProperty("urlToImage")
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "NewsApi{" +
                "author='" + author + '\'' +
                ", source id=" + source.getId() +
                ", source name=" + source.getName() +
                ", content='" + content + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", description='" + description + '\'' +
                ", publishDate=" +  +
                '}';
    }

    // Remember to keep no arg constructor and getters & setters for Jackson to convert
    public static class Source {
        private String id;
        private String name;

        public Source(){}

        public Source(String id, String name){
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}


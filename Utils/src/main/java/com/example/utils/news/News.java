package com.example.utils.news;

import java.io.Serializable;

// 抽象类，代表一篇新闻，只规定一篇新闻必须有的属性，它的不同子类会根据不同API来实现并添加更多的细节
public class News implements Serializable {


    // Super class for a News object
    protected String title;
    protected String url;
    protected String imageURL;
    protected String description;

    // Getters and setters for Jackson, will be override if the subclass has a different JSON field name
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

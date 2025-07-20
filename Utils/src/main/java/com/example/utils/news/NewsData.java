package com.example.utils.news;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NewsData extends News{

    @JsonProperty("creator")
    private List<String> authors;
    @JsonProperty("pubDate")
    private String publishDate;
    @JsonProperty("pubDateTZ")
    private String zoneId;

    // The local time zone
    @JsonProperty("localDateTime")
    private LocalDateTime localDateTime;

    @JsonProperty("source_id")
    private String sourceId;
    @JsonProperty("source_name")
    private String sourceName;
    @JsonProperty("source_url")
    private String sourceURL;
    @JsonProperty("source_icon")
    private String sourceIcon;

    public NewsData() {}

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public String getSourceIcon() {
        return sourceIcon;
    }

    public void setSourceIcon(String sourceIcon) {
        this.sourceIcon = sourceIcon;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalDateTime getLocalDateTime(){
        return convertToLocalDateTime();
    }

    public LocalDateTime convertToLocalDateTime() {
        if (localDateTime == null){
            try {
                // Input format: "2025-07-02 01:15:08"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.parse(publishDate, formatter);
                // Set default is UTC
                ZoneId zone = ZoneId.of(zoneId != null ? zoneId : "UTC");
                this.localDateTime = time.atZone(zone).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                return this.localDateTime;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse publishDate with zoneId", e);
            }
        }else{
            return this.localDateTime;
        }
    }

    @Override
    @JsonProperty("link")
    public void setUrl(String url){
        this.url = url;
    }

    @Override
    @JsonProperty("image_url")  // JSON 中叫 image_url，映射到 imageURL
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }


    @Override
    public String toString() {
        return "NewsData{" +
                "authors=" + authors +
                ", sourceId='" + sourceId + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", sourceURL='" + sourceURL + '\'' +
                ", sourceIcon='" + sourceIcon + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", description='" + description + '\'' +
                ", publishDate=" + publishDate +
                '}';
    }
}

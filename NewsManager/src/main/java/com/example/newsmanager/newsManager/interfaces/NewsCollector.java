package com.example.newsmanager.newsManager.interfaces;

import java.util.List;

// interface that what functionalities a Collector should have
public interface NewsCollector {

    void search(String query);
    void searchByDomains(List<String> source, String... excludeDomains);
    void searchByCategory(String category);
}

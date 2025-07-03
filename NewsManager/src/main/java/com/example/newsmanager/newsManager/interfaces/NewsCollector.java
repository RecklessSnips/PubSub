package com.example.newsmanager.newsManager.interfaces;

import java.util.List;

// interface that what functionalities a Collector should have
public interface NewsCollector {

    void search(String query);
    void searchByDomain(String source, String... excludeDomains);
    void searchByDomains(List<String> domains, String... excludeDomains);
    void searchByCategory(String category);
    // Within a range of options
    void searchByLanguage(String... lang);
}

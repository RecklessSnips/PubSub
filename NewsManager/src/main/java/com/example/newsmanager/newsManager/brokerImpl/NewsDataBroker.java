package com.example.newsmanager.newsManager.brokerImpl;

import com.example.newsmanager.newsManager.ConnectionManager;
import com.example.newsmanager.newsManager.NewsBroker;
import com.example.newsmanager.openfeign.NewsDataCollector;
import com.example.utils.Connector;
import com.example.utils.news.News;
import com.example.utils.news.NewsData;
import com.example.utils.response.NewsDataResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class NewsDataBroker extends NewsBroker {

    private final NewsDataCollector newsDataCollector;
    private static final String LANGUAGE = "en";
    private static final String TYPE = "NewsData";

    // Decide later if to strict the country within US

    public NewsDataBroker(
            @Autowired NewsDataCollector newsDataCollector,
            @Autowired ConnectionManager connectionManager,
            @Value("${newsdata.io.API_KEY}") String API_KEY){
        super(connectionManager.getConnector(), API_KEY);
        this.newsDataCollector = newsDataCollector;
    }

    // Publish news to the Event Broker
    @Override
    public void publish(){
        // Randomly pick a new from the list
        int i = new Random().nextInt(newsList.size());
        News newsApi = newsList.get(i);
        connector.publishDirect(newsApi, TYPE);
    }

    @Override
    public void search(String query) {
        // Add a pir of "" around the query
        String formattedQuery = "\"" +  String.join(" ", query.trim().split(" ")) + "\"";
        
        NewsDataResponse latestNews = newsDataCollector.getLatest(
                Map.of("domainurl", "nytimes.com",
                        "language", LANGUAGE,
                        "q", formattedQuery),
                API_KEY
        );
        List<NewsData> articles = latestNews.getResults();
        System.out.println("NewsData  Articles: ");
        System.out.println(latestNews);
        System.out.println(articles);
        newsList.addAll(articles);
    }

    @Override
    public void searchByDomains(List<String> domains, String... excludeDomains) {
        // Must enter a real domain with the TOp-level domain (.com, .ca...)
        if (domains.size() > 5) {
            throw new IllegalArgumentException("Must enter less than 5 domains");
        }

        String domain = String.join(",", domains);
        // Ignore the excludeDomains in NewsData.io

        NewsDataResponse news = newsDataCollector.getLatest(
                Map.of("domainurl", domain,
                        "language", LANGUAGE),
                API_KEY
        );

        System.out.println("NewsData Response: ");
        newsList.addAll(news.getResults());
    }

    @Override
    public void searchByCategory(String category) {
        NewsDataResponse latest = newsDataCollector.getLatest(
                Map.of("category", category,
                        "language", LANGUAGE),
                API_KEY
        );

        System.out.println("NewsData Category: ");
        newsList.addAll(latest.getResults());
    }

    public void searchByCategoryEnum(Category category) {
        searchByCategory(category.toString());
    }

    //******************* Running method *******************//
    @PostConstruct
    public void searchNews(){
        searchByDomains(List.of("reuters.com"), API_KEY);
    }

    @Scheduled(fixedRateString = "1m")
    public void extractAndPublish() {
        publish();
    }

    public enum Category {
        TOP("top"),
        BUSINESS("business"),
        HEALTH("health"),
        SCIENCE("science"),
        SPORTS("sports"),
        TECHNOLOGY("technology"),
        ENTERTAINMENT("entertainment"),
        CRIME("crime"),
        DOMESTIC("domestic"),
        EDUCATION("education"),
        ENVIRONMENT("environment"),
        FOOD("food"),
        LIFESTYLE("lifestyle"),
        POLITICS("politics"),
        TOURISM("tourism"),
        WORLD("world"),
        OTHER("other");

        private String category;

        Category(String category) {
            this.category = category;
        }

        @Override
        public String toString() {
            return category;
        }
    }
}

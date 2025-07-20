package com.example.newsmanager.newsManager.brokerImpl;

import com.example.newsmanager.newsManager.ConnectionManager;
import com.example.newsmanager.newsManager.NewsBroker;
import com.example.newsmanager.openfeign.NewsDataCollector;
import com.example.utils.exception.NewsException;
import com.example.utils.news.News;
import com.example.utils.news.NewsData;
import com.example.utils.response.NewsDataResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 这个类来发布 Reuters, NY Times, Economist 的新闻
 * 所有方法，均参考 <a href="https://newsdata.io/documentation#latest-news">Link</a> 来编写
 */
@Component
public class NewsDataBroker extends NewsBroker {

    private final NewsDataCollector newsDataCollector;
    private List<News> nytimes;
    private List<News> reuters;
    private List<News> economist;
    private static final String LANGUAGE = "en";
    private static final String TYPE = "NewsData";

    // Decide later if to strict the country within US

    public NewsDataBroker(
            @Autowired NewsDataCollector newsDataCollector,
            @Autowired ConnectionManager connectionManager,
            @Value("${newsdata.io.API_KEY}") String API_KEY){
        super(connectionManager.getConnector(), API_KEY);
        this.newsDataCollector = newsDataCollector;
        this.nytimes = new ArrayList<>();
        this.reuters = new ArrayList<>();
        this.economist = new ArrayList<>();
    }

    // Publish news to Event Broker
    @Override
    public void publish(){
        Collections.shuffle(reuters);
        Collections.shuffle(nytimes);
        Collections.shuffle(economist);

        int count = ThreadLocalRandom.current().nextInt(1, 2);
        if (!reuters.isEmpty()) {
            connector.publishDirect(reuters.subList(0, count), TYPE, "news/reuters");
        }
        if (!nytimes.isEmpty()) {
            connector.publishDirect(nytimes.subList(0, count), TYPE, "news/nytimes");
        }
        if (!economist.isEmpty()) {
            connector.publishDirect(economist.subList(0, count), TYPE, "news/economist");
        }
    }

    @Override
    public void search(String query) {
        // Add a pair of "" around the query
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
        // Must enter a real domain with the top-level domain (.com, .ca, etc.)
        if (domains == null || domains.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one domain.");
        }

        if (domains.size() > 5) {
            throw new IllegalArgumentException("Must enter less than or equal to 5 domains.");
        }

        // Currently, excludeDomains is ignored (not used in NewsData.io)
        for (String d : domains) {
            try {
                NewsDataResponse news = newsDataCollector.getLatest(
                    Map.of(
                        "domainurl", d,
                        "language", LANGUAGE
                    ),
                    API_KEY
                );
                System.out.println("News Data Search response: ");
                System.out.println(news.getResults().size());
                // Route result to correct list
                if (d.equalsIgnoreCase("reuters.com")) {
                    reuters.addAll(news.getResults());
                } else if (d.equalsIgnoreCase("nytimes.com")) {
                    nytimes.addAll(news.getResults());
                } else if (d.equalsIgnoreCase("economist.com")) {
                    economist.addAll(news.getResults());
                } else {
                    throw new NewsException("Unsupported News source: " + d);
                }

            } catch (Exception e) {
                throw new RuntimeException("News retrieval failed for domain: " + d, e);
            }
        }
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
        searchByDomains(List.of("reuters.com", "nytimes.com", "economist.com"), API_KEY);
    }

    @Scheduled(fixedRateString = "10s")
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

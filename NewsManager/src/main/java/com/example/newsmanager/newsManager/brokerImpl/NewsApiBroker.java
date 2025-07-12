package com.example.newsmanager.newsManager.brokerImpl;

import com.example.newsmanager.newsManager.ConnectionManager;
import com.example.newsmanager.newsManager.NewsBroker;
import com.example.newsmanager.openfeign.NewsAPICollector;
import com.example.utils.exception.NewsException;
import com.example.utils.news.News;
import com.example.utils.response.NewsApiResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NewsApiBroker extends NewsBroker {

    private final NewsAPICollector newsAPICollector;
    private List<News> bbc;
    private List<News> cnn;

    // Default News language
    private static final String LANGUAGE = "en";
    // Used to distinguish the news when receiver processing it: receiveDirect()
    private static final String TYPE = "NewsAPI";

    // Decide later if to strict the country within US
    private SortBy sortBy;

    public NewsApiBroker(
            @Autowired NewsAPICollector newsAPICollector,
            @Autowired ConnectionManager connectionManager,
            @Value("${newsapi.org.API_KEY}") String API_KEY){
        super(connectionManager.getConnector(), API_KEY);
        this.newsAPICollector = newsAPICollector;
        this.bbc = new ArrayList<>();
        this.cnn = new ArrayList<>();
        this.sortBy = SortBy.PUBLISHAT;
    }

    // Publish news to the Event Broker
    @Override
    public void publish(){

    }


    // Publish news to event broker
    public void publishNews(){
        // Publish news from different sources to the Broker!
        // Dynamic Topic!
        connector.publishDirect(bbc.subList(0, 1), TYPE, "news/bbc");
        connector.publishDirect(cnn.subList(0, 1), TYPE, "news/cnn");
    }

    // ***************** Each of the method need to specify the query params in each method *************************
    @Override
    public void search(String query){
        // Split the query into a list, then concatenate them using + sign
        String[] split = query.trim().split(" ");
        String formattedQuery = String.join("+", split);

        NewsApiResponse response = newsAPICollector.getEverything(
            Map.of("q", formattedQuery,
                    "language", LANGUAGE,
                    "sortBy", SortBy.POPULARITY.toString()),
            API_KEY
        );

        System.out.println("Search by q: ");
        System.out.println(response.getArticles());
        newsList.addAll(getNewsList());
    }

    @Override
    public void searchByDomains(List<String> source, String... excludeDomains) {
        // Search by domains (reuters.com, nytimes.com won't work)
        String excludedDomains = String.join(",", excludeDomains);

        for (String s : source) {
            NewsApiResponse response = newsAPICollector.getEverything(
                    Map.of("domains", s,
                            "excludeDomains", excludedDomains,
                            "language", LANGUAGE,
                            "sortBy", SortBy.POPULARITY.toString()),
                    API_KEY
            );

            System.out.println("News API Search response: ");
            System.out.println(response.getArticles().size());

            if (s.equals("bbc.com")) {
                bbc.addAll(response.getArticles());
            } else if (s.equals("cnn.com")) {
                cnn.addAll(response.getArticles());
            } else {
                throw new NewsException("Unsupported News source");
            }
        }
    }

    // Only available in top-headlines endpoint, and in US
    @Override
    public void searchByCategory(String category) {
        NewsApiResponse topHeadlines = newsAPICollector.getTopHeadlines(
            Map.of("category", category,
                    "language", LANGUAGE,
                    "sortBy", SortBy.POPULARITY.toString()),
            API_KEY
        );

        System.out.println("Response: ");
        System.out.println(topHeadlines.getArticles());
        newsList.addAll(topHeadlines.getArticles());
    }

    // Use this method to select a predefined category
    public void searchByCategoryEnum(Category category) {
        searchByCategory(category.toString());
    }

    // Set the sorting option
    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }

    //******************* Running method *******************//
    @PostConstruct
    public void searchNews(){
        // Fetch news periodically
        searchByDomains(List.of("cnn.com", "bbc.com"), API_KEY);
    }


    @Scheduled(fixedRateString = "2m")
    public void extractAndPublish(){
        publishNews();
    }

    // Sorting options
    public enum SortBy {
        RELEVANCY("relevancy"),
        POPULARITY("popularity"),
        PUBLISHAT("publishedAt");

        private String sortBy;

        SortBy(String sortBy) {
            this.sortBy = sortBy;
        }

        @Override
        public String toString() {
            return sortBy;
        }
    }

    // Category
    public enum Category {
        BUSINESS("business"),
        ENTERTAINMENT("entertainment"),
        GENERAL("general"),
        HEALTH("health"),
        SCIENCE("science"),
        SPORTS("sports"),
        TECHNOLOGY("technology");

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

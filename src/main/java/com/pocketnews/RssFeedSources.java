package com.pocketnews;

import java.util.List;

public class RssFeedSources {

    public record RssFeed(String url, String defaultCategory) {}

    public static final List<RssFeed> FEEDS = List.of(
            // General / Top Stories
            new RssFeed("https://feeds.feedburner.com/ndtvnews-top-stories", "top-stories"),
            new RssFeed("https://timesofindia.indiatimes.com/rssfeedstopstories.cms", "top-stories"),

            // India
            new RssFeed("https://feeds.feedburner.com/ndtvnews-india-news", "india"),
            new RssFeed("https://www.thehindu.com/news/national/feeder/default.rss", "india"),

            // World
            new RssFeed("https://feeds.feedburner.com/ndtvnews-world-news", "world"),
            new RssFeed("https://www.thehindu.com/news/international/feeder/default.rss", "world"),

            // Business
            new RssFeed("https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms", "business"),
            new RssFeed("https://www.business-standard.com/rss/home_page_top_stories.rss", "business"),

            // Technology
            new RssFeed("https://feeds.feedburner.com/gadgets360-latest", "technology"),
            new RssFeed("https://www.thehindu.com/sci-tech/technology/feeder/default.rss", "technology"),

            // Sports
            new RssFeed("https://feeds.feedburner.com/ndtvnews-sports", "sports"),
            new RssFeed("https://timesofindia.indiatimes.com/rssfeeds/4719148.cms", "sports"),

            // Entertainment
            new RssFeed("https://feeds.feedburner.com/ndtvnews-entertainment", "entertainment"),
            new RssFeed("https://timesofindia.indiatimes.com/rssfeeds/1081479906.cms", "entertainment"),

            // Science
            new RssFeed("https://www.thehindu.com/sci-tech/science/feeder/default.rss", "science"),

            // Politics
            new RssFeed("https://feeds.feedburner.com/ndtvnews-india-news", "politics"),
            new RssFeed("https://www.thehindu.com/news/national/feeder/default.rss", "politics"),

            // Health
            new RssFeed("https://timesofindia.indiatimes.com/rssfeeds/3908999.cms", "health-fitness"),

            // Automobile
            new RssFeed("https://feeds.feedburner.com/autocarindia/latest-news", "automobile"),

            // Startup
            new RssFeed("https://feeds.feedburner.com/YourStory", "startup"),

            // Law & Order
            new RssFeed("https://feeds.feedburner.com/ndtvnews-india-news", "law-order"),

            // Fashion
            new RssFeed("https://www.vogue.in/feed", "fashion")
    );
}

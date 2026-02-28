package com.pocketnews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

    @Component
    public class RssFeedClient {

        private static final Logger logger = LoggerFactory.getLogger(RssFeedClient.class);

        public record RawArticle(
                String title,
                String description,
                String sourceUrl,
                String sourceName,
                String imageUrl
        ) {}

        public List<RawArticle> fetchFeed(String feedUrl, String sourceName) {
            List<RawArticle> articles = new ArrayList<>();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new URL(feedUrl).openStream());
                doc.getDocumentElement().normalize();

                NodeList items = doc.getElementsByTagName("item");

                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);

                    String title = getTagValue("title", item);
                    String description = getTagValue("description", item);
                    String link = getTagValue("link", item);
                    String imageUrl = extractImageUrl(item);

                    if (title == null || title.isBlank()) continue;
                    if (description == null && title == null) continue;

                    articles.add(new RawArticle(
                            cleanHtml(title),
                            cleanHtml(description),
                            link,
                            sourceName,
                            imageUrl
                    ));
                }
            } catch (Exception e) {
                logger.error("Failed to fetch RSS feed {}: {}", feedUrl, e.getMessage());
            }
            return articles;
        }

        private String getTagValue(String tag, Element element) {
            NodeList list = element.getElementsByTagName(tag);
            if (list.getLength() == 0) return null;
            return list.item(0).getTextContent();
        }

        private String extractImageUrl(Element item) {
            // Try <media:content> tag first
            NodeList media = item.getElementsByTagName("media:content");
            if (media.getLength() > 0) {
                Element mediaEl = (Element) media.item(0);
                return mediaEl.getAttribute("url");
            }
            // Try <enclosure> tag
            NodeList enclosure = item.getElementsByTagName("enclosure");
            if (enclosure.getLength() > 0) {
                Element encEl = (Element) enclosure.item(0);
                return encEl.getAttribute("url");
            }
            return null;
        }

        private String cleanHtml(String text) {
            if (text == null) return null;
            return text.replaceAll("<[^>]*>", "").trim();
        }
}


package com.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    private final Map<String, Document> storage = new HashMap<>();

    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
        }
        Document existing = storage.get(document.getId());
        if (existing != null) {
            document.setCreated(existing.getCreated());
        } else {
            document.setCreated(Instant.now());
        }
        storage.put(document.getId(), document);
        return document;
    }


    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(doc -> filterByTitlePrefixes(doc, request.getTitlePrefixes()))
                .filter(doc -> filterByContent(doc, request.getContainsContents()))
                .filter(doc -> filterByAuthorID(doc, request.getAuthorIds()))
                .filter(doc -> filterByCreatedRange(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    private  boolean filterByTitlePrefixes(Document doc, List<String> titlePrefixes) {
        if(titlePrefixes ==null || titlePrefixes.isEmpty()) return true;
        return titlePrefixes.stream().anyMatch(prefix->doc.getTitle().startsWith(prefix));
    }

    private boolean filterByContent(Document doc, List<String> contents) {
        if (contents == null || contents.isEmpty()) return true;
        return contents.stream().anyMatch(content -> doc.getContent() != null && doc.getContent().contains(content));
    }

    private  boolean filterByAuthorID(Document doc, List<String> authorIDs) {
        if (authorIDs == null || authorIDs.isEmpty()) return true;
        return authorIDs.contains(doc.getAuthor().getId());
    }

    private boolean filterByCreatedRange(Document doc, Instant from, Instant to) {
        if (from == null && to == null) return true;
        if (from != null && doc.getCreated().isBefore(from)) return false;
        return to == null || !doc.getCreated().isAfter(to);
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        Document existing = storage.get(id);
        return Optional.ofNullable(existing);
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
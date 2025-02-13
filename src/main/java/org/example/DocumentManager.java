package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc.
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final List<Document> storage = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {

        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }
        if (document.getCreated() == null) {
            document.setCreated(Instant.now());
        }

        Optional<Document> existingDoc = findById(document.getId());
        existingDoc.ifPresent(storage::remove);
        storage.add(document);

        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {

        List<Document> result = storage.stream()
                .filter(doc -> matches(doc,request))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        Optional<Document> findResult = storage.stream()
                .filter(document -> document.getId().equals(id))
                .findFirst();

        return findResult;
    }

    // Check if the document matches the search request.
    private boolean matches(Document document, SearchRequest request) {
        boolean matches = false;

        // Check title prefixes
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            matches |= request.getTitlePrefixes().stream()
                    .anyMatch(prefix -> document.getTitle() != null && document.getTitle().startsWith(prefix));
        }

        // Check content contains
        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            matches |= request.getContainsContents().stream()
                    .anyMatch(content -> document.getContent() != null && document.getContent().contains(content));
        }

        // Check author IDs
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            matches |= (document.getAuthor() != null && request.getAuthorIds().contains(document.getAuthor().getId()));
        }

        // Check created from date
        if (request.getCreatedFrom() != null && document.getCreated() != null) {
            matches |= !document.getCreated().isBefore(request.getCreatedFrom());
        }

        // Check created to date
        if (request.getCreatedTo() != null && document.getCreated() != null) {
            matches |= !document.getCreated().isAfter(request.getCreatedTo());
        }

        return matches;
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
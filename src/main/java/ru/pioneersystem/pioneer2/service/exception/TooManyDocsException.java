package ru.pioneersystem.pioneer2.service.exception;

import ru.pioneersystem.pioneer2.model.Document;

import java.util.List;

public class TooManyDocsException extends ServiceException {
    private List<Document> documents;

    public TooManyDocsException(String message, List<Document> documents) {
        super(message);
        this.documents = documents;
    }

    public List<Document> getDocuments() {
        return documents;
    }
}

package ru.pioneersystem.pioneer2.dao.exception;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Document;

import java.util.List;

public class TooManyDocsDaoException extends DataAccessException {
    private List<Document> documents;

    public TooManyDocsDaoException(String message, List<Document> documents) {
        super(message);
        this.documents = documents;
    }

    public List<Document> getDocuments() {
        return documents;
    }
}

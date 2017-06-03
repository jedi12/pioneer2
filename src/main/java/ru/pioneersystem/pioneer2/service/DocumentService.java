package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface DocumentService {
    Document getDocument(int id) throws ServiceException;

    List<Document> getDocumentList() throws ServiceException;

    List<Document> getDocumentList(int partId) throws ServiceException;

    void createDocument(Document document) throws ServiceException;

    void updateDocument(Document document) throws ServiceException;

    void deleteDocument(int id) throws ServiceException, RestrictException;
}
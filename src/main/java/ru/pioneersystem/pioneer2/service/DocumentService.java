package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.Date;
import java.util.List;

public interface DocumentService {
    Document getDocument(int id) throws ServiceException;

    List<Document> getOnRouteDocumentList() throws ServiceException;

    List<Document> getDocumentListByPatrId(int partId) throws ServiceException;

    List<Document> getMyDocumentListOnDate(Date date) throws ServiceException;

    List<Document> getMyWorkingDocumentList() throws ServiceException;

    void createDocument(Document document) throws ServiceException;

    void updateDocument(Document document) throws ServiceException;

    void deleteDocument(int id) throws ServiceException, RestrictException;
}
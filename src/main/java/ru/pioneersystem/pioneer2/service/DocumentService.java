package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.RoutePoint;
import ru.pioneersystem.pioneer2.service.exception.LockException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.Date;
import java.util.List;

public interface DocumentService {

    List<Document> getOnRouteDocumentList() throws ServiceException;

    List<Document> getDocumentListByPatrId(int partId) throws ServiceException;

    List<Document> getMyDocumentListOnDate(Date date) throws ServiceException;

    List<Document> getMyWorkingDocumentList() throws ServiceException;

    List<RoutePoint> getDocumentRoute(int documentId) throws ServiceException;

    List<String> getDocToCancelByGroup(int groupId) throws ServiceException;

    List<String> getDocToCancelByRole(int roleId) throws ServiceException;

    Document getNewDocument(int templateId) throws ServiceException;

    Document getDocument(int id) throws ServiceException;

    void saveDocument(Document document) throws ServiceException, LockException;

    void saveAndSendDocument(Document document) throws ServiceException, LockException;

    void deleteDocument(Document document) throws ServiceException, LockException;

    void copyDocument(Document document) throws ServiceException, LockException;

    void recallDocument(Document document) throws ServiceException, LockException;

    void publishDocument(Document document) throws ServiceException;

    void cancelPublishDocument(Document document) throws ServiceException;

    void acceptDocument(Document document) throws ServiceException, LockException;

    void rejectDocument(Document document) throws ServiceException, LockException;
}
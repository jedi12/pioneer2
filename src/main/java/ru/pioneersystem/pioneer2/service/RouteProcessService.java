package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.RoutePoint;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface RouteProcessService {

    List<RoutePoint> getDocumentRoute(int documentId) throws ServiceException;

    List<Integer> getCurrNotSignedRoutePointGroups(int documentId) throws ServiceException;

    void createRouteProcess(Document document) throws ServiceException;

    void startRouteProcess(Document document) throws ServiceException;

    void acceptRoutePointProcess(Document document) throws ServiceException;

    void rejectRoutePointProcess(Document document) throws ServiceException;

    void cancelRouteProcess(Document document) throws ServiceException;
}
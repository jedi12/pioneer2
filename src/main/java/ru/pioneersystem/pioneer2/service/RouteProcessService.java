package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

public interface RouteProcessService {

    void createRouteProcess(Document document) throws ServiceException;

    void startRouteProcess(Document document) throws ServiceException;

    void acceptRoutePointProcess(Document document) throws ServiceException;

    void rejectRoutePointProcess(Document document) throws ServiceException;

    void cancelRouteProcess(Document document) throws ServiceException;
}
package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.UserLockException;

public interface RouteProcessService {

    void createRouteProcess(Document document) throws ServiceException, UserLockException;
}
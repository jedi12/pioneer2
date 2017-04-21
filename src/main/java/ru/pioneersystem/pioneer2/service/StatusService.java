package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface StatusService {
    Status getStatus(int id) throws ServiceException;

    List<Status> getStatusList() throws ServiceException;
}
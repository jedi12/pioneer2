package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface StatusService {

    Status getStatus(int statusId) throws ServiceException;

    List<Status> getStatusList() throws ServiceException;

    Map<String, Integer> getStatusMap() throws ServiceException;
}
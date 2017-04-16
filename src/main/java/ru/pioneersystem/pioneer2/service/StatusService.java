package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Locale;

public interface StatusService {
    Status getStatus(int id, Locale locale) throws ServiceException;

    List<Status> getStatusList(int companyId, Locale locale) throws ServiceException;
}
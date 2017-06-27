package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.File;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

public interface FileService {
    File getFile(int fileId) throws ServiceException;

}
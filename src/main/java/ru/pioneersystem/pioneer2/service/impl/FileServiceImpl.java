package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.FileDao;
import ru.pioneersystem.pioneer2.model.File;
import ru.pioneersystem.pioneer2.service.FileService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

@Service("fileService")
public class FileServiceImpl implements FileService {
    private Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private FileDao fileDao;

    @Autowired
    public FileServiceImpl(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    @Override
    public File getFile(int fileId) throws ServiceException {
        try {
            return fileDao.get(fileId);
        } catch (DataAccessException e) {
            log.error("Can't get File by id", e);
            throw new ServiceException("Can't get File by id", e);
        }
    }
}

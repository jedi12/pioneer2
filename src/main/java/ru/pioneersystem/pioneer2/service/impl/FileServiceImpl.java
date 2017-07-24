package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.FileDao;
import ru.pioneersystem.pioneer2.model.File;
import ru.pioneersystem.pioneer2.service.FileService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

@Service("fileService")
public class FileServiceImpl implements FileService {
    private Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private FileDao fileDao;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public FileServiceImpl(FileDao fileDao, LocaleBean localeBean, MessageSource messageSource) {
        this.fileDao = fileDao;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public File getFile(int fileId) throws ServiceException {
        try {
            return fileDao.get(fileId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.file.not.downloaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }
}

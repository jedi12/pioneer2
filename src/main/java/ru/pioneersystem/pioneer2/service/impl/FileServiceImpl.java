package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.FileDao;
import ru.pioneersystem.pioneer2.model.File;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.FileService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

@Service("fileService")
public class FileServiceImpl implements FileService {
    private EventService eventService;
    private FileDao fileDao;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public FileServiceImpl(EventService eventService, FileDao fileDao, LocaleBean localeBean,
                           MessageSource messageSource) {
        this.eventService = eventService;
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
            eventService.logError(mess, e.getMessage(), fileId);
            throw new ServiceException(mess, e);
        }
    }
}

package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.StatusDao;
import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.StatusService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("statusService")
public class StatusServiceImpl implements StatusService {
    private EventService eventService;
    private StatusDao statusDao;
    private DictionaryService dictionaryService;
    private LocaleBean localeBean;
    private CurrentUser currentUser;
    private MessageSource messageSource;

    @Autowired
    public StatusServiceImpl(EventService eventService, StatusDao statusDao, DictionaryService dictionaryService,
                             LocaleBean localeBean, CurrentUser currentUser, MessageSource messageSource) {
        this.eventService = eventService;
        this.statusDao = statusDao;
        this.dictionaryService = dictionaryService;
        this.localeBean = localeBean;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Override
    public Status getStatus(int statusId) throws ServiceException {
        try {
            Status status = statusDao.get(statusId, currentUser.getUser().getCompanyId());
            String statusName = dictionaryService.getLocalizedStatusName(status.getId(), localeBean.getLocale());
            if (statusName != null) {
                status.setName(statusName);
            }
            return status;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.status.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), statusId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Status> getStatusList() throws ServiceException {
        try {
            List<Status> statuses = statusDao.getList(currentUser.getUser().getCompanyId());
            for (Status status : statuses) {
                String statusName = dictionaryService.getLocalizedStatusName(status.getId(), localeBean.getLocale());
                if (statusName != null) {
                    status.setName(statusName);
                }
            }
            return statuses;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.status.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getStatusMap() throws ServiceException {
        Map<String, Integer> statusMap = new LinkedHashMap<>();
        for (Status status : getStatusList()) {
            statusMap.put(status.getName(), status.getId());
        }
        return statusMap;
    }
}

package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.StatusDao;
import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.service.StatusService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.List;

@Service("statusService")
public class StatusServiceImpl implements StatusService {
    private Logger log = LoggerFactory.getLogger(StatusServiceImpl.class);

    private StatusDao statusDao;
    private LocaleBean localeBean;
    private CurrentUser currentUser;
    private MessageSource messageSource;

    @Autowired
    public StatusServiceImpl(StatusDao statusDao, LocaleBean localeBean, CurrentUser currentUser,
                             MessageSource messageSource) {
        this.statusDao = statusDao;
        this.localeBean = localeBean;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Override
    public Status getStatus(int statusId) throws ServiceException {
        try {
            return setLocalizedStatusName(statusDao.get(statusId, currentUser.getUser().getCompanyId()));
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.status.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Status> getStatusList() throws ServiceException {
        try {
            List<Status> statuses = statusDao.getList(currentUser.getUser().getCompanyId());
            for (Status status : statuses) {
                setLocalizedStatusName(status);
            }
            return statuses;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.status.NotLoadedList", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    private Status setLocalizedStatusName(Status status) {
        if (status.getState() == Status.State.SYSTEM) {
            switch (status.getId()) {
                case Status.Id.DELETED:
                    status.setName(messageSource.getMessage("status.name.deleted", null, localeBean.getLocale()));
                    break;
                case Status.Id.CANCELED:
                    status.setName(messageSource.getMessage("status.name.canceled", null, localeBean.getLocale()));
                    break;
                case Status.Id.COMPLETED:
                    status.setName(messageSource.getMessage("status.name.completed", null, localeBean.getLocale()));
                    break;
                case Status.Id.EXECUTED:
                    status.setName(messageSource.getMessage("status.name.executed", null, localeBean.getLocale()));
                    break;
                case Status.Id.PUBLISHED:
                    status.setName(messageSource.getMessage("status.name.published", null, localeBean.getLocale()));
                    break;
                case Status.Id.REZ1:
                    status.setName(messageSource.getMessage("status.name.rez1", null, localeBean.getLocale()));
                    break;
                case Status.Id.REZ2:
                    status.setName(messageSource.getMessage("status.name.rez2", null, localeBean.getLocale()));
                    break;
                case Status.Id.REZ3:
                    status.setName(messageSource.getMessage("status.name.rez3", null, localeBean.getLocale()));
                    break;
                case Status.Id.CREATED:
                    status.setName(messageSource.getMessage("status.name.created", null, localeBean.getLocale()));
                    break;
                case Status.Id.ON_COORDINATION:
                    status.setName(messageSource.getMessage("status.name.onCoordination", null, localeBean.getLocale()));
                    break;
                case Status.Id.ON_EXECUTION:
                    status.setName(messageSource.getMessage("status.name.onExecution", null, localeBean.getLocale()));
                    break;
                default:
                    status.setName("Unknown");
            }
        }
        return status;
    }
}

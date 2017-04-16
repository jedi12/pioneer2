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

import java.util.List;
import java.util.Locale;

@Service("statusService")
public class StatusServiceImpl implements StatusService {
    private Logger log = LoggerFactory.getLogger(StatusServiceImpl.class);

    private StatusDao statusDao;
    private MessageSource messageSource;

    @Autowired
    public StatusServiceImpl(StatusDao statusDao, MessageSource messageSource) {
        this.statusDao = statusDao;
        this.messageSource = messageSource;
    }

    @Override
    public Status getStatus(int id, Locale locale) throws ServiceException {
        try {
            return setStatusName(statusDao.get(id), locale);
        } catch (DataAccessException e) {
            log.error("Can't get Status by id", e);
            throw new ServiceException("Can't get Status by id", e);
        }
    }

    @Override
    public List<Status> getStatusList(int companyId, Locale locale) throws ServiceException {
        try {
            List<Status> statuses = statusDao.getList(companyId);
            for (Status status : statuses) {
                setStatusName(status, locale);
            }
            return statuses;
        } catch (DataAccessException e) {
            log.error("Can't get list of Status", e);
            throw new ServiceException("Can't get list of Status", e);
        }
    }

    private Status setStatusName(Status status, Locale locale) {
        if (status.getState() == Status.State.SYSTEM) {
            switch (status.getId()) {
                case Status.Id.DELETED:
                    status.setName(messageSource.getMessage("status.name.deleted", null, locale));
                    break;
                case Status.Id.CANCELED:
                    status.setName(messageSource.getMessage("status.name.canceled", null, locale));
                    break;
                case Status.Id.COMPLETED:
                    status.setName(messageSource.getMessage("status.name.completed", null, locale));
                    break;
                case Status.Id.EXECUTED:
                    status.setName(messageSource.getMessage("status.name.executed", null, locale));
                    break;
                case Status.Id.PUBLISHED:
                    status.setName(messageSource.getMessage("status.name.published", null, locale));
                    break;
                case Status.Id.REZ1:
                    status.setName(messageSource.getMessage("status.name.rez1", null, locale));
                    break;
                case Status.Id.REZ2:
                    status.setName(messageSource.getMessage("status.name.rez2", null, locale));
                    break;
                case Status.Id.REZ3:
                    status.setName(messageSource.getMessage("status.name.rez3", null, locale));
                    break;
                case Status.Id.CREATED:
                    status.setName(messageSource.getMessage("status.name.created", null, locale));
                    break;
                case Status.Id.ON_COORDINATION:
                    status.setName(messageSource.getMessage("status.name.onCoordination", null, locale));
                    break;
                case Status.Id.ON_EXECUTION:
                    status.setName(messageSource.getMessage("status.name.onExecution", null, locale));
                    break;
                default:
                    status.setName("Unknown");
            }
        }
        return status;
    }
}

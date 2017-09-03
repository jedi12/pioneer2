package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.EventDao;
import ru.pioneersystem.pioneer2.dao.exception.TooManyRowsDaoException;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.service.exception.TooManyObjectsException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service("eventService")
public class EventServiceImpl implements EventService {
    private Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private EventDao eventDao;
    private DictionaryService dictionaryService;
    private LocaleBean localeBean;
    private CurrentUser currentUser;
    private MessageSource messageSource;

    @Autowired
    public EventServiceImpl(EventDao eventDao, DictionaryService dictionaryService, LocaleBean localeBean,
                            CurrentUser currentUser, MessageSource messageSource) {
        this.eventDao = eventDao;
        this.dictionaryService = dictionaryService;
        this.localeBean = localeBean;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Override
    public void logError(String detail1, String detail2) {
        logEvent(Event.Type.ERROR, 0, detail1, detail2);
    }

    @Override
    public void logError(String detail1, String detail2, int objectId) {
        logEvent(Event.Type.ERROR, objectId, detail1, detail2);
    }

    @Override
    public void logEvent(int eventType, int objectId) {
        logEvent(eventType, objectId, null, null);
    }

    @Override
    public void logEvent(int eventType, int objectId, String detail1) {
        logEvent(eventType, objectId, detail1, null);
    }

    @Override
    public void logEvent(int eventType, int objectId, String detail1, String detail2) {
        int userId = 0;
        int companyId = 0;
        try {
            if (currentUser.getUser() != null) {
                userId = currentUser.getUser().getId();
                companyId = currentUser.getUser().getCompanyId();
            }

            if (detail1 != null && detail1.length() > 100) {
                detail1 = detail1.substring(0, 100);
            }

            if (detail2 != null && detail2.length() > 500) {
                detail2 = detail2.substring(0, 500);
            }

            Event event = new Event(new Date(), userId, eventType, objectId, detail1, detail2);
            eventDao.create(event, companyId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.event.NotSaved",
                    new Object[]{userId, eventType, objectId, detail1, detail2}, localeBean.getLocale());
            log.error(mess, e);
        }
    }

    @Override
    public List<Event> getEventList(Date fromDate, Date toDate) throws ServiceException {
        List<Event> events;
        try {
            toDate = Date.from(toDate.toInstant().plus(1, ChronoUnit.DAYS));
            if (currentUser.isSuperRole()) {
                events = eventDao.getSuperList(fromDate, toDate);
            } else {
                events = eventDao.getAdminList(fromDate, toDate, currentUser.getUser().getCompanyId());
            }
            processEvents(events);
            return events;
        } catch (TooManyRowsDaoException e) {
            events = e.getObject();
            processEvents(events);
            String mess = messageSource.getMessage("error.event.TooManyDocsFound", null, localeBean.getLocale());
            logError(mess, null);
            throw new TooManyObjectsException(mess, events);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.event.NotLoadedList", null, localeBean.getLocale());
            logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public String getEventDetail(Date eventDate, int eventUser) throws ServiceException {
        try {
            return eventDao.getDetail(eventDate, eventUser, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.event.NotLoaded", null, localeBean.getLocale());
            logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    private void processEvents(List<Event> events) {
        for (Event event : events) {
            String newId = event.getDate().getTime() + "_" + event.getUserId();
            event.setId(newId);

            offsetDateAndFormat(event);

            String eventTypeName = dictionaryService.getLocalizedEventTypeName(event.getTypeId());
            if (eventTypeName != null) {
                event.setTypeName(eventTypeName);
            }
        }
    }

    private void offsetDateAndFormat(Event event) {
        Date date = event.getDate();
        if (date != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), localeBean.getZoneId());
//            event.setDate(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            event.setDateFormatted(localDateTime.format(localeBean.getDateTimeFormatter()));
        }
    }
}

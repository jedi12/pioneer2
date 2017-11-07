package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.MailProcessDao;
import ru.pioneersystem.pioneer2.dao.exception.TooManyRowsDaoException;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.Notice;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.TooManyObjectsException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service("noticeService")
public class NoticeServiceImpl implements NoticeService {
    private MailProcessDao mailProcessDao;
    private CurrentUser currentUser;
    private EventService eventService;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private DictionaryService dictionaryService;

    @Autowired
    public NoticeServiceImpl(MailProcessDao mailProcessDao, CurrentUser currentUser, EventService eventService,
                             LocaleBean localeBean, MessageSource messageSource,
                             DictionaryService dictionaryService) {
        this.mailProcessDao = mailProcessDao;
        this.currentUser = currentUser;
        this.eventService = eventService;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
        this.dictionaryService = dictionaryService;
    }

    @Override
    public List<Notice> getNoticeList(Date fromDate, Date toDate) throws ServiceException {
        List<Notice> notices;
        try {
            toDate = Date.from(toDate.toInstant().plus(1, ChronoUnit.DAYS));
            if (currentUser.isSuperRole()) {
                notices = mailProcessDao.getSuperList(fromDate, toDate);
            } else {
                notices = mailProcessDao.getAdminList(fromDate, toDate, currentUser.getUser().getCompanyId());
            }
            processNotice(notices);
            eventService.logEvent(Event.Type.NOTICE_FIND, 0);
            return notices;
        } catch (TooManyRowsDaoException e) {
            notices = e.getObject();
            processNotice(notices);
            String mess = messageSource.getMessage("error.notices.TooManyNoticesFound", null, localeBean.getLocale());
            eventService.logEvent(Event.Type.NOTICE_FIND_RESTRICTION, 0, mess);
            throw new TooManyObjectsException(mess, notices);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.notices.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public String getNoticeInfo(int noticeId) throws ServiceException {
        try {
            return mailProcessDao.getInfo(noticeId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.notices.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    private void processNotice(List<Notice> notices) {
        for (Notice notice : notices) {
            offsetDateAndFormat(notice);

            String eventTypeName = dictionaryService.getLocalizedEventTypeName(notice.getEventId(), localeBean.getLocale());
            if (eventTypeName != null) {
                notice.setEventName(eventTypeName);
            }

            String statusName = dictionaryService.getLocalizedStatusName(notice.getDocId(), localeBean.getLocale());
            if (statusName != null) {
                notice.setDocStatusName(statusName);
            }

            String emailSendStatusName = dictionaryService.getLocalizedEmailSendStatus(notice.getSendStatusId(), localeBean.getLocale());
            if (emailSendStatusName != null) {
                notice.setSendStatusName(emailSendStatusName);
            }
        }
    }

    private void offsetDateAndFormat(Notice notice) {
        Date date = notice.getChangeDate();
        if (date != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), localeBean.getZoneId());
//            event.setDate(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            notice.setChangeDateFormatted(localDateTime.format(localeBean.getDateTimeFormatter()));
        }
    }
}

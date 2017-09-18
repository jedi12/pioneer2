package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.SearchDao;
import ru.pioneersystem.pioneer2.dao.exception.TooManyRowsDaoException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.SearchDoc;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.SearchService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.TooManyObjectsException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service("searchService")
public class SearchServiceImpl implements SearchService {
    private EventService eventService;
    private SearchDao searchDao;
    private LocaleBean localeBean;
    private CurrentUser currentUser;
    private MessageSource messageSource;
    private DictionaryService dictionaryService;

    @Autowired
    public SearchServiceImpl(EventService eventService, SearchDao searchDao, LocaleBean localeBean,
                             CurrentUser currentUser, MessageSource messageSource, DictionaryService dictionaryService) {
        this.eventService = eventService;
        this.searchDao = searchDao;
        this.localeBean = localeBean;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
        this.dictionaryService = dictionaryService;
    }

    @Override
    public List<Document> findList(SearchDoc searchDoc) throws ServiceException {
        List<Document> documents;
        try {
            if (currentUser.isSuperRole()) {
                documents = searchDao.findForSuperList(searchDoc);
            } else if (currentUser.isAdminRole()) {
                documents = searchDao.findForAdminList(searchDoc, currentUser.getUser().getCompanyId());
            } else {
                documents = searchDao.findForUserList(searchDoc, currentUser.getUser().getId(),
                        currentUser.getUser().getCompanyId());
            }

            for (Document document : documents) {
                String statusName = dictionaryService.getLocalizedStatusName(document.getStatusId(), localeBean.getLocale());
                if (statusName != null) {
                    document.setStatusName(statusName);
                }
            }

            offsetDateAndFormat(documents);
            eventService.logEvent(Event.Type.SEARCH_FIND, 0);
            return documents;
        } catch (TooManyRowsDaoException e) {
            documents = e.getObject();
            offsetDateAndFormat(documents);
            String mess = messageSource.getMessage("error.search.TooManyDocsFound", null, localeBean.getLocale());
            eventService.logEvent(Event.Type.SEARCH_FIND, 0, mess);
            throw new TooManyObjectsException(mess, documents);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.search.NotFound", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    private void offsetDateAndFormat(List<Document> documents) {
        for (Document document: documents) {
            Date changeDate = document.getChangeDate();
            if (changeDate != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(changeDate.toInstant(), localeBean.getZoneId());
//                document.setChangeDate(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                document.setChangeDateFormatted(localDateTime.format(localeBean.getDateTimeFormatter()));
            }
        }
    }
}

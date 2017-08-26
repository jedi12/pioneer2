package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.SearchDao;
import ru.pioneersystem.pioneer2.dao.exception.TooManyDocsDaoException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.SearchDoc;
import ru.pioneersystem.pioneer2.service.SearchService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.TooManyDocsException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service("searchService")
public class SearchServiceImpl implements SearchService {
    private Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private SearchDao searchDao;
    private LocaleBean localeBean;
    private CurrentUser currentUser;
    private MessageSource messageSource;

    @Autowired
    public SearchServiceImpl(SearchDao searchDao, LocaleBean localeBean, CurrentUser currentUser,
                             MessageSource messageSource) {
        this.searchDao = searchDao;
        this.localeBean = localeBean;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Override
    public List<Document> findList(SearchDoc searchDoc) throws ServiceException {
        List<Document> documents;
        try {
            if (currentUser.isAdminRole()) {
                documents = searchDao.findForAdminList(searchDoc, currentUser.getUser().getCompanyId());
            } else {
                documents = searchDao.findForUserList(searchDoc, currentUser.getUser().getId(),
                        currentUser.getUser().getCompanyId());
            }
            offsetDateAndFormat(documents);
            return documents;
        } catch (TooManyDocsDaoException e) {
            documents = e.getDocuments();
            offsetDateAndFormat(documents);

            String mess = messageSource.getMessage("error.search.TooManyDocsFound", null, localeBean.getLocale());
            log.error(mess, e);
            throw new TooManyDocsException(mess, documents);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.search.NotFound", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    private void offsetDateAndFormat(List<Document> documents) {
        for (Document document: documents) {
            Date changeDate = document.getChangeDate();
            if (changeDate != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(changeDate.toInstant(), localeBean.getZoneId());
                document.setChangeDate(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                document.setChangeDateFormatted(localDateTime.format(localeBean.getDateTimeFormatter()));
            }
        }
    }
}

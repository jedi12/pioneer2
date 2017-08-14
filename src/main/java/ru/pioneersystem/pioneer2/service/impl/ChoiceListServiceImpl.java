package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.ChoiceListDao;
import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.service.ChoiceListService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("choiceListService")
public class ChoiceListServiceImpl implements ChoiceListService {
    private Logger log = LoggerFactory.getLogger(ChoiceListServiceImpl.class);

    private ChoiceListDao choiceListDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public ChoiceListServiceImpl(ChoiceListDao choiceListDao, CurrentUser currentUser, LocaleBean localeBean,
                                 MessageSource messageSource) {
        this.choiceListDao = choiceListDao;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<ChoiceList> getChoiceListList() throws ServiceException {
        try {
            return choiceListDao.getList(currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotLoadedList", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, ChoiceList> getChoiceListMap() throws ServiceException {
        Map<String, ChoiceList> choiceLists = new LinkedHashMap<>();
        for (ChoiceList choiceList : getChoiceListList()) {
            choiceLists.put(choiceList.getName(), choiceList);
        }
        return choiceLists;
    }

    @Override
    public void setChoiceListForTemplate(Document document) throws ServiceException {
        try {
            Map<Integer, List<String>> choiceListsForDocument = choiceListDao.getForTemplate(document.getTemplateId());
            for (Document.Field field : document.getFields()) {
                if (field.getChoiceListId() != null) {
                    List<String> choiceList = choiceListsForDocument.get(field.getChoiceListId());
                    if (choiceList != null && choiceList.size() != 0) {
                        field.setChoiceListValues(choiceList);
                    }
                }
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotSetChoiceListsForTemplate", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void setChoiceListsForDocument(Document document) throws ServiceException {
        try {
            Map<Integer, List<String>> choiceListsForDocument = choiceListDao.getForDocument(document.getId());
            for (Document.Field field : document.getFields()) {
                if (field.getChoiceListId() != null) {
                    List<String> choiceList = choiceListsForDocument.get(field.getChoiceListId());
                    if (choiceList != null && choiceList.size() != 0) {
                        field.setChoiceListValues(choiceList);
                    }
                }
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotSetChoiceListsForDocument", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public ChoiceList getNewChoiceList() {
        ChoiceList choiceList = new ChoiceList();
        choiceList.setValues(new ArrayList<>());
        choiceList.setCreateFlag(true);
        return choiceList;
    }

    @Override
    public ChoiceList getChoiceList(int id) throws ServiceException {
        try {
            ChoiceList choiceList = choiceListDao.get(id, currentUser.getUser().getCompanyId());
            choiceList.setCreateFlag(false);
            return choiceList;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveChoiceList(ChoiceList choiceList) throws ServiceException {
        try {
            if (choiceList.isCreateFlag()) {
                choiceListDao.create(choiceList, currentUser.getUser().getCompanyId());
            } else {
                choiceListDao.update(choiceList, currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotSaved", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteChoiceList(int id) throws ServiceException {
        // TODO: 28.02.2017 Сделать проверку, используется ли данный список в шаблоне или нет
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            choiceListDao.delete(id, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotDeleted", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }
}

package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.ChoiceListDao;
import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.service.ChoiceListService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("choiceListService")
public class ChoiceListServiceImpl implements ChoiceListService {
    private EventService eventService;
    private ChoiceListDao choiceListDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public ChoiceListServiceImpl(EventService eventService, ChoiceListDao choiceListDao, CurrentUser currentUser,
                                 LocaleBean localeBean, MessageSource messageSource) {
        this.eventService = eventService;
        this.choiceListDao = choiceListDao;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<ChoiceList> getChoiceListList() throws ServiceException {
        try {
            List<ChoiceList> choiceLists;
            if (currentUser.isSuperRole()) {
                choiceLists = choiceListDao.getSuperList();
            } else {
                choiceLists = choiceListDao.getAdminList(currentUser.getUser().getCompanyId());
            }
            return choiceLists;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
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
            eventService.logError(mess, e.getMessage(), document.getId());
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
            eventService.logError(mess, e.getMessage(), document.getId());
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
    public ChoiceList getChoiceList(ChoiceList selectedChoiceList) throws ServiceException {
        if (selectedChoiceList == null) {
            String mess = messageSource.getMessage("error.choiceList.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            int companyId;
            if (currentUser.isSuperRole()) {
                companyId = selectedChoiceList.getCompanyId();
            } else {
                companyId = currentUser.getUser().getCompanyId();
            }

            ChoiceList choiceList = choiceListDao.get(selectedChoiceList.getId(), companyId);
            choiceList.setCreateFlag(false);
            eventService.logEvent(Event.Type.LIST_GETED, selectedChoiceList.getId());
            return choiceList;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), selectedChoiceList.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveChoiceList(ChoiceList choiceList) throws ServiceException {
        try {
            if (choiceList.isCreateFlag()) {
                int choiceListId = choiceListDao.create(choiceList, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.LIST_CREATED, choiceListId);
            } else {
                choiceListDao.update(choiceList, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.LIST_CHANGED, choiceList.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), choiceList.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteChoiceList(ChoiceList choiceList) throws ServiceException {
        // TODO: 28.02.2017 Сделать проверку, используется ли данный список в шаблоне или нет
        try {
            choiceListDao.delete(choiceList.getId(), currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.LIST_DELETED, choiceList.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.choiceList.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), choiceList.getId());
            throw new ServiceException(mess, e);
        }
    }
}

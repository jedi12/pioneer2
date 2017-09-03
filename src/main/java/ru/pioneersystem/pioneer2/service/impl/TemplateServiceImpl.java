package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.TemplateDao;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.FieldType;
import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.TemplateService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service("templateService")
public class TemplateServiceImpl implements TemplateService {
    private EventService eventService;
    private TemplateDao templateDao;
    private DictionaryService dictionaryService;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public TemplateServiceImpl(EventService eventService, TemplateDao templateDao, DictionaryService dictionaryService,
                               CurrentUser currentUser, LocaleBean localeBean, MessageSource messageSource) {
        this.eventService = eventService;
        this.templateDao = templateDao;
        this.dictionaryService = dictionaryService;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<Template> getTemplateList() throws ServiceException {
        try {
            List<Template> templateList = templateDao.getList(currentUser.getUser().getCompanyId());

            String noRouteName = messageSource.getMessage("route.zero.name", null, localeBean.getLocale());
            String noPartName = messageSource.getMessage("part.zero.name", null, localeBean.getLocale());
            for (Template template: templateList) {
                if (template.getRouteId() == 0) {
                    template.setRouteName(noRouteName);
                }
                if (template.getPartId() == 0) {
                    template.setPartName(noPartName);
                }
            }
            return templateList;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Template> getTemplateList(int partId) throws ServiceException {
        try {
            return templateDao.getListByPartId(partId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.partTemplateNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), partId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> getListContainingChoiceList(int choiceListId) throws ServiceException {
        try {
            return templateDao.getListContainingChoiceList(choiceListId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.templateContainingChoiceListNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), choiceListId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> getListContainingRoute(int routeId) throws ServiceException {
        try {
            return templateDao.getListContainingRoute(routeId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.templateContainingRouteNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), routeId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getForSearchTemplateMap() throws ServiceException {
        try {
            if (currentUser.isAdminRole()) {
                return templateDao.getTemplateMap(currentUser.getUser().getCompanyId());
            } else {
                return templateDao.getUserTemplateMap(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.userTemplateNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Template getNewTemplate() {
        Template template = new Template();
        template.setFields(new LinkedList<>());
        template.setConditions(new ArrayList<>());
        template.setCreateFlag(true);
        return template;
    }

    @Override
    public Template getTemplate(int templateId) throws ServiceException {
        try {
            Template template = templateDao.get(templateId, currentUser.getUser().getCompanyId());
            for (Document.Field field: template.getFields()) {
                String fieldTypeName = dictionaryService.getLocalizedFieldTypeName(field.getTypeId());
                if (fieldTypeName != null) {
                    field.setTypeName(fieldTypeName);
                }
                if (field.getTypeId() == FieldType.Id.LIST) {
                    field.setTypeName(field.getTypeName() + " (" + field.getChoiceListName() + ")");
                }
            }

            template.setCreateFlag(false);
            eventService.logEvent(Event.Type.TEMPLATE_GETED, templateId);
            return template;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), templateId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveTemplate(Template template) throws ServiceException {
        try {
            if (template.isCreateFlag()) {
                int templateId = templateDao.create(template, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.TEMPLATE_GETED, templateId);
            } else {
                templateDao.update(template, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.TEMPLATE_CHANGED, template.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), template.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteTemplate(int templateId) throws ServiceException {
        // TODO: 28.02.2017 Сделать проверку, используется ли данный шаблон или не нужна проверка вообще
        try {
            templateDao.delete(templateId, currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.TEMPLATE_DELETED, templateId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), templateId);
            throw new ServiceException(mess, e);
        }
    }
}

package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.TemplateDao;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.FieldType;
import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.TemplateService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
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
            List<Template> templates;
            if (currentUser.isSuperRole()) {
                templates = templateDao.getSuperList();
            } else {
                templates = templateDao.getAdminList(currentUser.getUser().getCompanyId());
            }

            String noRouteName = messageSource.getMessage("route.zero.name", null, localeBean.getLocale());
            String noPartName = messageSource.getMessage("part.zero.name", null, localeBean.getLocale());
            for (Template template: templates) {
                if (template.getRouteId() == 0) {
                    template.setRouteName(noRouteName);
                }
                if (template.getPartId() == 0) {
                    template.setPartName(noPartName);
                }
            }
            return templates;
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
    public Template getTemplate(Template selectedTemplate) throws ServiceException {
        if (selectedTemplate == null) {
            String mess = messageSource.getMessage("error.template.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            int companyId;
            if (currentUser.isSuperRole()) {
                companyId = selectedTemplate.getCompanyId();
            } else {
                companyId = currentUser.getUser().getCompanyId();
            }

            Template template = templateDao.get(selectedTemplate.getId(), companyId);
            for (Document.Field field: template.getFields()) {
                String fieldTypeName = dictionaryService.getLocalizedFieldTypeName(field.getTypeId(), localeBean.getLocale());
                if (fieldTypeName != null) {
                    field.setTypeName(fieldTypeName);
                }
                if (field.getTypeId() == FieldType.Id.LIST) {
                    field.setTypeName(field.getTypeName() + " (" + field.getChoiceListName() + ")");
                }
            }

            template.setCreateFlag(false);
            eventService.logEvent(Event.Type.TEMPLATE_GETED, selectedTemplate.getId());
            return template;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), selectedTemplate.getId());
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
    public void deleteTemplate(Template template) throws ServiceException {
        // TODO: 28.02.2017 Сделать проверку, используется ли данный шаблон или не нужна проверка вообще
        try {
            templateDao.delete(template.getId(), currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.TEMPLATE_DELETED, template.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), template.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createExampleTemplate(int routeId, int partId, int choiceListId, int companyId) throws ServiceException {
        try {
            Template template = getNewTemplate();
            template.setName(messageSource.getMessage("template.example.name", null, localeBean.getLocale()));
            template.setRouteId(routeId);
            template.setPartId(partId);

            Document.Field choiceListField = new Document.Field();
            choiceListField.setNum(1);
            choiceListField.setTypeId(FieldType.Id.LIST);
            choiceListField.setChoiceListId(choiceListId);
            choiceListField.setName(messageSource.getMessage("template.example.choiceList", null, localeBean.getLocale()));
            template.getFields().add(choiceListField);

            Document.Field checkboxField = new Document.Field();
            checkboxField.setNum(2);
            checkboxField.setTypeId(FieldType.Id.CHECKBOX);
            checkboxField.setName(messageSource.getMessage("template.example.checkbox", null, localeBean.getLocale()));
            template.getFields().add(checkboxField);

            Document.Field calendarField = new Document.Field();
            calendarField.setNum(3);
            calendarField.setTypeId(FieldType.Id.CALENDAR);
            calendarField.setName(messageSource.getMessage("template.example.calendar", null, localeBean.getLocale()));
            template.getFields().add(calendarField);

            Document.Field textStringField = new Document.Field();
            textStringField.setNum(4);
            textStringField.setTypeId(FieldType.Id.TEXT_STRING);
            textStringField.setName(messageSource.getMessage("template.example.textstring", null, localeBean.getLocale()));
            template.getFields().add(textStringField);

            Document.Field textAreaField = new Document.Field();
            textAreaField.setNum(5);
            textAreaField.setTypeId(FieldType.Id.TEXT_AREA);
            textAreaField.setName(messageSource.getMessage("template.example.textarea", null, localeBean.getLocale()));
            template.getFields().add(textAreaField);

            return templateDao.create(template, companyId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.exampleNotCreated", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }
}

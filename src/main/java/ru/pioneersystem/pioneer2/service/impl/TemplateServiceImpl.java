package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.TemplateDao;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.FieldTypeService;
import ru.pioneersystem.pioneer2.service.TemplateService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service("templateService")
public class TemplateServiceImpl implements TemplateService {
    private Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);

    private TemplateDao templateDao;
    private FieldTypeService fieldTypeService;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public TemplateServiceImpl(TemplateDao templateDao, FieldTypeService fieldTypeService, CurrentUser currentUser,
                               LocaleBean localeBean, MessageSource messageSource) {
        this.templateDao = templateDao;
        this.fieldTypeService = fieldTypeService;
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
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Template> getTemplateList(int partId) throws ServiceException {
        try {
            return templateDao.getListByPartId(partId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.partTemplateNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> getListContainingChoiceList(int choiceListId) throws ServiceException {
        try {
            return templateDao.getListContainingChoiceList(choiceListId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.templateContainingChoiceListNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> getListContainingRoute(int routeId) throws ServiceException {
        try {
            return templateDao.getListContainingRoute(routeId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.templateContainingRouteNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
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
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Template getNewTemplate() {
        Template template = new Template();
        template.setFields(new LinkedList<>());
        template.setConditions(new LinkedList<>());
        template.setCreateFlag(true);
        return template;
    }

    @Override
    public Template getTemplate(int templateId) throws ServiceException {
        try {
            Template template = fieldTypeService.setLocalizedFieldTypeName(templateDao.get(templateId, currentUser.getUser().getCompanyId()));
            template.setCreateFlag(false);
            return template;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveTemplate(Template template) throws ServiceException {
        try {
            if (template.isCreateFlag()) {
                templateDao.create(template, currentUser.getUser().getCompanyId());
            } else {
                templateDao.update(template, currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotSaved", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteTemplate(int templateId) throws ServiceException {
        // TODO: 28.02.2017 Сделать проверку, используется ли данный шаблон или не нужна проверка вообще
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            templateDao.delete(templateId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.template.NotDeleted", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }
}

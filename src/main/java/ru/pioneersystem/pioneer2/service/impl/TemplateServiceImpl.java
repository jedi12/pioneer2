package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.TemplateDao;
import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.FieldTypeService;
import ru.pioneersystem.pioneer2.service.TemplateService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;

import java.util.List;

@Service("templateService")
public class TemplateServiceImpl implements TemplateService {
    private Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);

    private TemplateDao templateDao;
    private FieldTypeService fieldTypeService;
    private CurrentUser currentUser;

    @Autowired
    public TemplateServiceImpl(TemplateDao templateDao, FieldTypeService fieldTypeService, CurrentUser currentUser) {
        this.templateDao = templateDao;
        this.fieldTypeService = fieldTypeService;
        this.currentUser = currentUser;
    }

    @Override
    public Template getTemplate(int id) throws ServiceException {
        try {
            return fieldTypeService.setLocalizedFieldTypeName(templateDao.get(id));
        } catch (DataAccessException e) {
            log.error("Can't get Template by id", e);
            throw new ServiceException("Can't get Template by id", e);
        }
    }

    @Override
    public List<Template> getTemplateList() throws ServiceException {
        try {
            return templateDao.getList(currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't get list of Template", e);
            throw new ServiceException("Can't get list of Template", e);
        }
    }

    @Override
    public List<Template> getTemplateList(int partId) throws ServiceException {
        try {
            return templateDao.getListByPartId(partId);
        } catch (DataAccessException e) {
            log.error("Can't get list of Template by part id", e);
            throw new ServiceException("Can't get list of Template by part id", e);
        }
    }

    @Override
    public void createTemplate(Template template) throws ServiceException {
        try {
            templateDao.create(template, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't create Template", e);
            throw new ServiceException("Can't create Template", e);
        }
    }

    @Override
    public void updateTemplate(Template template) throws ServiceException {
        try {
            templateDao.update(template);
        } catch (DataAccessException e) {
            log.error("Can't update Template", e);
            throw new ServiceException("Can't update Template", e);
        }
    }

    @Override
    public void deleteTemplate(int id) throws ServiceException {
        // TODO: 28.02.2017 Сделать проверку, используется ли данный шаблон или не нужна проверка вообще
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            templateDao.delete(id);
        } catch (DataAccessException e) {
            log.error("Can't delete Template", e);
            throw new ServiceException("Can't delete Template", e);
        }
    }
}

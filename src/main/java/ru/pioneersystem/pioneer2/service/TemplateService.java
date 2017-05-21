package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface TemplateService {
    Template getTemplate(int id) throws ServiceException;

    List<Template> getTemplateList() throws ServiceException;

    void createTemplate(Template template) throws ServiceException;

    void updateTemplate(Template template) throws ServiceException;

    void deleteTemplate(int id) throws ServiceException, RestrictException;
}
package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface TemplateService {

    Template getTemplate(int templateId) throws ServiceException;

    List<Template> getTemplateList() throws ServiceException;

    List<Template> getTemplateList(int partId) throws ServiceException;

    void createTemplate(Template template) throws ServiceException;

    void updateTemplate(Template template) throws ServiceException;

    void deleteTemplate(int templateId) throws ServiceException;
}
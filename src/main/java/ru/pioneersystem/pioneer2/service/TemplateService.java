package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface TemplateService {

    List<Template> getTemplateList() throws ServiceException;

    List<Template> getTemplateList(int partId) throws ServiceException;

    List<String> getListContainingChoiceList(int choiceListId) throws ServiceException;

    List<String> getListContainingRoute(int routeId) throws ServiceException;

    Template getNewTemplate();

    Template getTemplate(int templateId) throws ServiceException;

    void saveTemplate(Template template) throws ServiceException;

    void deleteTemplate(int templateId) throws ServiceException;
}
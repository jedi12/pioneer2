package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface TemplateService {

    List<Template> getTemplateList() throws ServiceException;

    List<Template> getTemplateList(int partId) throws ServiceException;

    List<String> getListContainingChoiceList(int choiceListId) throws ServiceException;

    List<String> getListContainingRoute(int routeId) throws ServiceException;

    Map<String, Integer> getForSearchTemplateMap() throws ServiceException;

    Template getNewTemplate();

    Template getTemplate(Template selectedTemplate) throws ServiceException;

    void saveTemplate(Template template) throws ServiceException;

    void deleteTemplate(Template template) throws ServiceException;

    int createExampleTemplate(int routeId, int partId, int choiceListId, int companyId) throws ServiceException;
}
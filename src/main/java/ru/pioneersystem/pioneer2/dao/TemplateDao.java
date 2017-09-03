package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.model.Template;

import java.util.List;
import java.util.Map;

public interface TemplateDao {

    List<Template> getList(int companyId) throws DataAccessException;

    List<Template> getListByPartId(int partId, int companyId) throws DataAccessException;

    List<String> getListContainingChoiceList(int choiceListId, int companyId) throws DataAccessException;

    List<String> getListContainingRoute(int routeId, int companyId) throws DataAccessException;

    Map<String, Integer> getTemplateMap(int companyId) throws DataAccessException;

    Map<String, Integer> getUserTemplateMap(int userId, int companyId) throws DataAccessException;

    void removeFromParts(List<Part> parts, int companyId) throws DataAccessException;

    Template get(int templateId, int companyId) throws DataAccessException;

    int create(Template template, int companyId) throws DataAccessException;

    void update(Template template, int companyId) throws DataAccessException;

    void delete(int templateId, int companyId) throws DataAccessException;
}
package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Template;

import java.util.List;

public interface TemplateDao {

    List<Template> getList(int companyId) throws DataAccessException;

    List<Template> getListByPartId(int partId, int companyId) throws DataAccessException;

    Template get(int templateId, int companyId) throws DataAccessException;

    void create(Template template, int companyId) throws DataAccessException;

    void update(Template template, int companyId) throws DataAccessException;

    void delete(int templateId, int companyId) throws DataAccessException;
}
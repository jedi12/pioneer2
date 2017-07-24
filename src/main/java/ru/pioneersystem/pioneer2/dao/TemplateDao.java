package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Template;

import java.util.List;

public interface TemplateDao {

    Template get(int templateId) throws DataAccessException;

    List<Template> getList(int companyId) throws DataAccessException;

    List<Template> getListByPartId(int partId) throws DataAccessException;

    void create(Template template, int companyId) throws DataAccessException;

    void update(Template template) throws DataAccessException;

    void delete(int templateId) throws DataAccessException;
}
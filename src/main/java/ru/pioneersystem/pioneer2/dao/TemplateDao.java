package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Template;

import java.util.List;

public interface TemplateDao {

    Template get(int id) throws DataAccessException;

    List<Template> getList(int company) throws DataAccessException;

    List<Template> getListByPartId(int partId) throws DataAccessException;

    void create(Template template, int company) throws DataAccessException;

    void update(Template template) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}
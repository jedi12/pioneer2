package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Document;

import java.util.Date;
import java.util.List;

public interface DocumentDao {

    Document get(int id) throws DataAccessException;

    List<Document> getOnRouteList(int roleId, int userId) throws DataAccessException;

    List<Document> getListByPartId(int partId) throws DataAccessException;

    List<Document> getMyOnDateList(Date beginDate, Date endDate, int userId) throws DataAccessException;

    List<Document> getMyOnWorkingList(int userId) throws DataAccessException;

    void create(Document document, int company) throws DataAccessException;

    void update(Document document) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}
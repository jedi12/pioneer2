package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.dao.exception.LockDaoException;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DocumentDao {

    Document getTemplateBased(int templateId, Map<Integer, List<String>> choiceLists) throws DataAccessException;

    Document get(int documentId) throws DataAccessException;

    Document getForEdit(int documentId, Map<Integer, List<String>> choiceLists) throws DataAccessException;

    List<Document> getOnRouteList(int roleId, int userId) throws DataAccessException;

    List<Document> getListByPartId(int partId) throws DataAccessException;

    List<Document> getMyOnDateList(Date beginDate, Date endDate, int userId) throws DataAccessException;

    List<Document> getMyOnWorkingList(int userId) throws DataAccessException;

    void create(Document document, int userId, int company) throws DataAccessException;

    void update(Document document, int userId) throws DataAccessException;

    void delete(int documentId, int userId) throws DataAccessException;

    void publish(int documentId, int userId, int partId, boolean isPublic) throws DataAccessException;

    void lock(Document document) throws DataAccessException, LockDaoException, NotFoundDaoException;
}
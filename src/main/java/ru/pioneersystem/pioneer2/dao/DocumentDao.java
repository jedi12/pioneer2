package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Part;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DocumentDao {

    List<Document> getOnRouteList(int roleId, int userId, int companyId) throws DataAccessException;

    List<Document> getListByPartId(int partId, int companyId) throws DataAccessException;

    List<Document> getMyOnDateList(Date beginDate, Date endDate, int userId, int companyId) throws DataAccessException;

    List<Document> getMyOnWorkingList(int userId, int companyId) throws DataAccessException;

    Document getTemplateBased(int templateId, int companyId) throws DataAccessException;

    void cancelPublish(List<Part> parts, int userId, int companyId) throws DataAccessException;

    Document get(int documentId, int companyId) throws DataAccessException;

    void create(Document document, int userId, int companyId) throws DataAccessException;

    void update(Document document, int userId, int companyId) throws DataAccessException;

    void delete(int documentId, int userId, int companyId) throws DataAccessException;

    void publish(Document document, int userId, int companyId, boolean isPublic) throws DataAccessException;

    void lock(Document document, int companyId) throws DataAccessException;
}
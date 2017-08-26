package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.SearchDoc;

import java.util.List;

public interface SearchDao {

    List<Document> findForAdminList(SearchDoc searchDoc, int companyId) throws DataAccessException;

    List<Document> findForUserList(SearchDoc searchDoc, int userId, int companyId) throws DataAccessException;
}
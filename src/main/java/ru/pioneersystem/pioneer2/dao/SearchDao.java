package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.SearchFilter;

import java.util.List;

public interface SearchDao {

    List<Document> findForSuperList(SearchFilter searchFilter) throws DataAccessException;

    List<Document> findForAdminList(SearchFilter searchFilter, int companyId) throws DataAccessException;

    List<Document> findForUserList(SearchFilter searchFilter, int userId, int companyId) throws DataAccessException;
}
package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Document;

import java.util.List;

public interface DocumentDao {

    Document get(int id) throws DataAccessException;

    List<Document> getList(int company) throws DataAccessException;

    List<Document> getListByPartId(int partId) throws DataAccessException;

    void create(Document document, int company) throws DataAccessException;

    void update(Document document) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}
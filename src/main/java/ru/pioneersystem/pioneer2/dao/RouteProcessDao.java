package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.dao.exception.LockException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Route;

public interface RouteProcessDao {

    void create(Document document, int routeId) throws DataAccessException, LockException;
}
package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;

public interface RouteProcessDao {

    void create(int documentId, int routeId) throws DataAccessException;

    void start(int documentId) throws DataAccessException;

    void cancel(int documentId, int userId, String message) throws DataAccessException;
}
package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;

public interface RouteProcessDao {

    void create(int documentId, int routeId) throws DataAccessException;

    void start(int documentId, int userId) throws DataAccessException;

    void accept(int documentId, int userId, String message, boolean changeRoute, int newRouteId) throws DataAccessException;

    void reject(int documentId, int userId, String message) throws DataAccessException;

    void cancel(int documentId, int userId, String message) throws DataAccessException;
}
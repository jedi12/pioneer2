package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Route;

import java.util.List;
import java.util.Map;

public interface RouteDao {

    Route get(int routeId) throws DataAccessException;

    List<Route> getList(int companyId) throws DataAccessException;

    Map<String, Integer> getUserRouteMap(int companyId, int userId) throws DataAccessException;

    void create(Route route, int companyId) throws DataAccessException;

    void update(Route route) throws DataAccessException;

    void delete(int routeId) throws DataAccessException;
}
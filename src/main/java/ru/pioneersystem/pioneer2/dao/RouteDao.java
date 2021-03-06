package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Route;

import java.util.List;
import java.util.Map;

public interface RouteDao {

    List<Route> getSuperList() throws DataAccessException;

    List<Route> getAdminList(int companyId) throws DataAccessException;

    Map<String, Integer> getUserRouteMap(int userId, int companyId) throws DataAccessException;

    List<String> getRoutesWithGroup(int groupId, int companyId) throws DataAccessException;

    int getCountRoutesWithRestriction(int groupId, int companyId) throws DataAccessException;

    void removeGroupRestriction(int groupId, int companyId) throws DataAccessException;

    Route get(int routeId, int companyId) throws DataAccessException;

    int create(Route route, int companyId) throws DataAccessException;

    void update(Route route, int companyId) throws DataAccessException;

    void delete(int routeId, int companyId) throws DataAccessException;
}
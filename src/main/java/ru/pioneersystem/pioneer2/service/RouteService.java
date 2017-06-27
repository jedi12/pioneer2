package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface RouteService {
    Route getRoute(int id) throws ServiceException;

    List<Route> getRouteList() throws ServiceException;

    Map<String, Route> getRouteMap() throws ServiceException;

    Map<String, Integer> getUserRouteMap() throws ServiceException;

    void createRoute(Route route) throws ServiceException;

    void updateRoute(Route route) throws ServiceException;

    void deleteRoute(int id) throws ServiceException, RestrictException;
}
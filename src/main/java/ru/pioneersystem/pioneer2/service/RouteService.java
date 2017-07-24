package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface RouteService {

    Route getRoute(int routeId) throws ServiceException;

    List<Route> getRouteList() throws ServiceException;

    Map<String, Route> getRouteMap() throws ServiceException;

    Map<String, Integer> getUserRoutesMap() throws ServiceException;

    void createRoute(Route route) throws ServiceException;

    void updateRoute(Route route) throws ServiceException;

    void deleteRoute(int routeId) throws ServiceException;
}
package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface RouteService {

    List<Route> getRouteList() throws ServiceException;

    Map<String, Route> getRouteMap() throws ServiceException;

    Map<String, Integer> getUserRoutesMap() throws ServiceException;

    List<String> getRoutesWithGroup(int groupId) throws ServiceException;

    int getCountRoutesWithRestriction(int groupId) throws ServiceException;

    Route getNewRoute();

    Route getRoute(int routeId) throws ServiceException;

    void saveRoute(Route route) throws ServiceException;

    void deleteRoute(int routeId) throws ServiceException;
}
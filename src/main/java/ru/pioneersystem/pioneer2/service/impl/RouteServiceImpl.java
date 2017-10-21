package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RouteDao;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.RouteService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("routeService")
public class RouteServiceImpl implements RouteService {
    private EventService eventService;
    private RouteDao routeDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public RouteServiceImpl(EventService eventService, RouteDao routeDao, CurrentUser currentUser,
                            LocaleBean localeBean, MessageSource messageSource) {
        this.eventService = eventService;
        this.routeDao = routeDao;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<Route> getRouteList() throws ServiceException {
        try {
            List<Route> routes;
            if (currentUser.isSuperRole()) {
                routes = routeDao.getSuperList();
            } else {
                routes = routeDao.getAdminList(currentUser.getUser().getCompanyId());
            }

            return routes;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Route> getRouteMap() throws ServiceException {
        Map<String, Route> routes = new LinkedHashMap<>();
        for (Route routeList : getRouteList()) {
            routes.put(routeList.getName(), routeList);
        }

        String noRouteName = messageSource.getMessage("route.zero.name", null, localeBean.getLocale());
        Route zeroRoute = new Route();
        zeroRoute.setId(0);
        zeroRoute.setName(noRouteName);
        routes.put(noRouteName, zeroRoute);

        return routes;
    }

    @Override
    public Map<String, Integer> getUserRoutesMap() throws ServiceException {
        try {
            return routeDao.getUserRouteMap(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.userRouteNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> getRoutesWithGroup(int groupId) throws ServiceException {
        try {
            return routeDao.getRoutesWithGroup(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.routesWithGroup", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), groupId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int getCountRoutesWithRestriction(int groupId) throws ServiceException {
        try {
            return routeDao.getCountRoutesWithRestriction(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.countRoutesWithRestriction", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), groupId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Route getNewRoute() {
        Route route = new Route();
        route.setPoints(new ArrayList<>());
        route.setGroups(new ArrayList<>());
        route.setCreateFlag(true);
        return route;
    }

    @Override
    public Route getRoute(Route selectedRoute) throws ServiceException {
        if (selectedRoute == null) {
            String mess = messageSource.getMessage("error.route.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            int companyId;
            if (currentUser.isSuperRole()) {
                companyId = selectedRoute.getCompanyId();
            } else {
                companyId = currentUser.getUser().getCompanyId();
            }

            Route route = routeDao.get(selectedRoute.getId(), companyId);
            route.setCreateFlag(false);
            eventService.logEvent(Event.Type.ROUTE_GETED, selectedRoute.getId());
            return route;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), selectedRoute.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveRoute(Route route) throws ServiceException {
        try {
            if (route.isCreateFlag()) {
                int routeId = routeDao.create(route, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.ROUTE_CREATED, routeId);
            } else {
                routeDao.update(route, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.ROUTE_CHANGED, route.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), route.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteRoute(Route route) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного маршрута плюс еще какая-нибудь проверка
        try {
            routeDao.delete(route.getId(), currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.ROUTE_DELETED, route.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), route.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createExampleRoute(int coordGroupId, int execGroupId, int companyId) throws ServiceException {
        try {
            Route.Point coordPoint = new Route.Point();
            coordPoint.setStage(1);
            coordPoint.setGroupId(coordGroupId);

            Route.Point execPoint = new Route.Point();
            execPoint.setStage(2);
            execPoint.setGroupId(execGroupId);

            Route route = getNewRoute();
            route.setName(messageSource.getMessage("route.example.name", null, localeBean.getLocale()));
            route.getPoints().add(coordPoint);
            route.getPoints().add(execPoint);

            return routeDao.create(route, companyId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.exampleNotCreated", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }
}

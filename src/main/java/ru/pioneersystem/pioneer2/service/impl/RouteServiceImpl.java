package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.RouteDao;
import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.service.RouteService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service("routeService")
public class RouteServiceImpl implements RouteService {
    private Logger log = LoggerFactory.getLogger(RouteServiceImpl.class);

    private RouteDao routeDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public RouteServiceImpl(RouteDao routeDao, CurrentUser currentUser, LocaleBean localeBean,
                            MessageSource messageSource) {
        this.routeDao = routeDao;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<Route> getRouteList() throws ServiceException {
        try {
            return routeDao.getList(currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.NotLoadedList", null, localeBean.getLocale());
            log.error(mess, e);
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
            return routeDao.getUserRouteMap(currentUser.getUser().getCompanyId(), currentUser.getUser().getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.userRouteNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> getRoutesWithGroup(int groupId) throws ServiceException {
        try {
            return routeDao.getRoutesWithGroup(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.routesWithGroup", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int getCountRoutesWithRestriction(int groupId) throws ServiceException {
        try {
            return routeDao.getCountRoutesWithRestriction(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.countRoutesWithRestriction", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Route getNewRoute() {
        Route route = new Route();
        route.setPoints(new LinkedList<>());
        route.setGroups(new LinkedList<>());
        route.setCreateFlag(true);
        return route;
    }

    @Override
    public Route getRoute(int routeId) throws ServiceException {
        try {
            Route route = routeDao.get(routeId, currentUser.getUser().getCompanyId());
            route.setCreateFlag(false);
            return route;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveRoute(Route route) throws ServiceException {
        try {
            if (route.isCreateFlag()) {
                routeDao.create(route, currentUser.getUser().getCompanyId());
            } else {
                routeDao.update(route, currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.NotSaved", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteRoute(int routeId) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного маршрута плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            routeDao.delete(routeId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.route.NotDeleted", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }
}

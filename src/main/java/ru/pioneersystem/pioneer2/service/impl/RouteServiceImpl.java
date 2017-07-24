package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.RouteDao;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.service.RouteService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("routeService")
public class RouteServiceImpl implements RouteService {
    private Logger log = LoggerFactory.getLogger(RouteServiceImpl.class);

    private RouteDao routeDao;
    private CurrentUser currentUser;

    @Autowired
    public RouteServiceImpl(RouteDao routeDao, CurrentUser currentUser) {
        this.routeDao = routeDao;
        this.currentUser = currentUser;
    }

    @Override
    public Route getRoute(int routeId) throws ServiceException {
        try {
            return routeDao.get(routeId);
        } catch (DataAccessException e) {
            log.error("Can't get Route by id", e);
            throw new ServiceException("Can't get Route by id", e);
        }
    }

    @Override
    public List<Route> getRouteList() throws ServiceException {
        try {
            return routeDao.getList(currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't get list of Route", e);
            throw new ServiceException("Can't get list of Route", e);
        }
    }

    @Override
    public Map<String, Route> getRouteMap() throws ServiceException {
        Map<String, Route> routes = new LinkedHashMap<>();
        for (Route routeList : getRouteList()) {
            routes.put(routeList.getName(), routeList);
        }
        return routes;
    }

    @Override
    public Map<String, Integer> getUserRoutesMap() throws ServiceException {
        try {
            return routeDao.getUserRouteMap(currentUser.getUser().getCompanyId(), currentUser.getUser().getId());
        } catch (DataAccessException e) {
            log.error("Can't get user Route map", e);
            throw new ServiceException("Can't get user Route map", e);
        }
    }

    @Override
    public void createRoute(Route route) throws ServiceException {
        try {
            routeDao.create(route, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't create Route", e);
            throw new ServiceException("Can't create Route", e);
        }
    }

    @Override
    public void updateRoute(Route route) throws ServiceException {
        try {
            routeDao.update(route);
        } catch (DataAccessException e) {
            log.error("Can't update Route", e);
            throw new ServiceException("Can't update Route", e);
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
            routeDao.delete(routeId);
        } catch (DataAccessException e) {
            log.error("Can't delete Route", e);
            throw new ServiceException("Can't delete Route", e);
        }
    }
}

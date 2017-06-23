package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Route;

import java.util.List;

public interface RouteDao {

    Route get(int id) throws DataAccessException;

    List<Route> getList(int company) throws DataAccessException;

    void create(Route route, int company) throws DataAccessException;

    void update(Route route) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}
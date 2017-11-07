package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Menu;

import java.util.List;

public interface MenuDao {

    List<Menu> getList(int companyId) throws DataAccessException;

    List<Menu> getUserMenu(int userId) throws DataAccessException;

    Menu get(int menuId, int companyId) throws DataAccessException;

    int create(Menu menu, int companyId) throws DataAccessException;

    void update(Menu menu, int companyId) throws DataAccessException;

    void delete(int menuId, int companyId) throws DataAccessException;
}
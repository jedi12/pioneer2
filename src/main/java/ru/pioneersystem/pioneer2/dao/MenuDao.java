package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Menu;

import java.util.List;

public interface MenuDao {

    Menu get(int menuId) throws DataAccessException;

    List<Menu> getList(int companyId) throws DataAccessException;

    List<Menu> getUserMenu(int userId) throws DataAccessException;

    void create(Menu menu, int companyId) throws DataAccessException;

    void update(Menu menu) throws DataAccessException;

    void delete(int menuId) throws DataAccessException;
}
package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface MenuService {
    Menu getMenu(int id) throws ServiceException;

    List<Menu> getMenuList() throws ServiceException;

    List<Menu> getUserMenu(int userId) throws ServiceException;

    void createMenu(Menu menu) throws ServiceException;

    void updateMenu(Menu menu) throws ServiceException;

    void deleteMenu(int id) throws ServiceException;
}
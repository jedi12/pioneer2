package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface MenuService {

    Menu getMenu(int menuId) throws ServiceException;

    List<Menu> getMenuList() throws ServiceException;

    List<Menu> getUserMenu(int userId) throws ServiceException;

    int getMenuIndex(int menuId, List<Menu> menus) throws ServiceException;

    void createMenu(Menu menu) throws ServiceException;

    void updateMenu(Menu menu) throws ServiceException;

    void deleteMenu(int menuId) throws ServiceException;
}
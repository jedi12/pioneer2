package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface MenuService {

    List<Menu> getMenuList() throws ServiceException;

    List<Menu> getUserMenu() throws ServiceException;

    int getMenuIndex(int menuId, List<Menu> menus) throws ServiceException;

    Menu getNewMenu();

    Menu getMenu(int menuId) throws ServiceException;

    void saveMenu(Menu menu) throws ServiceException;

    void deleteMenu(int menuId) throws ServiceException;
}
package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Locale;

public interface MenuService {
    Menu getMenu(int id, Locale locale) throws ServiceException;

    List<Menu> getMenuList(int companyId, Locale locale) throws ServiceException;

    List<Menu> getUserMenu(int userId, Locale locale) throws ServiceException;

    void createMenu(Menu menu, int companyId) throws ServiceException;

    void updateMenu(Menu menu) throws ServiceException;

    void deleteMenu(int id) throws ServiceException;
}
package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.MenuDao;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.MenuService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.List;

@Service("menuService")
public class MenuServiceImpl implements MenuService {
    private EventService eventService;
    private MenuDao menuDao;
    private DictionaryService dictionaryService;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private CurrentUser currentUser;

    @Autowired
    public MenuServiceImpl(EventService eventService, MenuDao menuDao, DictionaryService dictionaryService,
                           LocaleBean localeBean, CurrentUser currentUser, MessageSource messageSource) {
        this.eventService = eventService;
        this.menuDao = menuDao;
        this.dictionaryService = dictionaryService;
        this.localeBean = localeBean;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Override
    public List<Menu> getMenuList() throws ServiceException {
        try {
            List<Menu> menus = menuDao.getList(currentUser.getUser().getCompanyId());
            for (Menu menu : menus) {
                String menuName = dictionaryService.getLocalizedMenuName(menu.getId(), localeBean.getLocale());
                if (menuName != null) {
                    menu.setName(menuName);
                }
            }
            return menus;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.menu.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Menu> getUserMenu() throws ServiceException {
        try {
            List<Menu> menus = menuDao.getUserMenu(currentUser.getUser().getId());
            for (Menu menu : menus) {
                String menuName = dictionaryService.getLocalizedMenuName(menu.getId(), localeBean.getLocale());
                if (menuName != null) {
                    menu.setName(menuName);
                }
            }
            return menus;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.menu.userMenuNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int getMenuIndex(int menuId, List<Menu> menus) {
        int menuIndex = -1;
        for (Menu menu: menus) {
            menuIndex = menuIndex + 1;
            if (menu.getId() == menuId) {
                break;
            }
        }
        return menuIndex;
    }

    @Override
    public Menu getNewMenu() {
        Menu menu = new Menu();
        menu.setCreateFlag(true);
        return menu;
    }

    @Override
    public Menu getMenu(int menuId) throws ServiceException {
        try {
            Menu menu = menuDao.get(menuId, currentUser.getUser().getCompanyId());
            String menuName = dictionaryService.getLocalizedMenuName(menu.getId(), localeBean.getLocale());
            if (menuName != null) {
                menu.setName(menuName);
            }
            menu.setCreateFlag(false);
            eventService.logEvent(Event.Type.MENU_GETED, menuId);
            return menu;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.menu.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), menuId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveMenu(Menu menu) throws ServiceException {
        try {
            if (menu.isCreateFlag()) {
                int menuId = menuDao.create(menu, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.MENU_CREATED, menuId);
            } else {
                menuDao.update(menu, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.MENU_CHANGED, menu.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.menu.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), menu.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteMenu(int menuId) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного меню плюс еще какая-нибудь проверка
        try {
            menuDao.delete(menuId, currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.MENU_DELETED, menuId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.menu.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), menuId);
            throw new ServiceException(mess, e);
        }
    }
}

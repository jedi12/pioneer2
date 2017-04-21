package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.MenuDao;
import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.service.MenuService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.List;

@Service("menuService")
public class MenuServiceImpl implements MenuService {
    private Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

    private MenuDao menuDao;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private CurrentUser currentUser;

    @Autowired
    public MenuServiceImpl(MenuDao menuDao, LocaleBean localeBean, CurrentUser currentUser, MessageSource messageSource) {
        this.menuDao = menuDao;
        this.localeBean = localeBean;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Override
    public Menu getMenu(int id) throws ServiceException {
        try {
            return setLocalizedMenuName(menuDao.get(id));
        } catch (DataAccessException e) {
            log.error("Can't get Menu by id", e);
            throw new ServiceException("Can't get Menu by id", e);
        }
    }

    @Override
    public List<Menu> getMenuList() throws ServiceException {
        try {
            List<Menu> menus = menuDao.getList(currentUser.getUser().getCompanyId());
            for (Menu menu : menus) {
                setLocalizedMenuName(menu);
            }
            return menus;
        } catch (DataAccessException e) {
            log.error("Can't get list of Menu", e);
            throw new ServiceException("Can't get list of Menu", e);
        }
    }

    @Override
    public List<Menu> getUserMenu(int userId) throws ServiceException {
        try {
            List<Menu> menus = menuDao.getUserMenu(userId);
            for (Menu menu : menus) {
                setLocalizedMenuName(menu);
            }
            return menus;
        } catch (DataAccessException e) {
            log.error("Can't get list of Menu", e);
            throw new ServiceException("Can't get list of Menu", e);
        }
    }

    @Override
    public void createMenu(Menu menu) throws ServiceException {
        try {
            menuDao.create(menu, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't create Menu", e);
            throw new ServiceException("Can't create Menu", e);
        }
    }

    @Override
    public void updateMenu(Menu menu) throws ServiceException {
        try {
            menuDao.update(menu);
        } catch (DataAccessException e) {
            log.error("Can't update Menu", e);
            throw new ServiceException("Can't update Menu", e);
        }
    }

    @Override
    public void deleteMenu(int id) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного меню плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            menuDao.delete(id);
        } catch (DataAccessException e) {
            log.error("Can't delete Menu", e);
            throw new ServiceException("Can't delete Menu", e);
        }
    }

    private Menu setLocalizedMenuName(Menu menu) {
        if (menu.getState() == Menu.State.SYSTEM) {
            switch (menu.getId()) {
                case Menu.Id.PUB_DOCS:
                    menu.setName(messageSource.getMessage("menu.name.pubDocs", null, localeBean.getLocale()));
                    break;
                case Menu.Id.CREATE_DOCS:
                    menu.setName(messageSource.getMessage("menu.name.createDocs", null, localeBean.getLocale()));
                    break;
                case Menu.Id.SEARCH_DOCS:
                    menu.setName(messageSource.getMessage("menu.name.searchDocs", null, localeBean.getLocale()));
                    break;
                case Menu.Id.SETTINGS:
                    menu.setName(messageSource.getMessage("menu.name.settings", null, localeBean.getLocale()));
                    break;
                case Menu.Id.JOURNALS:
                    menu.setName(messageSource.getMessage("menu.name.journals", null, localeBean.getLocale()));
                    break;
                case Menu.Id.USERS:
                    menu.setName(messageSource.getMessage("menu.name.users", null, localeBean.getLocale()));
                    break;
                case Menu.Id.ROLES:
                    menu.setName(messageSource.getMessage("menu.name.roles", null, localeBean.getLocale()));
                    break;
                case Menu.Id.MY_DOCS:
                    menu.setName(messageSource.getMessage("menu.name.myDocs", null, localeBean.getLocale()));
                    break;
                case Menu.Id.GROUPS:
                    menu.setName(messageSource.getMessage("menu.name.groups", null, localeBean.getLocale()));
                    break;
                case Menu.Id.ROUTES:
                    menu.setName(messageSource.getMessage("menu.name.routes", null, localeBean.getLocale()));
                    break;
                case Menu.Id.PARTS:
                    menu.setName(messageSource.getMessage("menu.name.parts", null, localeBean.getLocale()));
                    break;
                case Menu.Id.LISTS:
                    menu.setName(messageSource.getMessage("menu.name.lists", null, localeBean.getLocale()));
                    break;
                case Menu.Id.TEMPLATES:
                    menu.setName(messageSource.getMessage("menu.name.templates", null, localeBean.getLocale()));
                    break;
                case Menu.Id.EVENTS:
                    menu.setName(messageSource.getMessage("menu.name.events", null, localeBean.getLocale()));
                    break;
                case Menu.Id.NOTICES:
                    menu.setName(messageSource.getMessage("menu.name.notices", null, localeBean.getLocale()));
                    break;
                case Menu.Id.MENUS:
                    menu.setName(messageSource.getMessage("menu.name.menus", null, localeBean.getLocale()));
                    break;
                case Menu.Id.ON_ROUTE_CONFIRM:
                    menu.setName(messageSource.getMessage("menu.name.onRouteConfirm", null, localeBean.getLocale()));
                    break;
                case Menu.Id.ON_ROUTE_EXEC:
                    menu.setName(messageSource.getMessage("menu.name.onRouteExec", null, localeBean.getLocale()));
                    break;
                case Menu.Id.COMPANY:
                    menu.setName(messageSource.getMessage("menu.name.company", null, localeBean.getLocale()));
                    break;
                default:
                    menu.setName("Unknown");
            }
        }
        return menu;
    }
}

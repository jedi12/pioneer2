package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.CompanyDao;
import ru.pioneersystem.pioneer2.dao.GroupDao;
import ru.pioneersystem.pioneer2.dao.UserDao;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.service.CompanyService;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.SessionListener;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.ArrayList;
import java.util.List;

@Service("companyService")
public class CompanyServiceImpl implements CompanyService {
    private EventService eventService;
    private CompanyDao companyDao;
    private UserDao userDao;
    private GroupDao groupDao;
    private DictionaryService dictionaryService;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public CompanyServiceImpl(EventService eventService, CompanyDao companyDao, UserDao userDao, GroupDao groupDao,
                              DictionaryService dictionaryService, LocaleBean localeBean, MessageSource messageSource,
                              SessionListener sessionListener) {
        this.eventService = eventService;
        this.companyDao = companyDao;
        this.userDao = userDao;
        this.groupDao = groupDao;
        this.dictionaryService = dictionaryService;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
        this.sessionListener = sessionListener;
    }

    @Override
    public List<Company> getCompanyList() throws ServiceException {
        try {
            List<Company> companies = companyDao.getList();
            for (Company company : companies) {
                String stateName = dictionaryService.getLocalizedStateName(company.getState(), localeBean.getLocale());
                if (stateName != null) {
                    company.setStateName(stateName);
                }
            }
            return companies;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Company getNewCompany() {
        Company company = new Company();
        company.setCreateFlag(true);
        return company;
    }

    @Override
    public Company getCompany(Company selectedCompany) throws ServiceException {
        if (selectedCompany == null) {
            String mess = messageSource.getMessage("error.company.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            Company company = companyDao.get(selectedCompany.getId());
            String stateName = dictionaryService.getLocalizedStateName(company.getState(), localeBean.getLocale());
            if (stateName != null) {
                company.setStateName(stateName);
            }
            company.setCreateFlag(false);
            eventService.logEvent(Event.Type.COMPANY_GETED, selectedCompany.getId());
            return company;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), selectedCompany.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCompany(Company company) throws ServiceException {
        try {
            if (company.isCreateFlag()) {
                int companyId = companyDao.create(company);

                User adminUser = new User();
                adminUser.setName(company.getAdminName());
                adminUser.setLogin(company.getAdminLogin().trim());
                adminUser.setEmail(company.getAdminEmail().trim());
                adminUser.setPhone(company.getAdminPhone());
                adminUser.setLinkGroups(new ArrayList<>());
                int userId = userDao.create(adminUser, companyId);

                Group.LinkUser linkUser = new Group.LinkUser();
                linkUser.setUserId(userId);
                linkUser.setParticipant(true);

                List<Group.LinkUser> linkUsers = new ArrayList<>();
                linkUsers.add(linkUser);

                Group adminGroup = new Group();
                adminGroup.setName(company.getAdminGroupName());
                adminGroup.setRoleId(Role.Id.ADMIN);
                adminGroup.setLinkUsers(linkUsers);
                int groupId = groupDao.create(adminGroup, companyId);

                eventService.logEvent(Event.Type.COMPANY_CREATED, companyId);
                eventService.logEvent(Event.Type.USER_CREATED, userId);
                eventService.logEvent(Event.Type.GROUP_CREATED, groupId);
            } else {
                companyDao.update(company);
                eventService.logEvent(Event.Type.COMPANY_CHANGED, company.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), company.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void lockCompany(Company company) throws ServiceException {
        if (company == null) {
            String mess = messageSource.getMessage("error.company.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        if (company.getState() == Company.State.SYSTEM) {
            String mess = messageSource.getMessage("error.operation.NotAllowed", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            companyDao.lock(company.getId());
            eventService.logEvent(Event.Type.COMPANY_LOCKED, company.getId());
            sessionListener.invalidateCompanySessions(company.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotLocked", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), company.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void unlockCompany(Company company) throws ServiceException {
        if (company == null) {
            String mess = messageSource.getMessage("error.company.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        if (company.getState() == Company.State.SYSTEM) {
            String mess = messageSource.getMessage("error.operation.NotAllowed", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            companyDao.unlock(company.getId());
            eventService.logEvent(Event.Type.COMPANY_UNLOCKED, company.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotUnLocked", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), company.getId());
            throw new ServiceException(mess, e);
        }
    }
}

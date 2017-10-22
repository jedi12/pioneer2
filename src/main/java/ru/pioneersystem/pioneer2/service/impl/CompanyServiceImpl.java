package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.CompanyDao;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.List;

@Service("companyService")
public class CompanyServiceImpl implements CompanyService {
    private EventService eventService;
    private CompanyDao companyDao;
    private UserService userService;
    private GroupService groupService;
    private RoleService roleService;
    private RouteService routeService;
    private ChoiceListService choiceListService;
    private PartService partService;
    private TemplateService templateService;
    private DictionaryService dictionaryService;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public CompanyServiceImpl(EventService eventService, CompanyDao companyDao, UserService userService,
                              GroupService groupService, RoleService roleService, RouteService routeService,
                              ChoiceListService choiceListService, PartService partService,
                              TemplateService templateService,DictionaryService dictionaryService,
                              LocaleBean localeBean, MessageSource messageSource, SessionListener sessionListener) {
        this.eventService = eventService;
        this.companyDao = companyDao;
        this.userService = userService;
        this.groupService = groupService;
        this.roleService = roleService;
        this.routeService = routeService;
        this.choiceListService = choiceListService;
        this.partService = partService;
        this.templateService = templateService;
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
        company.setUser(userService.getNewUser());
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
                int adminUserId = userService.createAdminUser(company.getUser(), companyId);
                int adminGroupId = groupService.createGroupWithUser(company.getGroupName(), Role.Id.ADMIN, adminUserId, companyId);
                int exampleCoordinatorRoleId = roleService.createExampleRole(Role.Example.COORDINATOR, companyId);
                int exampleExecutorRoleId = roleService.createExampleRole(Role.Example.EXECUTOR, companyId);
                int exampleCoordinatorGroupId = groupService.createGroupWithUser(
                        messageSource.getMessage("group.coord.name", null, localeBean.getLocale()), exampleCoordinatorRoleId, 0, companyId);
                int exampleExecutorGroupId = groupService.createGroupWithUser(
                        messageSource.getMessage("group.exec.name", null, localeBean.getLocale()), exampleExecutorRoleId, 0, companyId);
                int exampleRouteId = routeService.createExampleRoute(exampleCoordinatorGroupId, exampleExecutorGroupId, companyId);
                int exampleChoiceListId = choiceListService.createExampleChoiceList(companyId);
                int exampleTemplatePartId = partService.createExamplePart(Part.Type.FOR_TEMPLATES, adminGroupId, companyId);
                int exampleDocumentPartId = partService.createExamplePart(Part.Type.FOR_DOCUMENTS, adminGroupId, companyId);
                int exampleTemplateId = templateService.createExampleTemplate(exampleRouteId, exampleTemplatePartId, exampleChoiceListId, companyId);

                eventService.logEvent(Event.Type.COMPANY_CREATED, companyId);
            } else {
                companyDao.update(company);
                eventService.logEvent(Event.Type.COMPANY_CHANGED, company.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), company.getId());
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.company.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null);
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

package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.CompanyDao;
import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.service.CompanyService;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.SessionListener;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.List;

@Service("companyService")
public class CompanyServiceImpl implements CompanyService {
    private EventService eventService;
    private CompanyDao companyDao;
    private DictionaryService dictionaryService;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public CompanyServiceImpl(EventService eventService, CompanyDao companyDao, DictionaryService dictionaryService,
                              LocaleBean localeBean, MessageSource messageSource, SessionListener sessionListener) {
        this.eventService = eventService;
        this.companyDao = companyDao;
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
    public Company getCompany(int companyId) throws ServiceException {
        try {
            Company company = companyDao.get(companyId);
            String stateName = dictionaryService.getLocalizedStateName(company.getState(), localeBean.getLocale());
            if (stateName != null) {
                company.setStateName(stateName);
            }
            company.setCreateFlag(false);
            eventService.logEvent(Event.Type.COMPANY_GETED, companyId);
            return company;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), companyId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveCompany(Company company) throws ServiceException {
        try {
            if (company.isCreateFlag()) {
                int companyId = companyDao.create(company);
                eventService.logEvent(Event.Type.COMPANY_CREATED, companyId);
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
    public void lockCompany(int companyId) throws ServiceException {
        try {
            companyDao.lock(companyId);
            eventService.logEvent(Event.Type.COMPANY_LOCKED, companyId);
            sessionListener.invalidateCompanySessions(companyId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotLocked", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), companyId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void unlockCompany(int companyId) throws ServiceException {
        try {
            companyDao.unlock(companyId);
            eventService.logEvent(Event.Type.COMPANY_UNLOCKED, companyId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotUnLocked", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), companyId);
            throw new ServiceException(mess, e);
        }
    }
}

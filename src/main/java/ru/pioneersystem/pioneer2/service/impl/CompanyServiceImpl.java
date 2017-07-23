package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.CompanyDao;
import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.service.CompanyService;
import ru.pioneersystem.pioneer2.service.SessionListener;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.List;

@Service("companyService")
public class CompanyServiceImpl implements CompanyService {
    private Logger log = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private CompanyDao companyDao;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public CompanyServiceImpl(CompanyDao companyDao, LocaleBean localeBean, MessageSource messageSource,
                              SessionListener sessionListener) {
        this.companyDao = companyDao;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
        this.sessionListener = sessionListener;
    }

    @Override
    public List<Company> getCompanyList() throws ServiceException {
        try {
            List<Company> companies = companyDao.getList();
            for (Company company : companies) {
                setLocalizedStateName(company);
            }
            return companies;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotLoadedList", null, localeBean.getLocale());
            log.error(mess, e);
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
    public Company getCompany(int id) throws ServiceException {
        try {
            Company company = setLocalizedStateName(companyDao.get(id));
            company.setCreateFlag(false);
            return company;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveCompany(Company company) throws ServiceException {
        try {
            if (company.isCreateFlag()) {
                companyDao.create(company);
            } else {
                companyDao.update(company);
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotSaved", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void lockCompany(int id) throws ServiceException {
        try {
            companyDao.lock(id);
            sessionListener.invalidateCompanySessions(id);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotLocked", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void unlockCompany(int id) throws ServiceException {
        try {
            companyDao.unlock(id);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.company.NotUnLocked", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    private Company setLocalizedStateName(Company company) {
        switch (company.getState()) {
            case Company.State.LOCKED:
                company.setStateName(messageSource.getMessage("status.locked", null, localeBean.getLocale()));
                break;
            case Company.State.ACTIVE:
                company.setStateName(messageSource.getMessage("status.active", null, localeBean.getLocale()));
                break;
            default:
                company.setStateName("Unknown");
        }
        return company;
    }
}

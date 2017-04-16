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

import java.util.List;
import java.util.Locale;

@Service("companyService")
public class CompanyServiceImpl implements CompanyService {
    private Logger log = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private CompanyDao companyDao;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public CompanyServiceImpl(CompanyDao companyDao, MessageSource messageSource, SessionListener sessionListener) {
        this.companyDao = companyDao;
        this.messageSource = messageSource;
        this.sessionListener = sessionListener;
    }

    @Override
    public Company getCompany(int id, Locale locale) throws ServiceException {
        try {
            return setCompanyStateName(companyDao.get(id), locale);
        } catch (DataAccessException e) {
            log.error("Can't get Company by id", e);
            throw new ServiceException("Can't get Company by id", e);
        }
    }

    @Override
    public List<Company> getCompanyList(Locale locale) throws ServiceException {
        try {
            List<Company> companies = companyDao.getList();
            for (Company company : companies) {
                setCompanyStateName(company, locale);
            }
            return companies;
        } catch (DataAccessException e) {
            log.error("Can't get list of Company", e);
            throw new ServiceException("Can't get list of Company", e);
        }
    }

    @Override
    public void createCompany(Company company) throws ServiceException {
        try {
            companyDao.create(company);
        } catch (DataAccessException e) {
            log.error("Can't create Company", e);
            throw new ServiceException("Can't create Company", e);
        }
    }

    @Override
    public void updateCompany(Company company) throws ServiceException {
        try {
            companyDao.update(company);
        } catch (DataAccessException e) {
            log.error("Can't update Company", e);
            throw new ServiceException("Can't update Company", e);
        }
    }

    @Override
    public void lockCompany(int id) throws ServiceException {
        try {
            companyDao.lock(id);
            sessionListener.invalidateCompanySessions(id);
        } catch (DataAccessException e) {
            log.error("Can't lock Company", e);
            throw new ServiceException("Can't lock Company", e);
        }
    }

    @Override
    public void unlockCompany(int id) throws ServiceException {
        try {
            companyDao.unlock(id);
        } catch (DataAccessException e) {
            log.error("Can't unlock Company", e);
            throw new ServiceException("Can't unlock Company", e);
        }
    }

    private Company setCompanyStateName(Company company, Locale locale) {
        switch (company.getState()) {
            case Company.State.LOCKED:
                company.setStateName(messageSource.getMessage("status.locked", null, locale));
                break;
            case Company.State.ACTIVE:
                company.setStateName(messageSource.getMessage("status.active", null, locale));
                break;
            default:
                company.setStateName("Unknown");
        }
        return company;
    }
}

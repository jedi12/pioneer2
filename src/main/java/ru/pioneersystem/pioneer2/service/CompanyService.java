package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Locale;

public interface CompanyService {
    Company getCompany(int id, Locale locale) throws ServiceException;

    List<Company> getCompanyList(Locale locale) throws ServiceException;

    void createCompany(Company company) throws ServiceException;

    void updateCompany(Company company) throws ServiceException;

    void lockCompany(int id) throws ServiceException;

    void unlockCompany(int id) throws ServiceException;
}
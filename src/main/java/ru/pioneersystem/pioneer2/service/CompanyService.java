package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface CompanyService {

    List<Company> getCompanyList() throws ServiceException;

    Company getNewCompany();

    Company getCompany(Company selectedCompany) throws ServiceException;

    void saveCompany(Company company) throws ServiceException;

    void lockCompany(Company company) throws ServiceException;

    void unlockCompany(Company company) throws ServiceException;
}
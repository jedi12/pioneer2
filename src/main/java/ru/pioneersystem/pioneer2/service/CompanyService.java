package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface CompanyService {

    List<Company> getCompanyList() throws ServiceException;

    Company getNewCompany();

    Company getCompany(int id) throws ServiceException;

    void saveCompany(Company company) throws ServiceException;

    void lockCompany(int id) throws ServiceException;

    void unlockCompany(int id) throws ServiceException;
}
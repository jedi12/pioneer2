package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Company;

import java.util.List;

public interface CompanyDao {

    Company get(int companyId) throws DataAccessException;

    List<Company> getList() throws DataAccessException;

    void create(Company company) throws DataAccessException;

    void update(Company company) throws DataAccessException;

    void lock(int companyId) throws DataAccessException;

    void unlock(int companyId) throws DataAccessException;

    int getMaxUserCount(int companyId) throws DataAccessException;
}
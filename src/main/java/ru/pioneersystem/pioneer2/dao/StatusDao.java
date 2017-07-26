package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Status;

import java.util.List;

public interface StatusDao {

    Status get(int statusId, int companyId) throws DataAccessException;

    List<Status> getList(int companyId) throws DataAccessException;
}
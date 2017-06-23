package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Status;

import java.util.List;

public interface StatusDao {

    Status get(int id) throws DataAccessException;

    List<Status> getList(int company) throws DataAccessException;
}
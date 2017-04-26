package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Group;

import java.util.List;

public interface GroupDao {

    Group get(int id) throws DataAccessException;

    List<Group> getList(int company) throws DataAccessException;

    void create(Group group, int company) throws DataAccessException;

    void update(Group group) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}
package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Group;

import java.util.List;
import java.util.Map;

public interface GroupDao {

    List<Group> getList(int companyId) throws DataAccessException;

    Map<String, Group> getRouteGroup(int companyId) throws DataAccessException;

    Map<String, Integer> getUserPublishGroup(int companyId, int userId) throws DataAccessException;

    Map<String, Integer> getUserCreateGroup(int companyId, int userId) throws DataAccessException;

    Group get(int groupId, int companyId) throws DataAccessException;

    void create(Group group, int companyId) throws DataAccessException;

    void update(Group group, int companyId) throws DataAccessException;

    void delete(int groupId, int companyId) throws DataAccessException;
}
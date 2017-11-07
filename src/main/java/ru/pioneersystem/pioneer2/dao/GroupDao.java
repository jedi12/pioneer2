package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Group;

import java.util.List;
import java.util.Map;

public interface GroupDao {

    List<Group> getSuperList() throws DataAccessException;

    List<Group> getAdminList(int companyId) throws DataAccessException;

    Map<String, Group> getRouteGroup(int companyId) throws DataAccessException;

    Map<String, Integer> getUserPublishGroup(int userId, int companyId) throws DataAccessException;

    Map<String, Integer> getCreateGroup(int companyId) throws DataAccessException;

    Map<String, Integer> getUserCreateGroup(int userId, int companyId) throws DataAccessException;

    Map<Integer, Map<Integer, Integer>> getUserRolesGroupActivity(int userId, int companyId) throws DataAccessException;

    List<String> groupsWithRole(int roleId, int companyId) throws DataAccessException;

    Group get(int groupId, int companyId) throws DataAccessException;

    int create(Group group, int companyId) throws DataAccessException;

    void update(Group group, int companyId) throws DataAccessException;

    void delete(int groupId, int companyId) throws DataAccessException;
}
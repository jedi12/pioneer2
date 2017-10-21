package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface GroupService {

    List<Group> getGroupList() throws ServiceException;

    Map<String, Integer> getGroupMap() throws ServiceException;

    Map<String, Group> getPointMap() throws ServiceException;

    Map<String, Integer> getUserPublishMap() throws ServiceException;

    Map<String, Integer> getUserCreateGroupsMap() throws ServiceException;

    Map<String, Integer> getForSearchGroupMap() throws ServiceException;

    Map<Integer, Map<Integer, Integer>> getUserRolesGroupActivityMap() throws ServiceException;

    List<String> groupsWithRole(int roleId) throws ServiceException;

    Group getNewGroup();

    Group getGroup(Group selectedGroup) throws ServiceException;

    void saveGroup(Group group) throws ServiceException;

    void deleteGroup(Group group) throws ServiceException;

    int createAdminGroup(Group group, int userId, int companyId) throws ServiceException;

    int createExampleGroup(int groupExample, int roleId, int companyId) throws ServiceException;
}
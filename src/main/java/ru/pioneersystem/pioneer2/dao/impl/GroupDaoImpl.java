package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.GroupDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.model.Role;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository(value = "groupDao")
public class GroupDaoImpl implements GroupDao {
    private static final String INSERT_GROUP =
            "INSERT INTO DOC.GROUPS (NAME, STATE, ROLE_ID, COMPANY) VALUES (?, ?, ?, ?)";
    private static final String INSERT_GROUP_USER =
            "INSERT INTO DOC.GROUPS_USER (ID, USER_ID, ACTOR_TYPE) VALUES (?, ?, ?)";
    private static final String UPDATE_GROUP =
            "UPDATE DOC.GROUPS SET NAME = ?, ROLE_ID = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_GROUP =
            "UPDATE DOC.GROUPS SET STATE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_GROUP_USER =
            "DELETE FROM DOC.GROUPS_USER WHERE ID = ?";
    private static final String SELECT_GROUP =
            "SELECT ID, NAME, STATE, ROLE_ID FROM DOC.GROUPS WHERE ID = ? AND COMPANY = ?";
    private static final String SELECT_GROUP_USER =
            "SELECT USER_ID, ACTOR_TYPE, NAME FROM DOC.GROUPS_USER GU " +
                    "LEFT JOIN DOC.USERS U ON GU.USER_ID = U.ID WHERE GU.ID = ? ORDER BY NAME ASC";
    private static final String SELECT_SUPER_GROUP_LIST =
            "SELECT G.ID AS ID, G.NAME AS NAME, G.STATE AS STATE, R.NAME AS ROLE_NAME, G.COMPANY AS COMPANY, " +
                    "C.NAME AS COMPANY_NAME FROM DOC.GROUPS G LEFT JOIN DOC.ROLES R ON R.ID = G.ROLE_ID " +
                    "LEFT JOIN DOC.COMPANY C ON C.ID = G.COMPANY WHERE G.STATE > 0 AND G.COMPANY >= 0 " +
                    "ORDER BY G.COMPANY ASC, NAME ASC";
    private static final String SELECT_ADMIN_GROUP_LIST =
            "SELECT G.ID AS ID, G.NAME AS NAME, G.STATE AS STATE, R.NAME AS ROLE_NAME, G.COMPANY AS COMPANY, " +
                    "NULL AS COMPANY_NAME FROM DOC.GROUPS G LEFT JOIN DOC.ROLES R ON R.ID = G.ROLE_ID " +
                    "WHERE G.STATE > 0 AND G.COMPANY = ? ORDER BY STATE DESC, NAME ASC";
    private static final String SELECT_POINT_MAP =
            "SELECT G.ID AS GROUP_ID, G.NAME AS GROUP_NAME, R.ID AS ROLE_ID, R.NAME AS ROLE_NAME FROM DOC.GROUPS G " +
                    "LEFT JOIN DOC.ROLES R ON G.ROLE_ID = R.ID WHERE TYPE = 10 AND G.STATE > 0 AND G.COMPANY = ? " +
                    "ORDER BY GROUP_NAME ASC";
    private static final String SELECT_PUB_GROUP_MAP =
            "SELECT G.ID AS G_ID, G.NAME AS G_NAME FROM DOC.GROUPS G, DOC.GROUPS_USER GU, DOC.ROLES R " +
                    "WHERE G.ID = GU.ID AND G.ROLE_ID = R.ID AND TYPE IN (?, ?) AND G.STATE > 0 AND G.COMPANY = ? " +
                    "AND USER_ID = ? ORDER BY G.NAME ASC";
    private static final String SELECT_CREATE_GROUP_MAP =
            "SELECT ID, NAME FROM DOC.GROUPS G WHERE STATE > 0 AND ROLE_ID = ? AND COMPANY = ? ORDER BY NAME ASC";
    private static final String SELECT_USER_CREATE_GROUP_MAP =
            "SELECT G.ID AS ID, NAME FROM DOC.GROUPS G, DOC.GROUPS_USER GU " +
                    "WHERE G.ID = GU.ID AND STATE > 0 AND ROLE_ID = ? AND USER_ID = ? AND COMPANY = ? ORDER BY NAME ASC";
    private static final String SELECT_USER_GROUP_ACTIVITY_MAP =
            "SELECT ROLE_ID, G.ID, ACTOR_TYPE FROM DOC.GROUPS G, DOC.GROUPS_USER GU WHERE G.ID = GU.ID " +
                    "AND STATE > 0 AND USER_ID = ? AND COMPANY = ? ORDER BY ROLE_ID ASC";
    private static final String SELECT_GROUP_LIST_CONTAIN_ROLE =
            "SELECT NAME FROM DOC.GROUPS WHERE STATE = 1 AND ROLE_ID = ? AND COMPANY = ? ORDER BY NAME";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Group get(int groupId, int companyId) throws DataAccessException {
        Group resultGroup = jdbcTemplate.query(SELECT_GROUP,
                new Object[]{groupId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        Group group = new Group();
                        group.setId(rs.getInt("ID"));
                        group.setName(rs.getString("NAME"));
                        group.setState(rs.getInt("STATE"));
                        group.setRoleId(rs.getInt("ROLE_ID"));
                        return group;
                    } else {
                        throw new NotFoundDaoException("Not found Group with groupId = " + groupId +
                                " and companyId = " + companyId);
                    }
                }
        );

        List<Group.LinkUser> resultLinkUsers = jdbcTemplate.query(SELECT_GROUP_USER,
                new Object[]{groupId},
                rs -> {
                    List<Group.LinkUser> linkUsers = new ArrayList<>();
                    while(rs.next()){
                        Group.LinkUser linkUser = new Group.LinkUser();
                        linkUser.setUserId(rs.getInt("USER_ID"));
                        linkUser.setUserName(rs.getString("NAME"));
                        linkUser.setParticipant(rs.getInt("ACTOR_TYPE") == Group.ActorType.PARTICIPANT);

                        linkUsers.add(linkUser);
                    }
                    return linkUsers;
                }
        );

        resultGroup.setLinkUsers(resultLinkUsers);
        return resultGroup;
    }

    @Override
    public List<Group> getSuperList() throws DataAccessException {
        Object[] params = new Object[]{};
        return getList(SELECT_SUPER_GROUP_LIST, params);
    }

    @Override
    public List<Group> getAdminList(int companyId) throws DataAccessException {
        Object[] params = new Object[]{companyId};
        return getList(SELECT_ADMIN_GROUP_LIST, params);
    }

    private List<Group> getList(String query, Object[] params) throws DataAccessException {
        return jdbcTemplate.query(query, params,
                (rs) -> {
                    List<Group> groups = new ArrayList<>();
                    while(rs.next()){
                        Group group = new Group();
                        group.setId(rs.getInt("ID"));
                        group.setName(rs.getString("NAME"));
                        group.setState(rs.getInt("STATE"));
                        group.setRoleName(rs.getString("ROLE_NAME"));
                        group.setCompanyId(rs.getInt("COMPANY"));
                        group.setCompanyName(rs.getString("COMPANY_NAME"));

                        groups.add(group);
                    }
                    return groups;
                }
        );
    }

    @Override
    public Map<String, Group> getRouteGroup(int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_POINT_MAP,
                new Object[]{companyId},
                rs -> {
                    Map<String, Group> groups = new LinkedHashMap<>();
                    while(rs.next()){
                        Group group = new Group();
                        group.setId(rs.getInt("GROUP_ID"));
                        group.setRoleId(rs.getInt("ROLE_ID"));
                        group.setRoleName(rs.getString("ROLE_NAME"));

                        groups.put(rs.getString("GROUP_NAME"), group);
                    }
                    return groups;
                }
        );
    }

    @Override
    public Map<String, Integer> getUserPublishGroup(int userId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_PUB_GROUP_MAP,
                new Object[]{Role.Type.ADMIN, Role.Type.PUBLIC, companyId, userId},
                rs -> {
                    Map<String, Integer> groups = new LinkedHashMap<>();
                    while(rs.next()){
                        groups.put(rs.getString("G_NAME"), rs.getInt("G_ID"));
                    }
                    return groups;
                }
        );
    }

    @Override
    public Map<String, Integer> getCreateGroup(int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_CREATE_GROUP_MAP,
                new Object[]{Role.Type.CREATE, companyId},
                rs -> {
                    Map<String, Integer> groups = new LinkedHashMap<>();
                    while(rs.next()){
                        groups.put(rs.getString("NAME"), rs.getInt("ID"));
                    }
                    return groups;
                }
        );
    }

    @Override
    public Map<String, Integer> getUserCreateGroup(int userId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USER_CREATE_GROUP_MAP,
                new Object[]{Role.Type.CREATE, userId, companyId},
                rs -> {
                    Map<String, Integer> groups = new LinkedHashMap<>();
                    while(rs.next()){
                        groups.put(rs.getString("NAME"), rs.getInt("ID"));
                    }
                    return groups;
                }
        );
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> getUserRolesGroupActivity(int userId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USER_GROUP_ACTIVITY_MAP,
                new Object[]{userId, companyId},
                (rs) -> {
                    Map<Integer, Map<Integer, Integer>> userRolesGroupActivity = new LinkedHashMap<>();
                    Map<Integer, Integer> groupsActivity = new LinkedHashMap<>();
                    int oldRoleId = 0;
                    while(rs.next()){
                        int roleId = rs.getInt("ROLE_ID");
                        int groupId = rs.getInt("ID");
                        int activity = rs.getInt("ACTOR_TYPE");

                        if (roleId != oldRoleId) {
                            oldRoleId = roleId;
                            groupsActivity = new LinkedHashMap<>();
                            userRolesGroupActivity.put(roleId, groupsActivity);
                        }

                        groupsActivity.put(groupId, activity);
                    }
                    return userRolesGroupActivity;
                }
        );
    }

    @Override
    public List<String> groupsWithRole(int roleId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_GROUP_LIST_CONTAIN_ROLE,
                new Object[]{roleId, companyId},
                (rs, rowNum) -> rs.getString("NAME")
        );
    }

    @Override
    @Transactional
    public int create(Group group, int companyId) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_GROUP, new String[] {"id"});
                    pstmt.setString(1, group.getName());
                    pstmt.setInt(2, Group.State.EXISTS);
                    pstmt.setInt(3, group.getRoleId());
                    pstmt.setInt(4, companyId);
                    return pstmt;
                }, keyHolder
        );

        jdbcTemplate.batchUpdate(INSERT_GROUP_USER,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setInt(2, group.getLinkUsers().get(i).getUserId());
                        pstmt.setInt(3, group.getLinkUsers().get(i).isParticipant()
                                ? Group.ActorType.PARTICIPANT : Group.ActorType.SPECTATOR);
                    }
                    public int getBatchSize() {
                        return group.getLinkUsers().size();
                    }
                }
        );
        return keyHolder.getKey().intValue();
    }

    @Override
    @Transactional
    public void update(Group group, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_GROUP,
                group.getName(),
                group.getRoleId(),
                group.getId(),
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Group with groupId = " + group.getId() +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_GROUP_USER,
                group.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_GROUP_USER,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, group.getId());
                        pstmt.setInt(2, group.getLinkUsers().get(i).getUserId());
                        pstmt.setInt(3, group.getLinkUsers().get(i).isParticipant()
                                ? Group.ActorType.PARTICIPANT : Group.ActorType.SPECTATOR);
                    }
                    public int getBatchSize() {
                        return group.getLinkUsers().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void delete(int groupId, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(DELETE_GROUP,
                Group.State.DELETED,
                groupId,
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Group with groupId = " + groupId +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_GROUP_USER,
                groupId
        );
    }
}
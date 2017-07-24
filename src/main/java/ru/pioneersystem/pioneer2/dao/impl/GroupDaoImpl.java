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
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
    private static final String SELECT_GROUP_LIST =
            "SELECT ID, NAME, STATE FROM DOC.GROUPS WHERE STATE > 0 AND COMPANY = ? ORDER BY STATE DESC, NAME ASC";
    private static final String SELECT_POINT_MAP =
            "SELECT G.ID AS GROUP_ID, G.NAME AS GROUP_NAME, R.ID AS ROLE_ID, R.NAME AS ROLE_NAME FROM DOC.GROUPS G " +
                    "LEFT JOIN DOC.ROLES R ON G.ROLE_ID = R.ID \nWHERE TYPE = 10 AND G.STATE > 0 AND G.COMPANY = ? " +
                    "ORDER BY GROUP_NAME ASC";
    private static final String SELECT_PUB_GROUP_MAP =
            "SELECT G.ID AS G_ID, G.NAME AS G_NAME FROM DOC.GROUPS G, DOC.GROUPS_USER GU, DOC.ROLES R " +
                    "WHERE G.ID = GU.ID AND G.ROLE_ID = R.ID AND TYPE IN (?, ?) AND G.STATE > 0 AND G.COMPANY = ? " +
                    "AND USER_ID = ? ORDER BY G.NAME ASC";
    private static final String SELECT_CREATE_GROUP_MAP =
            "SELECT G.ID AS ID, NAME FROM DOC.GROUPS G, DOC.GROUPS_USER GU " +
                    "WHERE G.ID = GU.ID AND ROLE_ID = ? AND COMPANY = ? AND STATE > 0 AND USER_ID = ? ORDER BY NAME ASC";

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
                        throw new NotFoundDaoException("Not found Group with groupId=" + groupId +
                                " and companyId=" + companyId);
                    }
                }
        );

        List<Group.LinkUser> resultLinkUsers = jdbcTemplate.query(SELECT_GROUP_USER,
                new Object[]{groupId},
                rs -> {
                    List<Group.LinkUser> linkUsers = new LinkedList<>();
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
    public List<Group> getList(int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_GROUP_LIST,
                new Object[]{companyId},
                (rs, rowNum) -> {
                    Group group = new Group();
                    group.setId(rs.getInt("ID"));
                    group.setName(rs.getString("NAME"));
                    group.setState(rs.getInt("STATE"));
                    return group;
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
    public Map<String, Integer> getUserPublishGroup(int companyId, int userId) throws DataAccessException {
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
    public Map<String, Integer> getUserCreateGroup(int companyId, int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_CREATE_GROUP_MAP,
                new Object[]{Role.Type.CREATE, companyId, userId},
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
    @Transactional
    public void create(Group group, int companyId) throws DataAccessException {
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
            throw new NotFoundDaoException("Not found Group with groupId=" + group.getId() +
                    " and companyId=" + companyId);
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
            throw new NotFoundDaoException("Not found Group with groupId=" + groupId +
                    " and companyId=" + companyId);
        }

        jdbcTemplate.update(DELETE_GROUP_USER, groupId);
    }
}
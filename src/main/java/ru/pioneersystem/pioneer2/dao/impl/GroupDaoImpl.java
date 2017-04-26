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
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.model.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Repository(value = "groupDao")
public class GroupDaoImpl implements GroupDao {
    private static final String INSERT_GROUP =
            "INSERT INTO DOC.GROUPS (NAME, STATE, ROLE_ID, COMPANY) VALUES (?, ?, ?, ?)";
    private static final String INSERT_GROUP_USER =
            "INSERT INTO DOC.GROUPS_USER (ID, USER_ID, ACTOR_TYPE) VALUES (?, ?, ?)";
    private static final String UPDATE_GROUP =
            "UPDATE DOC.GROUPS SET NAME = ?, ROLE_ID = ? WHERE ID = ?";
    private static final String DELETE_GROUP =
            "UPDATE DOC.GROUPS SET STATE = ? WHERE ID = ?";
    private static final String DELETE_GROUP_USER =
            "DELETE FROM DOC.GROUPS_USER WHERE ID = ?";
    private static final String SELECT_GROUP =
            "SELECT ID, NAME, STATE, ROLE_ID FROM DOC.GROUPS WHERE ID = ?";
    private static final String SELECT_GROUP_USER =
            "SELECT USER_ID, ACTOR_TYPE, NAME FROM DOC.GROUPS_USER GU " +
                    "LEFT JOIN DOC.USERS U ON GU.USER_ID = U.ID WHERE GU.ID = ? ORDER BY NAME ASC";
    private static final String SELECT_GROUP_LIST =
            "SELECT ID, NAME, STATE FROM DOC.GROUPS WHERE STATE > 0 AND COMPANY = ? ORDER BY STATE DESC, NAME ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Group get(int id) throws DataAccessException {
        Group resultGroup = jdbcTemplate.queryForObject(SELECT_GROUP,
                new Object[]{id},
                (rs, rowNum) -> {
                    Group group = new Group();
                    group.setId(rs.getInt("ID"));
                    group.setName(rs.getString("NAME"));
                    group.setState(rs.getInt("STATE"));
                    group.setRoleId(rs.getInt("ROLE_ID"));
                    return group;
                }
        );

        List<Group.LinkUser> resultLinkUsers = jdbcTemplate.query(SELECT_GROUP_USER,
                new Object[]{id},
                rs -> {
                    List<Group.LinkUser> linkUsers = new LinkedList<>();
                    while(rs.next()){
                        User user = new User();
                        user.setId(rs.getInt("USER_ID"));
                        user.setName(rs.getString("NAME"));
                        Group.LinkUser linkUser = new Group.LinkUser();
                        linkUser.setUser(user);
                        linkUser.setGroupId(id);
                        linkUser.setUserId(rs.getInt("USER_ID"));
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
    public List<Group> getList(int company) throws DataAccessException {
        return jdbcTemplate.query(SELECT_GROUP_LIST,
                new Object[]{company},
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
    @Transactional
    public void create(Group group, int company) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_GROUP, new String[] {"id"});
                    pstmt.setString(1, group.getName());
                    pstmt.setInt(2, Group.State.EXISTS);
                    pstmt.setInt(3, group.getRoleId());
                    pstmt.setInt(4, company);
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
    public void update(Group group) throws DataAccessException {
        jdbcTemplate.update(UPDATE_GROUP,
                group.getName(),
                group.getRoleId(),
                group.getId()
        );

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
    public void delete(int id) throws DataAccessException {
        jdbcTemplate.update(DELETE_GROUP, Group.State.DELETED, id);
        jdbcTemplate.update(DELETE_GROUP_USER, id);
    }
}
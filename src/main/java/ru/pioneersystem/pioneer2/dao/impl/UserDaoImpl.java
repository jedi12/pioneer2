package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.UserDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.model.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository(value = "userDao")
public class UserDaoImpl implements UserDao {
    private static final int LOCKED = 0;
    private static final int ACTIVE = 1;
    public static final String USER_ID = "userId";
    public static final String PASS = "pass";

    private static final String INSERT_USER = "INSERT INTO DOC.USERS (LOGIN, NAME, STATE, EMAIL, PHONE, COMPANY, " +
            "COMMENT, POSITION, NOTICE_DOC_IN, NOTICE_STATUS_CH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_GROUP_USER =
            "INSERT INTO DOC.GROUPS_USER (ID, USER_ID, ACTOR_TYPE) VALUES (?, ?, ?)";
    private static final String DELETE_GROUP_USER =
            "DELETE FROM DOC.GROUPS_USER WHERE USER_ID = ?";
    private static final String UPDATE_USER = "UPDATE DOC.USERS SET LOGIN = ?, NAME = ?, EMAIL = ?, PHONE = ?, " +
            "COMMENT = ?, POSITION = ?, NOTICE_DOC_IN = ?, NOTICE_STATUS_CH = ? WHERE ID = ? AND COMPANY = ?";
    private static final String UPDATE_USER_LOCK = "UPDATE DOC.USERS SET STATE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String UPDATE_USER_CHANGE_PASS = "UPDATE DOC.USERS SET PASS = ? WHERE ID = ?";
    private static final String SELECT_USER =
            "SELECT U.ID AS ID, LOGIN, U.NAME AS USER_NAME, U.STATE AS STATE, U.EMAIL AS EMAIL, " +
                    "U.PHONE AS PHONE, COMPANY, U.COMMENT AS COMMENT, POSITION, NOTICE_DOC_IN, NOTICE_STATUS_CH, " +
                    "C.NAME AS COMPANY_NAME FROM DOC.USERS U LEFT JOIN DOC.COMPANY C ON C.ID = U.COMPANY " +
                    "WHERE U.ID = ? AND COMPANY = ?";
    private static final String SELECT_GROUP_USER =
            "SELECT G.ID AS GROUP_ID, ACTOR_TYPE, NAME FROM DOC.GROUPS_USER GU LEFT JOIN DOC.GROUPS G ON G.ID = GU.ID " +
                    "WHERE USER_ID = ? ORDER BY NAME ASC";
    private static final String SELECT_USER_WITH_COMPANY = "SELECT U.ID AS U_ID, U.LOGIN AS U_LOGIN, U.NAME AS U_NAME, " +
            "U.STATE AS U_STATE, U.EMAIL AS U_EMAIL, U.PHONE AS U_PHONE, U.COMPANY AS U_COMPANY, U.COMMENT AS U_COMMENT, " +
            "U.POSITION AS U_POSITION, U.NOTICE_DOC_IN AS U_NOTICE_DOC_IN, U.NOTICE_STATUS_CH AS U_NOTICE_STATUS_CH, " +
            "C.ID AS C_ID, C.NAME AS C_NAME, C.FULL_NAME AS C_FULL_NAME, C.PHONE AS C_PHONE, C.EMAIL AS C_EMAIL, " +
            "C.ADDRESS AS C_ADDRESS, C.STATE AS C_STATE, C.MAX_USERS AS C_MAX_USERS, C.SITE AS C_SITE, " +
            "C.COMMENT AS C_COMMENT FROM DOC.USERS U, DOC.COMPANY C WHERE U.COMPANY = C.ID AND U.ID = ?";
    private static final String SELECT_SUPER_USER_LIST =
            "SELECT U.ID AS ID, U.NAME AS NAME, LOGIN, U.EMAIL AS EMAIL, U.STATE AS STATE, COMPANY, " +
                    "C.NAME AS COMPANY_NAME FROM DOC.USERS U LEFT JOIN DOC.COMPANY C ON C.ID = U.COMPANY " +
                    "WHERE COMPANY >= 0 ORDER BY COMPANY ASC, NAME ASC";
    private static final String SELECT_ADMIN_USER_LIST =
            "SELECT ID, NAME, LOGIN, EMAIL, STATE, COMPANY, NULL AS COMPANY_NAME FROM DOC.USERS WHERE COMPANY = ? " +
                    "ORDER BY NAME ASC";
    private static final String SELECT_USERS_IN_GROUP_LIST =
            "SELECT USER_ID, NAME, EMAIL, PHONE, POSITION FROM DOC.GROUPS_USER GU, DOC.USERS U WHERE GU.USER_ID = U.ID " +
                    "AND GU.ID = ? AND ACTOR_TYPE = 1 AND STATE = 1 AND COMPANY = ? ORDER BY NAME";
    private static final String SELECT_ID_AND_PASS = "SELECT ID, PASS FROM DOC.USERS WHERE LOGIN = ?";
    private static final String SELECT_COUNT = "SELECT COUNT(*) FROM DOC.USERS WHERE COMPANY = ? AND STATE = ?";
    private static final String SELECT_COUNT_BY_LOGIN = "SELECT COUNT(*) FROM DOC.USERS WHERE LOGIN = ?";
    private static final String SELECT_COUNT_BY_EMAIL = "SELECT COUNT(*) FROM DOC.USERS WHERE EMAIL = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User get(int userId, int companyId) throws DataAccessException {
        User resultUser =  jdbcTemplate.query(SELECT_USER,
                new Object[]{userId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("ID"));
                        user.setLogin(rs.getString("LOGIN"));
                        user.setName(rs.getString("USER_NAME"));
                        user.setState(rs.getInt("STATE"));
                        user.setEmail(rs.getString("EMAIL"));
                        user.setPhone(rs.getString("PHONE"));
                        user.setCompanyId(rs.getInt("COMPANY"));
                        user.setCompanyName(rs.getString("COMPANY_NAME"));
                        user.setComment(rs.getString("COMMENT"));
                        user.setPosition(rs.getString("POSITION"));
                        user.setNoticeDocIncoming(rs.getInt("NOTICE_DOC_IN") == 1);
                        user.setNoticeStatusChanged(rs.getInt("NOTICE_STATUS_CH") == 1);
                        return user;
                    } else {
                        throw new NotFoundDaoException("Not found User with userId = " + userId +
                                " and companyId = " + companyId);
                    }
                }
        );

        List<User.LinkGroup> resultLinkGroups = jdbcTemplate.query(SELECT_GROUP_USER,
                new Object[]{userId},
                rs -> {
                    List<User.LinkGroup> linkGroups = new ArrayList<>();
                    while(rs.next()){
                        User.LinkGroup linkGroup = new User.LinkGroup();
                        linkGroup.setGroupId(rs.getInt("GROUP_ID"));
                        linkGroup.setGroupName(rs.getString("NAME"));
                        linkGroup.setParticipant(rs.getInt("ACTOR_TYPE") == Group.ActorType.PARTICIPANT);

                        linkGroups.add(linkGroup);
                    }
                    return linkGroups;
                }
        );

        resultUser.setLinkGroups(resultLinkGroups);
        return resultUser;
    }

    @Override
    public User getWithCompany(int userId) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_USER_WITH_COMPANY,
                new Object[]{userId},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getInt("U_ID"));
                    user.setLogin(rs.getString("U_LOGIN"));
                    user.setName(rs.getString("U_NAME"));
                    user.setEmail(rs.getString("U_EMAIL"));
                    user.setPhone(rs.getString("U_PHONE"));
                    user.setCompanyId(rs.getInt("U_COMPANY"));
                    user.setComment(rs.getString("U_COMMENT"));
                    user.setPosition(rs.getString("U_POSITION"));
                    user.setState(rs.getInt("U_STATE"));
                    user.setNoticeDocIncoming(rs.getInt("U_NOTICE_DOC_IN") == 1);
                    user.setNoticeStatusChanged(rs.getInt("U_NOTICE_STATUS_CH") == 1);

                    Company comp = new Company();
                    comp.setId(rs.getInt("C_ID"));
                    comp.setName(rs.getString("C_NAME"));
                    comp.setFullName(rs.getString("C_FULL_NAME"));
                    comp.setPhone(rs.getString("C_PHONE"));
                    comp.setEmail(rs.getString("C_EMAIL"));
                    comp.setAddress(rs.getString("C_ADDRESS"));
                    comp.setMaxUsers(rs.getInt("C_MAX_USERS"));
                    comp.setSite(rs.getString("C_SITE"));
                    comp.setComment(rs.getString("C_COMMENT"));
                    comp.setState(rs.getInt("C_STATE"));

                    user.setCompany(comp);
                    return user;
                }
        );
    }

    @Override
    public List<User> getSuperList() throws DataAccessException {
        Object[] params = new Object[]{};
        return getList(SELECT_SUPER_USER_LIST, params);
    }

    @Override
    public List<User> getAdminList(int companyId) throws DataAccessException {
        Object[] params = new Object[]{companyId};
        return getList(SELECT_ADMIN_USER_LIST, params);
    }

    private List<User> getList(String query, Object[] params) throws DataAccessException {
        return jdbcTemplate.query(query, params,
                (rs) -> {
                    List<User> users = new ArrayList<>();
                    while(rs.next()){
                        User user = new User();
                        user.setId(rs.getInt("ID"));
                        user.setName(rs.getString("NAME"));
                        user.setLogin(rs.getString("LOGIN"));
                        user.setEmail(rs.getString("EMAIL"));
                        user.setState(rs.getInt("STATE"));
                        user.setCompanyId(rs.getInt("COMPANY"));
                        user.setCompanyName(rs.getString("COMPANY_NAME"));

                        users.add(user);
                    }
                    return users;
                }
        );
    }

    @Override
    public List<User> getInGroup(int groupId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USERS_IN_GROUP_LIST,
                new Object[]{groupId, companyId},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getInt("USER_ID"));
                    user.setName(rs.getString("NAME"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setPhone(rs.getString("PHONE"));
                    user.setPosition(rs.getString("POSITION"));
                    return user;
                }
        );
    }

    @Override
    @Transactional
    public int create(User user, int companyId) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_USER, new String[] {"id"});
                    pstmt.setString(1, user.getLogin());
                    pstmt.setString(2, user.getName());
                    pstmt.setInt(3, ACTIVE);
                    pstmt.setString(4, user.getEmail());
                    pstmt.setString(5, user.getPhone());
                    pstmt.setInt(6, companyId);
                    pstmt.setString(7, user.getComment());
                    pstmt.setString(8, user.getPosition());
                    pstmt.setInt(9, user.isNoticeDocIncoming() ? 1 : 0);
                    pstmt.setInt(10, user.isNoticeStatusChanged() ? 1 : 0);
                    return pstmt;
                }, keyHolder
        );

        jdbcTemplate.batchUpdate(INSERT_GROUP_USER,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, user.getLinkGroups().get(i).getGroupId());
                        pstmt.setInt(2, keyHolder.getKey().intValue());
                        pstmt.setInt(3, user.getLinkGroups().get(i).isParticipant()
                                ? Group.ActorType.PARTICIPANT : Group.ActorType.SPECTATOR);
                    }
                    public int getBatchSize() {
                        return user.getLinkGroups().size();
                    }
                }
        );
        return keyHolder.getKey().intValue();
    }

    @Override
    @Transactional
    public void update(User user, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_USER,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getComment(),
                user.getPosition(),
                user.isNoticeDocIncoming() ? 1 : 0,
                user.isNoticeStatusChanged() ? 1 : 0,
                user.getId(),
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found User with userId = " + user.getId() +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_GROUP_USER,
                user.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_GROUP_USER,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, user.getLinkGroups().get(i).getGroupId());
                        pstmt.setInt(2, user.getId());
                        pstmt.setInt(3, user.getLinkGroups().get(i).isParticipant()
                                ? Group.ActorType.PARTICIPANT : Group.ActorType.SPECTATOR);
                    }
                    public int getBatchSize() {
                        return user.getLinkGroups().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void setState(int state, int userId, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_USER_LOCK,
                state,
                userId,
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found User with userId = " + userId +
                    " and companyId = " + companyId);
        }
    }

    @Override
    @Transactional
    public void savePass(int userId, String passHash) throws DataAccessException {
        jdbcTemplate.update(UPDATE_USER_CHANGE_PASS,
                passHash,
                userId
        );
    }

    @Override
    @Transactional
    public Map<String, Object> getUserIdAndPass(String login) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ID_AND_PASS,
                new Object[]{login},
                (rs) -> {
                    if (rs.next()) {
                        Map<String, Object> userIdAndPass = new HashMap<>();
                        userIdAndPass.put(USER_ID, rs.getInt("ID"));
                        userIdAndPass.put(PASS, rs.getString("PASS"));
                        return userIdAndPass;
                    } else {
                        throw new NotFoundDaoException("User not found by login = " + login);
                    }
                });
    }

    @Override
    public int getCount(int companyId, int state) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_COUNT,
                new Object[]{companyId, state},
                (rs, rowNum) -> rs.getInt(1)
        );
    }

    @Override
    public int getCountByLogin(String login) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_COUNT_BY_LOGIN,
                new Object[]{login},
                (rs, rowNum) -> rs.getInt(1)
        );
    }

    @Override
    public int getCountByEmail(String email) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_COUNT_BY_EMAIL,
                new Object[]{email},
                (rs, rowNum) -> rs.getInt(1)
        );
    }
}
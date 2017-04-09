package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.UserDao;
import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.model.User;

import java.sql.PreparedStatement;
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
            "COMMENT) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_USER = "UPDATE DOC.USERS SET LOGIN = ?, NAME = ?, EMAIL = ?, PHONE = ?, " +
            "COMMENT = ? WHERE ID = ?";
    private static final String UPDATE_USER_LOCK = "UPDATE DOC.USERS SET STATE = ? WHERE ID = ?";
    private static final String UPDATE_USER_UNLOCK = "UPDATE DOC.USERS SET STATE = ? WHERE ID = ?";
    private static final String UPDATE_USER_CHANGE_PASS = "UPDATE DOC.USERS SET PASS = ? WHERE ID = ?";
    private static final String SELECT_USER = "SELECT ID, LOGIN, NAME, STATE, EMAIL, PHONE, COMPANY, COMMENT " +
            "FROM DOC.USERS WHERE ID = ?";
    private static final String SELECT_USER_WITH_COMPANY = "SELECT U.ID AS U_ID, U.LOGIN AS U_LOGIN, U.NAME AS U_NAME, " +
            "U.STATE AS U_STATE, U.EMAIL AS U_EMAIL, U.PHONE AS U_PHONE, U.COMPANY AS U_COMPANY, U.COMMENT AS U_COMMENT, " +
            "C.ID AS C_ID, C.NAME AS C_NAME, C.FULL_NAME AS C_FULL_NAME, C.PHONE AS C_PHONE, C.EMAIL AS C_EMAIL, " +
            "C.ADDRESS AS C_ADDRESS, C.STATE AS C_STATE, C.MAX_USERS AS C_MAX_USERS, C.SITE AS C_SITE, " +
            "C.COMMENT AS C_COMMENT FROM DOC.USERS U, DOC.COMPANY C WHERE U.COMPANY = C.ID AND U.ID = ?";
    private static final String SELECT_USER_LIST = "SELECT ID, NAME, LOGIN, EMAIL, STATE FROM DOC.USERS WHERE COMPANY = ?";
    private static final String SELECT_ID_AND_PASS = "SELECT ID, PASS FROM DOC.USERS WHERE LOGIN = ?";
    private static final String SELECT_COUNT = "SELECT COUNT(*) FROM DOC.USERS WHERE COMPANY = ? AND STATE = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User get(int id) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_USER,
                new Object[]{id},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getInt("ID"));
                    user.setLogin(rs.getString("LOGIN"));
                    user.setName(rs.getString("NAME"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setPhone(rs.getString("PHONE"));
                    user.setCompanyId(rs.getInt("COMPANY"));
                    user.setComment(rs.getString("COMMENT"));
                    user.setState(rs.getInt("STATE"));
                    return user;
                }
        );
    }

    @Override
    public User getWithCompany(int id) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_USER_WITH_COMPANY,
                new Object[]{id},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getInt("U_ID"));
                    user.setLogin(rs.getString("U_LOGIN"));
                    user.setName(rs.getString("U_NAME"));
                    user.setEmail(rs.getString("U_EMAIL"));
                    user.setPhone(rs.getString("U_PHONE"));
                    user.setCompanyId(rs.getInt("U_COMPANY"));
                    user.setComment(rs.getString("U_COMMENT"));
                    user.setState(rs.getInt("U_STATE"));
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
    public List<User> getList(int company) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USER_LIST,
                new Object[]{company},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getInt("ID"));
                    user.setName(rs.getString("NAME"));
                    user.setLogin(rs.getString("LOGIN"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setState(rs.getInt("STATE"));
                    return user;
                }
        );
    }

    @Override
    @Transactional
    public void create(User user, int company) throws DataAccessException {
        // TODO: 01.04.2017 Сделать обработку ошибки, связанной с неуникальным логином или емайлом
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_USER, new String[] {"id"});
                    pstmt.setString(1, user.getLogin());
                    pstmt.setString(2, user.getName());
                    pstmt.setInt(3, ACTIVE);
                    pstmt.setString(4, user.getEmail());
                    pstmt.setString(5, user.getPhone());
                    pstmt.setInt(6, company);
                    pstmt.setString(7, user.getComment());
                    return pstmt;
                }, keyHolder
        );
    }

    @Override
    @Transactional
    public void update(User user) throws DataAccessException {
        // TODO: 01.04.2017 Сделать обработку ошибки, связанной с неуникальным логином или емайлом
        jdbcTemplate.update(UPDATE_USER,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getComment(),
                user.getId()
        );
    }

    @Override
    @Transactional
    public void lock(int id) throws DataAccessException {
        jdbcTemplate.update(UPDATE_USER_LOCK, LOCKED, id);
    }

    @Override
    @Transactional
    public void unlock(int id) throws DataAccessException {
        jdbcTemplate.update(UPDATE_USER_UNLOCK, ACTIVE, id);
    }

    @Override
    @Transactional
    public void savePass(int id, String passHash) throws DataAccessException {
        jdbcTemplate.update(UPDATE_USER_CHANGE_PASS, passHash, id);
    }

    @Override
    @Transactional
    public Map<String, Object> getUserIdAndPass(String login) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ID_AND_PASS,
                new Object[]{login},
                rs -> {
                    Map<String, Object> userIdAndPass = new HashMap<>();
                    while(rs.next()){
                        userIdAndPass.put(USER_ID, rs.getInt("ID"));
                        userIdAndPass.put(PASS, rs.getString("PASS"));
                    }
                    return userIdAndPass;
                });
    }

    @Override
    public int getCount(int company, int state) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_COUNT,
                new Object[]{company, state},
                (rs, rowNum) -> rs.getInt(1)
        );
    }
}
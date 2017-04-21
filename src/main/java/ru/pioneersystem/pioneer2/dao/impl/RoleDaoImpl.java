package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RoleDao;
import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Status;

import java.sql.PreparedStatement;
import java.util.List;

@Repository(value = "roleDao")
public class RoleDaoImpl implements RoleDao {
    private static final String INSERT_ROLE =
            "INSERT INTO DOC.ROLES (NAME, STATE, COMPANY, TYPE, ACCEPT, REJECT) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_STATUS =
            "INSERT INTO DOC.DOCUMENTS_STATUS (ID, NAME, STATE, TYPE, COMPANY) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_MENU =
            "INSERT INTO DOC.MENU (NAME, PAGE, NUM, PARENT, ROLE_ID, STATE, COMPANY) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ROLE =
            "UPDATE DOC.ROLES SET NAME = ?, TYPE = ?, ACCEPT = ?, REJECT = ? WHERE ID = ?";
    private static final String UPDATE_STATUS =
            "UPDATE DOC.DOCUMENTS_STATUS SET NAME = ? WHERE ID = ?";
    private static final String UPDATE_MENU =
            "UPDATE DOC.MENU SET NAME = ? WHERE ROLE_ID = ?";
    private static final String DELETE_ROLE =
            "UPDATE DOC.ROLES SET STATE = ? WHERE ID = ?";
    private static final String DELETE_STATUS =
            "UPDATE DOC.DOCUMENTS_STATUS SET STATE = ? WHERE ID = ?";
    private static final String DELETE_MENU =
            "UPDATE DOC.MENU SET STATE = ? WHERE ROLE_ID = ?";
    private static final String SELECT_ROLE =
            "SELECT R.ID AS R_ID, R.NAME AS R_NAME, R.STATE AS R_STATE, R.TYPE AS R_TYPE, R.ACCEPT AS R_ACCEPT, " +
                    "R.REJECT AS R_REJECT, DS.NAME AS STATUS_NAME, M.NAME AS MENU_NAME FROM DOC.ROLES R " +
                    "LEFT JOIN DOC.DOCUMENTS_STATUS DS ON R.ID = DS.ID " +
                    "LEFT JOIN DOC.MENU M ON R.ID = M.ROLE_ID " +
                    "WHERE R.ID = ?";
    private static final String SELECT_ROLE_LIST =
            "SELECT ID, NAME, STATE, TYPE FROM DOC.ROLES WHERE STATE > 0 AND COMPANY = ? OR STATE = ? ORDER BY STATE DESC, NAME ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Role get(int id) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_ROLE,
                new Object[]{id},
                (rs, rowNum) -> {
                    Role role = new Role();
                    role.setId(rs.getInt("R_ID"));
                    role.setName(rs.getString("R_NAME"));
                    role.setState(rs.getInt("R_STATE"));
                    role.setType(rs.getInt("R_TYPE"));
                    role.setAcceptButton(rs.getString("R_ACCEPT"));
                    role.setRejectButton(rs.getString("R_REJECT"));
                    role.setStatusName(rs.getString("STATUS_NAME"));
                    role.setMenuName(rs.getString("MENU_NAME"));
                    return role;
                }
        );
    }

    @Override
    public List<Role> getList(int company) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ROLE_LIST,
                new Object[]{company, Role.State.SYSTEM},
                (rs, rowNum) -> {
                    Role role = new Role();
                    role.setId(rs.getInt("ID"));
                    role.setName(rs.getString("NAME"));
                    role.setState(rs.getInt("STATE"));
                    role.setType(rs.getInt("TYPE"));
                    return role;
                }
        );
    }

    @Override
    @Transactional
    public void create(Role role, int company) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_ROLE, new String[] {"id"});
                    pstmt.setString(1, role.getName());
                    pstmt.setInt(2, Role.State.EXISTS);
                    pstmt.setInt(3, company);
                    pstmt.setInt(4, Role.Type.ROUTE);
                    pstmt.setString(5, role.getAcceptButton());
                    pstmt.setString(6, role.getRejectButton());
                    return pstmt;
                }, keyHolder
        );

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_STATUS);
                    pstmt.setInt(1, keyHolder.getKey().intValue());
                    pstmt.setString(2, role.getStatusName());
                    pstmt.setInt(3, Status.State.EXISTS);
                    pstmt.setInt(4, Status.Type.MEDIUM);
                    pstmt.setInt(5, company);
                    return pstmt;
                }
        );

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_MENU);
                    pstmt.setString(1, role.getMenuName());
                    pstmt.setString(2, "onRouteDocs.xhtml");
                    pstmt.setInt(3, keyHolder.getKey().intValue());
                    pstmt.setInt(4, 0);
                    pstmt.setInt(5, keyHolder.getKey().intValue());
                    pstmt.setInt(6, Menu.State.CREATED_BY_ROLE);
                    pstmt.setInt(7, company);
                    return pstmt;
                }
        );
    }

    @Override
    @Transactional
    public void update(Role role) throws DataAccessException {
        jdbcTemplate.update(UPDATE_ROLE,
                role.getName(),
                role.getType(),
                role.getAcceptButton(),
                role.getRejectButton(),
                role.getId()
        );

        jdbcTemplate.update(UPDATE_STATUS,
                role.getStatusName(),
                role.getId()
        );

        jdbcTemplate.update(UPDATE_MENU,
                role.getMenuName(),
                role.getId()
        );
    }

    @Override
    @Transactional
    public void delete(int id) throws DataAccessException {
        jdbcTemplate.update(DELETE_ROLE, Role.State.DELETED, id);
        jdbcTemplate.update(DELETE_STATUS, Status.State.DELETED, id);
        jdbcTemplate.update(DELETE_MENU, Menu.State.DELETED, id);
    }
}
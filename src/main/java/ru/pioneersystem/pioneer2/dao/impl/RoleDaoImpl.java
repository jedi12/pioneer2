package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RoleDao;
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
    private static final String UPDATE_ROLE =
            "UPDATE DOC.ROLES SET NAME = ?, TYPE = ?, ACCEPT = ?, REJECT = ? WHERE ID = ?";
    private static final String UPDATE_STATUS =
            "UPDATE DOC.DOCUMENTS_STATUS SET NAME = ? WHERE ID = ?";
    private static final String DELETE_ROLE =
            "UPDATE DOC.ROLES SET STATE = ? WHERE ID = ?";
    private static final String DELETE_STATUS =
            "UPDATE DOC.DOCUMENTS_STATUS SET STATE = ? WHERE ID = ?";
    private static final String SELECT_ROLE =
            "SELECT ID, NAME, STATE, TYPE, ACCEPT, REJECT FROM DOC.ROLES WHERE ID = ?";
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
                    role.setId(rs.getInt("ID"));
                    role.setName(rs.getString("NAME"));
                    role.setState(rs.getInt("STATE"));
                    role.setType(rs.getInt("TYPE"));
                    role.setAcceptButton(rs.getString("ACCEPT"));
                    role.setRejectButton(rs.getString("REJECT"));
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
    public void create(Role role, Status status, int company) throws DataAccessException {
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
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_STATUS, new String[] {"id"});
                    pstmt.setInt(1, keyHolder.getKey().intValue());
                    pstmt.setString(2, status.getName());
                    pstmt.setInt(3, Status.State.EXISTS);
                    pstmt.setInt(4, Status.Type.MEDIUM);
                    pstmt.setInt(5, company);
                    return pstmt;
                }, keyHolder
        );
    }

    @Override
    @Transactional
    public void update(Role role, Status status) throws DataAccessException {
        jdbcTemplate.update(UPDATE_ROLE,
                role.getName(),
                role.getType(),
                role.getAcceptButton(),
                role.getRejectButton(),
                role.getId()
        );

        jdbcTemplate.update(UPDATE_STATUS,
                status.getName(),
                role.getId()
        );
    }

    @Override
    @Transactional
    public void delete(int id) throws DataAccessException {
        jdbcTemplate.update(DELETE_ROLE, Role.State.DELETED, id);
        jdbcTemplate.update(DELETE_STATUS, Status.State.DELETED, id);
    }
}
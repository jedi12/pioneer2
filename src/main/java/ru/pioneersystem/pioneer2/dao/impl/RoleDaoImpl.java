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

import java.sql.PreparedStatement;
import java.util.List;

@Repository(value = "roleDao")
public class RoleDaoImpl implements RoleDao {
    private static final int DELETED = 0;
    private static final int EXISTS = 1;
    private static final int READ_ONLY = 2;

    private static final int TYPE_SUPER = 0;
    private static final int TYPE_ADMIN = 1;
    private static final int TYPE_USER = 2;

    private static final String INSERT_ROLE = "INSERT INTO DOC.ROLES (NAME, STATE, COMPANY, TYPE, CREATE, PUBLISH, " +
            "COMMENT, EDIT, CH_ROUTE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ROLE = "UPDATE DOC.ROLES SET NAME = ?, TYPE = ?, CREATE = ?, PUBLISH = ?, " +
            "COMMENT = ?, EDIT = ?, CH_ROUTE = ? WHERE ID = ?";
    private static final String DELETE_ROLE = "UPDATE DOC.ROLES SET STATE = ? WHERE ID = ?";
    private static final String SELECT_ROLE = "SELECT ID, NAME, STATE, TYPE, CREATE, PUBLISH, COMMENT, EDIT, " +
            "CH_ROUTE FROM DOC.ROLES WHERE ID = ?";
    private static final String SELECT_ROLE_LIST = "SELECT ID, NAME, STATE, TYPE FROM DOC.ROLES WHERE STATE > 0 " +
            "AND COMPANY = ? OR STATE = ? ORDER BY STATE DESC, NAME ASC";

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
                    role.setCreate(rs.getInt("CREATE") == 1);
                    role.setPublish(rs.getInt("PUBLISH") == 1);
                    role.setComment(rs.getInt("COMMENT") == 1);
                    role.setEdit(rs.getInt("EDIT") == 1);
                    role.setChangeRoute(rs.getInt("CH_ROUTE") == 1);
                    return role;
                }
        );
    }

    @Override
    public List<Role> getList(int company) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ROLE_LIST,
                new Object[]{company, READ_ONLY},
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
                    pstmt.setInt(2, EXISTS);
                    pstmt.setInt(3, company);
                    pstmt.setInt(4, TYPE_USER);
                    pstmt.setInt(5, role.isCreate() ? 1 : 0);
                    pstmt.setInt(6, role.isPublish() ? 1 : 0);
                    pstmt.setInt(7, role.isComment() ? 1 : 0);
                    pstmt.setInt(8, role.isEdit() ? 1 : 0);
                    pstmt.setInt(9, role.isChangeRoute() ? 1 : 0);
                    return pstmt;
                }, keyHolder
        );
    }

    @Override
    @Transactional
    public void update(Role role) throws DataAccessException {
        jdbcTemplate.update(UPDATE_ROLE,
                role.getName(),
                role.getType(),
                role.isCreate() ? 1 : 0,
                role.isPublish() ? 1 : 0,
                role.isComment() ? 1 : 0,
                role.isEdit() ? 1 : 0,
                role.isChangeRoute() ? 1 : 0,
                role.getId()
        );
    }

    @Override
    @Transactional
    public void delete(int id) throws DataAccessException {
        // TODO: 28.02.2017 Возможно нужна какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException

        jdbcTemplate.update(DELETE_ROLE, DELETED, id);
    }
}
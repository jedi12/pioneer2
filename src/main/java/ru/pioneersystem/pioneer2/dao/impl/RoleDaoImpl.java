package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RoleDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Status;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository(value = "roleDao")
public class RoleDaoImpl implements RoleDao {
    private static final String INSERT_ROLE =
            "INSERT INTO DOC.ROLES (NAME, STATE, COMPANY, TYPE, ACCEPT, REJECT, ROUTE_CHANGE, EDIT, COMMENT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_STATUS =
            "INSERT INTO DOC.DOCUMENTS_STATUS (ID, NAME, STATE, TYPE, COMPANY) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_MENU =
            "INSERT INTO DOC.MENU (NAME, PAGE, NUM, PARENT, ROLE_ID, STATE, COMPANY) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ROLE =
            "UPDATE DOC.ROLES SET NAME = ?, TYPE = ?, ACCEPT = ?, REJECT = ?, ROUTE_CHANGE = ?, EDIT = ?, " +
                    "COMMENT = ? WHERE ID = ? AND COMPANY = ?";
    private static final String UPDATE_STATUS =
            "UPDATE DOC.DOCUMENTS_STATUS SET NAME = ? WHERE ID = ?";
    private static final String UPDATE_MENU =
            "UPDATE DOC.MENU SET NAME = ? WHERE ROLE_ID = ?";
    private static final String DELETE_ROLE =
            "UPDATE DOC.ROLES SET STATE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_STATUS =
            "UPDATE DOC.DOCUMENTS_STATUS SET STATE = ? WHERE ID = ?";
    private static final String DELETE_MENU =
            "UPDATE DOC.MENU SET STATE = ? WHERE ROLE_ID = ?";
    private static final String SELECT_ROLE =
            "SELECT R.ID AS R_ID, R.NAME AS R_NAME, R.STATE AS R_STATE, R.TYPE AS R_TYPE, ACCEPT, REJECT, " +
                    "ROUTE_CHANGE, EDIT, COMMENT, DS.NAME AS STATUS_NAME, M.NAME AS MENU_NAME FROM DOC.ROLES R " +
                    "LEFT JOIN DOC.DOCUMENTS_STATUS DS ON R.ID = DS.ID LEFT JOIN DOC.MENU M ON R.ID = M.ROLE_ID " +
                    "WHERE R.ID = ? AND R.COMPANY IN (0, ?)";
    private static final String SELECT_SUPER_ROLE_LIST =
            "SELECT R.ID AS ID, R.NAME AS NAME, R.STATE AS STATE, TYPE, COMPANY, C.NAME AS COMPANY_NAME " +
                    "FROM DOC.ROLES R LEFT JOIN DOC.COMPANY C ON C.ID = R.COMPANY " +
                    "WHERE R.STATE > 0 ORDER BY R.COMPANY ASC, R.NAME ASC";
    private static final String SELECT_ADMIN_ROLE_LIST =
            "SELECT ID, NAME, STATE, TYPE, COMPANY, NULL AS COMPANY_NAME FROM DOC.ROLES WHERE STATE > 0 " +
                    "AND COMPANY = ? OR STATE = ? ORDER BY STATE DESC, NAME ASC";
    private static final String SELECT_USER_ROLE =
            "SELECT DISTINCT R.ID AS ID, R.NAME AS NAME, R.TYPE AS TYPE, ACCEPT, REJECT, ROUTE_CHANGE, EDIT, " +
                    "R.COMMENT AS COMMENT FROM DOC.GROUPS G, DOC.GROUPS_USER GU, DOC.ROLES R, DOC.USERS U " +
                    "WHERE G.ID = GU.ID AND G.ROLE_ID = R.ID AND GU.USER_ID = U.ID AND U.ID = ? AND U.COMPANY = ? " +
                    "ORDER BY R.ID ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Role get(int roleId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ROLE,
                new Object[]{roleId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        Role role = new Role();
                        role.setId(rs.getInt("R_ID"));
                        role.setName(rs.getString("R_NAME"));
                        role.setState(rs.getInt("R_STATE"));
                        role.setType(rs.getInt("R_TYPE"));
                        role.setAcceptButton(rs.getString("ACCEPT"));
                        role.setRejectButton(rs.getString("REJECT"));
                        role.setCanRouteChange(rs.getInt("ROUTE_CHANGE") == 1);
                        role.setCanEdit(rs.getInt("EDIT") == 1);
                        role.setCanComment(rs.getInt("COMMENT") == 1);
                        role.setStatusName(rs.getString("STATUS_NAME"));
                        role.setMenuName(rs.getString("MENU_NAME"));
                        return role;
                    } else {
                        throw new NotFoundDaoException("Not found Role with roleId = " + roleId +
                                " and companyId = " + companyId);
                    }
                }
        );
    }

    @Override
    public List<Role> getSuperList() throws DataAccessException {
        Object[] params = new Object[]{};
        return getList(SELECT_SUPER_ROLE_LIST, params);
    }

    @Override
    public List<Role> getAdminList(int companyId) throws DataAccessException {
        Object[] params = new Object[]{companyId, Role.State.SYSTEM};
        return getList(SELECT_ADMIN_ROLE_LIST, params);
    }

    private List<Role> getList(String query, Object[] params) throws DataAccessException {
        return jdbcTemplate.query(query, params,
                (rs) -> {
                    List<Role> roles = new ArrayList<>();
                    while(rs.next()){
                        Role role = new Role();
                        role.setId(rs.getInt("ID"));
                        role.setName(rs.getString("NAME"));
                        role.setState(rs.getInt("STATE"));
                        role.setType(rs.getInt("TYPE"));
                        role.setCompanyId(rs.getInt("COMPANY"));
                        role.setCompanyName(rs.getString("COMPANY_NAME"));

                        roles.add(role);
                    }
                    return roles;
                }
        );
    }

    @Override
    public Map<Integer, Role> getUserRole(int userId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USER_ROLE,
                new Object[]{userId, companyId},
                (rs) -> {
                    Map<Integer, Role> roleMap = new LinkedHashMap<>();
                    while(rs.next()){
                        Role role = new Role();
                        role.setId(rs.getInt("ID"));
                        role.setName(rs.getString("NAME"));
                        role.setType(rs.getInt("TYPE"));
                        role.setAcceptButton(rs.getString("ACCEPT"));
                        role.setRejectButton(rs.getString("REJECT"));
                        role.setCanRouteChange(rs.getInt("ROUTE_CHANGE") == 1);
                        role.setCanEdit(rs.getInt("EDIT") == 1);
                        role.setCanComment(rs.getInt("COMMENT") == 1);

                        roleMap.put(rs.getInt("ID"), role);
                    }
                    return roleMap;
                }
        );
    }

    @Override
    @Transactional
    public int create(Role role, int companyId) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_ROLE, new String[] {"id"});
                    pstmt.setString(1, role.getName());
                    pstmt.setInt(2, Role.State.EXISTS);
                    pstmt.setInt(3, companyId);
                    pstmt.setInt(4, Role.Type.ON_ROUTE);
                    pstmt.setString(5, role.getAcceptButton());
                    pstmt.setString(6, role.getRejectButton());
                    pstmt.setInt(7, role.isCanRouteChange() ? 1 : 0);
                    pstmt.setInt(8, role.isCanEdit() ? 1 : 0);
                    pstmt.setInt(9, role.isCanComment() ? 1 : 0);
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
                    pstmt.setInt(5, companyId);
                    return pstmt;
                }
        );

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_MENU);
                    pstmt.setString(1, role.getMenuName());
                    pstmt.setString(2, Menu.Page.ON_ROUTE_DOC);
                    pstmt.setInt(3, keyHolder.getKey().intValue());
                    pstmt.setInt(4, 0);
                    pstmt.setInt(5, keyHolder.getKey().intValue());
                    pstmt.setInt(6, Menu.State.CREATED_BY_ROLE);
                    pstmt.setInt(7, companyId);
                    return pstmt;
                }
        );
        return keyHolder.getKey().intValue();
    }

    @Override
    @Transactional
    public void update(Role role, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_ROLE,
                role.getName(),
                role.getType(),
                role.getAcceptButton(),
                role.getRejectButton(),
                role.isCanRouteChange(),
                role.isCanEdit(),
                role.isCanComment(),
                role.getId(),
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Role with roleId = " + role.getId() +
                    " and companyId = " + companyId);
        }

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
    public void delete(int roleId, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(DELETE_ROLE,
                Role.State.DELETED,
                roleId,
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Role with roleId = " + roleId +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_STATUS,
                Status.State.DELETED,
                roleId
        );

        jdbcTemplate.update(DELETE_MENU,
                Menu.State.DELETED,
                roleId
        );
    }
}
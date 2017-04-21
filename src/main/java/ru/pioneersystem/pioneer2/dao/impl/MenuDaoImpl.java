package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.MenuDao;
import ru.pioneersystem.pioneer2.model.Menu;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static ru.pioneersystem.pioneer2.model.Menu.State.DELETED;
import static ru.pioneersystem.pioneer2.model.Role.State.EXISTS;

@Repository(value = "menuDao")
public class MenuDaoImpl implements MenuDao {
    private static final String INSERT_MENU =
            "INSERT INTO DOC.MENU (NAME, PAGE, NUM, PARENT, ROLE_ID, STATE, COMPANY) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_MENU = "UPDATE DOC.MENU SET NAME = ? PAGE = ?, NUM = ?, ROLE_ID = ? WHERE ID = ?";
    private static final String DELETE_MENU = "UPDATE DOC.MENU SET STATE = ? WHERE ID = ? OR PARENT = ?";
    private static final String SELECT_MENU =
            "SELECT ID, NAME, PAGE, NUM, PARENT, ROLE_ID, STATE FROM DOC.MENU WHERE ID = ?";
    private static final String SELECT_SUB_MENU =
            "SELECT ID, NAME, PAGE, NUM, PARENT, ROLE_ID, STATE FROM DOC.MENU WHERE PARENT = ?";
    private static final String SELECT_MENU_LIST =
            "SELECT ID, NAME, STATE FROM DOC.MENU WHERE STATE > 0 AND COMPANY = ? OR STATE = ? ORDER BY NUM ASC";
    private static final String SELECT_USER_MENU_LIST =
            "SELECT ID, NAME, PAGE, NUM, PARENT, ROLE_ID, STATE FROM DOC.MENU WHERE ROLE_ID IN(" +
                    "SELECT ROLE_ID FROM DOC.GROUPS WHERE ID IN (" +
                    "SELECT ID FROM DOC.GROUPS_USER WHERE USER_ID = ?)) AND STATE > 0 ORDER BY PARENT DESC, NUM ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Menu get(int id) throws DataAccessException {
        Menu menuWithSubMenu = jdbcTemplate.queryForObject(SELECT_MENU,
                new Object[]{id},
                (rs, rowNum) -> {
                    Menu menu = new Menu();
                    menu.setId(rs.getInt("ID"));
                    menu.setName(rs.getString("NAME"));
                    menu.setPage(rs.getString("PAGE"));
                    menu.setNum(rs.getInt("NUM"));
                    menu.setParent(rs.getInt("PARENT"));
                    menu.setRoleId(rs.getInt("ROLE_ID"));
                    menu.setState(rs.getInt("STATE"));
                    return menu;
                }
        );

        List<Menu> subMenus = jdbcTemplate.query(SELECT_SUB_MENU,
                new Object[]{menuWithSubMenu.getParent()},
                rs -> {
                    List<Menu> subMenu = new LinkedList<>();
                    while(rs.next()){
                        Menu menu = new Menu();
                        menu.setId(rs.getInt("ID"));
                        menu.setName(rs.getString("NAME"));
                        menu.setPage(rs.getString("PAGE"));
                        menu.setNum(rs.getInt("NUM"));
                        menu.setParent(rs.getInt("PARENT"));
                        menu.setRoleId(rs.getInt("ROLE_ID"));
                        menu.setState(rs.getInt("STATE"));
                        subMenu.add(menu);
                    }
                    return subMenu;
                }
        );

        menuWithSubMenu.setSubMenu(subMenus);
        return menuWithSubMenu;
    }

    @Override
    public List<Menu> getList(int company) throws DataAccessException {
        return jdbcTemplate.query(SELECT_MENU_LIST,
                new Object[]{company, Menu.State.SYSTEM},
                (rs, rowNum) -> {
                    Menu menu = new Menu();
                    menu.setId(rs.getInt("ID"));
                    menu.setName(rs.getString("NAME"));
                    menu.setState(rs.getInt("STATE"));
                    return menu;
                }
        );
    }

    @Override
    public List<Menu> getUserMenu(int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USER_MENU_LIST,
                new Object[]{userId},
                rs -> {
                    List<Menu> menus = new LinkedList<>();
                    Map<Integer, List<Menu>> subMenus = new HashMap<>();
                    int oldParent = 0;
                    while(rs.next()){
                        Menu menu = new Menu();
                        int id = rs.getInt("ID");
                        menu.setId(id);
                        menu.setName(rs.getString("NAME"));
                        menu.setPage(rs.getString("PAGE"));
                        menu.setNum(rs.getInt("NUM"));
                        int parent = rs.getInt("PARENT");
                        menu.setParent(parent);
                        menu.setRoleId(rs.getInt("ROLE_ID"));
                        menu.setState(rs.getInt("STATE"));

                        if (parent != 0) {
                            if (parent != oldParent) {
                                subMenus.put(parent, new LinkedList<>());
                                oldParent = parent;
                            }
                            subMenus.get(parent).add(menu);
                        }
                        else {
                            menu.setSubMenu(subMenus.get(id));
                            menus.add(menu);
                        }
                    }
                    return menus;
                }
        );
    }

    @Override
    @Transactional
    public void create(Menu menu, int company) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_MENU, new String[] {"id"});
                    pstmt.setString(1, menu.getName());
                    pstmt.setString(2, menu.getPage());
                    pstmt.setInt(3, 1000);
                    pstmt.setInt(4, 0);
                    pstmt.setInt(5, menu.getRoleId());
                    pstmt.setInt(6, EXISTS);
                    pstmt.setInt(7, company);
                    return pstmt;
                }, keyHolder
        );
        jdbcTemplate.batchUpdate(INSERT_MENU,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setString(1, menu.getSubMenu().get(i).getName());
                        pstmt.setString(2, menu.getSubMenu().get(i).getPage());
                        pstmt.setInt(3, menu.getSubMenu().get(i).getNum());
                        pstmt.setInt(4, keyHolder.getKey().intValue());
                        pstmt.setInt(5, menu.getSubMenu().get(i).getRoleId());
                        pstmt.setInt(6, EXISTS);
                        pstmt.setInt(7, company);
                    }
                    public int getBatchSize() {
                        return menu.getSubMenu().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void update(Menu menu) throws DataAccessException {
        jdbcTemplate.update(UPDATE_MENU,
                menu.getName(),
                menu.getPage(),
                menu.getNum(),
                menu.getRoleId(),
                menu.getId()
        );
        jdbcTemplate.batchUpdate(UPDATE_MENU,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setString(1, menu.getSubMenu().get(i).getName());
                        pstmt.setString(2, menu.getSubMenu().get(i).getPage());
                        pstmt.setInt(3, menu.getSubMenu().get(i).getNum());
                        pstmt.setInt(4, menu.getSubMenu().get(i).getRoleId());
                        pstmt.setInt(5, menu.getSubMenu().get(i).getId());
                    }
                    public int getBatchSize() {
                        return menu.getSubMenu().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void delete(int id) throws DataAccessException {
        jdbcTemplate.update(DELETE_MENU, DELETED, id, id);
    }
}
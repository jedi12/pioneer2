package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RouteDao;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Route;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Repository(value = "routeDao")
public class RouteDaoImpl implements RouteDao {
    private static final String INSERT_ROUTE =
            "INSERT INTO DOC.ROUTES (NAME, STATE, COMPANY) VALUES (?, ?, ?)";
    private static final String INSERT_ROUTE_POINT =
            "INSERT INTO DOC.ROUTES_POINT (ID, STAGE, GROUP_ID) VALUES (?, ?, ?)";
    private static final String INSERT_ROUTE_GROUP =
            "INSERT INTO DOC.ROUTES_GROUP (ID, GROUP_ID) VALUES (?, ?)";
    private static final String UPDATE_ROUTE =
            "UPDATE DOC.ROUTES SET NAME = ? WHERE ID = ?";
    private static final String DELETE_ROUTE =
            "UPDATE DOC.ROUTES SET STATE = ? WHERE ID = ?";
    private static final String DELETE_ROUTE_POINT =
            "DELETE FROM DOC.ROUTES_POINT WHERE ID = ?";
    private static final String DELETE_ROUTE_GROUP =
            "DELETE FROM DOC.ROUTES_GROUP WHERE ID = ?";
    private static final String SELECT_ROUTE =
            "SELECT ID, NAME, STATE FROM DOC.ROUTES WHERE ID = ?";
    private static final String SELECT_ROUTE_POINT =
            "SELECT RP.ID AS ID, RP.STAGE AS STAGE, GROUP_ID, G.NAME AS GROUP_NAME, R.ID AS ROLE_ID, " +
                    "R.NAME AS ROLE_NAME FROM DOC.ROUTES_POINT RP LEFT JOIN DOC.GROUPS G ON RP.GROUP_ID = G.ID " +
                    "LEFT JOIN DOC.ROLES R ON G.ROLE_ID = R.ID WHERE RP.ID = ? ORDER BY RP.STAGE ASC, G.NAME ASC";
    private static final String SELECT_ROUTE_GROUP =
            "SELECT GROUP_ID, NAME FROM DOC.ROUTES_GROUP RG " +
                    "LEFT JOIN DOC.GROUPS G ON RG.GROUP_ID = G.ID WHERE RG.ID = ? ORDER BY NAME ASC";
    private static final String SELECT_ROUTE_LIST =
            "SELECT ID, NAME, STATE FROM DOC.ROUTES WHERE STATE > 0 AND COMPANY = ? OR STATE = ? ORDER BY STATE DESC, NAME ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Route get(int id) throws DataAccessException {
        Route resultRoute = jdbcTemplate.queryForObject(SELECT_ROUTE,
                new Object[]{id},
                (rs, rowNum) -> {
                    Route route = new Route();
                    route.setId(rs.getInt("ID"));
                    route.setName(rs.getString("NAME"));
                    route.setState(rs.getInt("STATE"));
                    return route;
                }
        );

        List<Route.Point> resultPoints = jdbcTemplate.query(SELECT_ROUTE_POINT,
                new Object[]{id},
                rs -> {
                    List<Route.Point> points = new LinkedList<>();
                    while(rs.next()){
                        Role role = new Role();
                        role.setId(rs.getInt("ROLE_ID"));
                        role.setName(rs.getString("ROLE_NAME"));

                        Group group = new Group();
                        group.setId(rs.getInt("GROUP_ID"));
                        group.setName(rs.getString("GROUP_NAME"));
                        group.setRole(role);

                        Route.Point point = new Route.Point();
                        point.setRouteId(rs.getInt("ID"));
                        point.setStage(rs.getInt("STAGE"));
                        point.setGroupId(rs.getInt("GROUP_ID"));
                        point.setGroup(group);

                        points.add(point);
                    }
                    return points;
                }
        );

        List<Group> resultGroups = jdbcTemplate.query(SELECT_ROUTE_GROUP,
                new Object[]{id},
                rs -> {
                    List<Group> groups = new LinkedList<>();
                    while(rs.next()){
                        Group group = new Group();
                        group.setId(rs.getInt("GROUP_ID"));
                        group.setName(rs.getString("NAME"));

                        groups.add(group);
                    }
                    return groups;
                }
        );

        resultRoute.setGroups(resultGroups);
        resultRoute.setPoints(resultPoints);
        return resultRoute;
    }

    @Override
    public List<Route> getList(int company) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ROUTE_LIST,
                new Object[]{company, Route.State.SYSTEM},
                (rs, rowNum) -> {
                    Route route = new Route();
                    route.setId(rs.getInt("ID"));
                    route.setName(rs.getString("NAME"));
                    route.setState(rs.getInt("STATE"));
                    return route;
                }
        );
    }

    @Override
    @Transactional
    public void create(Route route, int company) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_ROUTE, new String[] {"id"});
                    pstmt.setString(1, route.getName());
                    pstmt.setInt(2, Route.State.EXISTS);
                    pstmt.setInt(3, company);
                    return pstmt;
                }, keyHolder
        );

        jdbcTemplate.batchUpdate(INSERT_ROUTE_POINT,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setInt(2, route.getPoints().get(i).getStage());
                        pstmt.setInt(3, route.getPoints().get(i).getGroupId());
                    }
                    public int getBatchSize() {
                        return route.getPoints().size();
                    }
                }
        );

        jdbcTemplate.batchUpdate(INSERT_ROUTE_GROUP,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setInt(2, route.getGroups().get(i).getId());
                    }
                    public int getBatchSize() {
                        return route.getGroups().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void update(Route route) throws DataAccessException {
        jdbcTemplate.update(UPDATE_ROUTE,
                route.getName(),
                route.getId()
        );

        jdbcTemplate.update(DELETE_ROUTE_POINT,
                route.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_ROUTE_POINT,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, route.getId());
                        pstmt.setInt(2, route.getPoints().get(i).getStage());
                        pstmt.setInt(3, route.getPoints().get(i).getGroupId());
                    }
                    public int getBatchSize() {
                        return route.getPoints().size();
                    }
                }
        );

        jdbcTemplate.update(DELETE_ROUTE_GROUP,
                route.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_ROUTE_GROUP,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, route.getId());
                        pstmt.setInt(2, route.getGroups().get(i).getId());
                    }
                    public int getBatchSize() {
                        return route.getGroups().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void delete(int id) throws DataAccessException {
        jdbcTemplate.update(DELETE_ROUTE, Route.State.DELETED, id);
        jdbcTemplate.update(DELETE_ROUTE_POINT, id);
        jdbcTemplate.update(DELETE_ROUTE_GROUP, id);
    }
}
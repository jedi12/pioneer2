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
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Route;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository(value = "routeDao")
public class RouteDaoImpl implements RouteDao {
    private static final String INSERT_ROUTE =
            "INSERT INTO DOC.ROUTES (NAME, STATE, COMPANY) VALUES (?, ?, ?)";
    private static final String INSERT_ROUTE_POINT =
            "INSERT INTO DOC.ROUTES_POINT (ID, STAGE, GROUP_ID) VALUES (?, ?, ?)";
    private static final String INSERT_ROUTE_GROUP =
            "INSERT INTO DOC.ROUTES_GROUP (ID, GROUP_ID) VALUES (?, ?)";
    private static final String UPDATE_ROUTE =
            "UPDATE DOC.ROUTES SET NAME = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_ROUTE =
            "UPDATE DOC.ROUTES SET STATE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_ROUTE_POINT =
            "DELETE FROM DOC.ROUTES_POINT WHERE ID = ?";
    private static final String DELETE_ROUTE_GROUP =
            "DELETE FROM DOC.ROUTES_GROUP WHERE ID = ?";
    private static final String SELECT_ROUTE =
            "SELECT ID, NAME, STATE FROM DOC.ROUTES WHERE ID = ? AND COMPANY IN (0, ?)";
    private static final String SELECT_ROUTE_POINT =
            "SELECT RP.ID AS ID, RP.STAGE AS STAGE, GROUP_ID, G.NAME AS GROUP_NAME, R.ID AS ROLE_ID, " +
                    "R.NAME AS ROLE_NAME FROM DOC.ROUTES_POINT RP LEFT JOIN DOC.GROUPS G ON RP.GROUP_ID = G.ID " +
                    "LEFT JOIN DOC.ROLES R ON G.ROLE_ID = R.ID WHERE RP.ID = ? ORDER BY RP.STAGE ASC, G.NAME ASC";
    private static final String SELECT_ROUTE_GROUP =
            "SELECT GROUP_ID, NAME FROM DOC.ROUTES_GROUP RG " +
                    "LEFT JOIN DOC.GROUPS G ON RG.GROUP_ID = G.ID WHERE RG.ID = ? ORDER BY NAME ASC";
    private static final String SELECT_ROUTE_LIST =
            "SELECT ID, NAME, STATE FROM DOC.ROUTES WHERE STATE > 0 AND COMPANY = ? OR STATE = ? ORDER BY STATE DESC, NAME ASC";
    private static final String SELECT_USER_ROUTE_MAP =
            "SELECT DISTINCT R.ID AS ID, NAME FROM DOC.ROUTES R LEFT JOIN DOC.ROUTES_GROUP RG ON R.ID = RG.ID " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON RG.GROUP_ID = GU.ID WHERE COMPANY = ? AND USER_ID = ? " +
                    "AND STATE > 0 ORDER BY NAME ASC\n";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Route get(int routeId, int companyId) throws DataAccessException {
        Route resultRoute = jdbcTemplate.query(SELECT_ROUTE,
                new Object[]{routeId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        Route route = new Route();
                        route.setId(rs.getInt("ID"));
                        route.setName(rs.getString("NAME"));
                        route.setState(rs.getInt("STATE"));
                        return route;
                    } else {
                        throw new NotFoundDaoException("Not found Route with routeId = " + routeId +
                                " and companyId = " + companyId);
                    }
                }
        );

        List<Route.Point> resultPoints = jdbcTemplate.query(SELECT_ROUTE_POINT,
                new Object[]{routeId},
                rs -> {
                    List<Route.Point> points = new LinkedList<>();
                    while(rs.next()){
                        Route.Point point = new Route.Point();
                        point.setStage(rs.getInt("STAGE"));
                        point.setGroupId(rs.getInt("GROUP_ID"));
                        point.setGroupName(rs.getString("GROUP_NAME"));
                        point.setRoleId(rs.getInt("ROLE_ID"));
                        point.setRoleName(rs.getString("ROLE_NAME"));

                        points.add(point);
                    }
                    return points;
                }
        );

        List<Route.LinkGroup> resultGroups = jdbcTemplate.query(SELECT_ROUTE_GROUP,
                new Object[]{routeId},
                rs -> {
                    List<Route.LinkGroup> linkGroups = new LinkedList<>();
                    while(rs.next()){
                        Route.LinkGroup linkGroup = new Route.LinkGroup();
                        linkGroup.setGroupId(rs.getInt("GROUP_ID"));
                        linkGroup.setGroupName(rs.getString("NAME"));

                        linkGroups.add(linkGroup);
                    }
                    return linkGroups;
                }
        );

        resultRoute.setGroups(resultGroups);
        resultRoute.setPoints(resultPoints);
        return resultRoute;
    }

    @Override
    public List<Route> getList(int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ROUTE_LIST,
                new Object[]{companyId, Route.State.SYSTEM},
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
    public Map<String, Integer> getUserRouteMap(int companyId, int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USER_ROUTE_MAP,
                new Object[]{companyId, userId},
                rs -> {
                    Map<String, Integer> routes = new LinkedHashMap<>();
                    while(rs.next()){
                        routes.put(rs.getString("NAME"), rs.getInt("ID"));
                    }
                    return routes;
                }
        );
    }

    @Override
    @Transactional
    public void create(Route route, int companyId) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_ROUTE, new String[] {"id"});
                    pstmt.setString(1, route.getName());
                    pstmt.setInt(2, Route.State.EXISTS);
                    pstmt.setInt(3, companyId);
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
                        pstmt.setInt(2, route.getGroups().get(i).getGroupId());
                    }
                    public int getBatchSize() {
                        return route.getGroups().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void update(Route route, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_ROUTE,
                route.getName(),
                route.getId(),
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Route with routeId = " + route.getId() +
                    " and companyId = " + companyId);
        }

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
                        pstmt.setInt(2, route.getGroups().get(i).getGroupId());
                    }
                    public int getBatchSize() {
                        return route.getGroups().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void delete(int routeId, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(DELETE_ROUTE,
                Route.State.DELETED,
                routeId,
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Route with routeId = " + routeId +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_ROUTE_POINT,
                routeId
        );

        jdbcTemplate.update(DELETE_ROUTE_GROUP,
                routeId
        );
    }
}
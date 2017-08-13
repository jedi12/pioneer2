package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RouteProcessDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.RoutePoint;
import ru.pioneersystem.pioneer2.model.Status;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository(value = "routeProcessDao")
public class RouteProcessDaoImpl implements RouteProcessDao {
    private static final String SELECT_ROUTE =
            "SELECT SIGNED, G.NAME AS GROUP_NAME, G.ID AS GROUP_ID, U.NAME AS USER_NAME, U.ID AS USER_ID, " +
                    "RECEIPT_DATE, SIGN_DATE, SIGN_MESSAGE FROM DOC.DOCUMENTS_SIGN DS LEFT JOIN DOC.GROUPS G " +
                    "ON DS.GROUP_ID = G.ID LEFT JOIN DOC.USERS U ON DS.SIGN_USER = U.ID WHERE DS.ID = ? " +
                    "ORDER BY STAGE ASC, SIGN_DATE ASC";
    private static final String SELECT_CURRENT_ROUTE_POINT =
            "SELECT GROUP_ID FROM DOC.DOCUMENTS_SIGN WHERE ACTIVE = 1 AND ID = ?";
    private static final String INSERT_ROUTE_PROCESS =
            "INSERT INTO DOC.DOCUMENTS_SIGN (ID, STAGE, GROUP_ID, ROLE_ID, SIGNED, SIGN_DATE, SIGN_USER, " +
                    "SIGN_MESSAGE, ACTIVE, RECEIPT_DATE) SELECT ?, STAGE, GROUP_ID, ROLE_ID, 0, NULL, NULL, NULL," +
                    " 0, NULL FROM DOC.ROUTES_POINT RP, DOC.GROUPS G WHERE GROUP_ID = G.ID AND RP.ID = ?";
    private static final String DELETE_ROUTE_PROCESS =
            "DELETE FROM DOC.DOCUMENTS_SIGN WHERE ID = ?";
    private static final String SELECT_FIRST_ROUTE_STAGE =
            "SELECT STAGE, ROLE_ID FROM DOC.DOCUMENTS_SIGN WHERE ID = ? ORDER BY STAGE";
    private static final String UPDATE_FIRST_ROUTE_STAGE =
            "UPDATE DOC.DOCUMENTS_SIGN SET ACTIVE = 1, RECEIPT_DATE = ? WHERE ID = ? AND STAGE = ?";
    private static final String SELECT_ACTIVE_ROUTE_STAGE =
            "SELECT ID FROM DOC.DOCUMENTS_SIGN WHERE ACTIVE = 1 AND ID = ?";
    private static final String UPDATE_UNSIGNED_ROUTE_POINT =
            "UPDATE DOC.DOCUMENTS_SIGN SET SIGNED = ?, ACTIVE = 0, SIGN_DATE = ?, SIGN_USER = ?, " +
                    "SIGN_MESSAGE = ? WHERE SIGNED = 0 AND ID = ?";
    private static final String SELECT_ACTIVE_ROUTE_POINT =
            "SELECT STAGE FROM DOC.DOCUMENTS_SIGN WHERE ACTIVE = 1 AND GROUP_ID IN (" +
                    "SELECT ID FROM DOC.GROUPS_USER WHERE USER_ID = ?) AND ID = ?";
    private static final String SELECT_MIN_SIGNED_ROUTE_POINT =
            "SELECT MIN(SIGNED) AS MIN_SIGNED FROM DOC.DOCUMENTS_SIGN WHERE ACTIVE = 1 AND ID = ?";
    private static final String UPDATE_ACTIVE_ROUTE_POINT =
            "UPDATE DOC.DOCUMENTS_SIGN SET SIGNED = ?, SIGN_DATE = ?, SIGN_USER = ?, SIGN_MESSAGE = ? WHERE ACTIVE = 1 " +
                    "AND GROUP_ID IN (SELECT ID FROM DOC.GROUPS_USER WHERE  ACTOR_TYPE = 1 AND USER_ID = ?) AND ID = ?";
    private static final String UPDATE_ROUTE_ALL_STAGE =
            "UPDATE DOC.DOCUMENTS_SIGN SET ACTIVE = 0 WHERE ID = ?";
    private static final String DELETE_ROUTE_TAIL =
            "DELETE FROM DOC.DOCUMENTS_SIGN WHERE ID = ? AND STAGE > ?";
    private static final String INSERT_NEW_ROUTE_TAIL =
            "INSERT INTO DOC.DOCUMENTS_SIGN (ID, STAGE, GROUP_ID, ROLE_ID, SIGNED, SIGN_DATE, SIGN_USER, " +
                    "SIGN_MESSAGE, ACTIVE, RECEIPT_DATE) SELECT ?, STAGE + ?, GROUP_ID, ROLE_ID, 0, NULL, NULL, " +
                    "NULL, 0, NULL FROM DOC.ROUTES_POINT RP, DOC.GROUPS G WHERE GROUP_ID = G.ID AND RP.ID = ?";
    private static final String SELECT_NEXT_ROUTE_STAGE =
            "SELECT STAGE, ROLE_ID FROM DOC.DOCUMENTS_SIGN WHERE STAGE > ? AND ID = ? ORDER BY STAGE ASC";
    private static final String UPDATE_CURRENT_ROUTE_STAGE =
            "UPDATE DOC.DOCUMENTS_SIGN SET ACTIVE = 0 WHERE STAGE = ? AND ID = ?";
    private static final String UPDATE_NEXT_ROUTE_STAGE =
            "UPDATE DOC.DOCUMENTS_SIGN SET ACTIVE = 1, RECEIPT_DATE = ? WHERE STAGE = ? AND ID = ?";
    private static final String UPDATE_DOCUMENT_STATUS =
            "UPDATE DOC.DOCUMENTS SET STATUS = ?, U_DATE = ?, U_USER = ? WHERE ID = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<RoutePoint> getRoute(int documentId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ROUTE,
                new Object[]{documentId},
                (rs, rowNum) -> {
                    RoutePoint routePoint = new RoutePoint();
                    routePoint.setSigned(rs.getInt("SIGNED"));
                    routePoint.setGroupId(rs.getInt("GROUP_ID"));
                    routePoint.setGroupName(rs.getString("GROUP_NAME"));
                    routePoint.setSignUserId(rs.getInt("USER_ID"));
                    routePoint.setSignUserName(rs.getString("USER_NAME"));
                    Timestamp receiptDate = rs.getTimestamp("RECEIPT_DATE");
                    if (receiptDate != null) {
                        routePoint.setReceiptDate(Date.from(rs.getTimestamp("RECEIPT_DATE").toInstant()));
                    }
                    Timestamp signDate = rs.getTimestamp("SIGN_DATE");
                    if (signDate != null) {
                        routePoint.setSignDate(Date.from(rs.getTimestamp("SIGN_DATE").toInstant()));
                    }
                    routePoint.setSignMessage(rs.getString("SIGN_MESSAGE"));
                    return routePoint;
                }
        );
    }

    @Override
    public List<Integer> getCurrRoutePointGroups(int documentId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_CURRENT_ROUTE_POINT,
                new Object[]{documentId},
                (rs, rowNum) -> rs.getInt("GROUP_ID")
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void create(int documentId, int routeId) throws DataAccessException {
        jdbcTemplate.update(DELETE_ROUTE_PROCESS,
                documentId
        );

        jdbcTemplate.update(INSERT_ROUTE_PROCESS,
                documentId,
                routeId
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void start(int documentId, int userId) throws DataAccessException {
        RoutePoint tempVal = jdbcTemplate.query(SELECT_FIRST_ROUTE_STAGE,
                new Object[]{documentId},
                (rs) -> {
                    RoutePoint values = null;
                    if(rs.next()) {
                        int stage = rs.getInt("STAGE");
                        int roleId = rs.getInt("ROLE_ID");

                        values = new RoutePoint();
                        values.setStage(stage);
                        values.setRoleId(roleId);
                    }

                    return values;
                }
        );

        Date uDate = new Date();
        if (tempVal != null) {
            jdbcTemplate.update(UPDATE_FIRST_ROUTE_STAGE,
                    Timestamp.from(uDate.toInstant()),
                    documentId,
                    tempVal.getStage()
            );
        } else {
            tempVal = new RoutePoint();
            tempVal.setRoleId(Status.Id.COMPLETED);
        }

        jdbcTemplate.update(UPDATE_DOCUMENT_STATUS,
                tempVal.getRoleId(),
                Timestamp.from(uDate.toInstant()),
                userId,
                documentId
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void accept(int documentId, int userId, String message, boolean changeRoute, int newRouteId) throws DataAccessException {
        int currStage = jdbcTemplate.query(SELECT_ACTIVE_ROUTE_POINT,
                new Object[]{userId, documentId},
                (rs) -> {
                    if (!rs.next()) {
                        throw new NotFoundDaoException("Document with documentId = " + documentId + " is in wrong route point");
                    }
                    return rs.getInt("STAGE");
                }
        );

        Date uDate = new Date();
        jdbcTemplate.update(UPDATE_ACTIVE_ROUTE_POINT,
                RoutePoint.Signed.ACCEPTED,
                Timestamp.from(uDate.toInstant()),
                userId,
                message.trim().equals("") ? null : message.trim(),
                userId,
                documentId
        );

        if (changeRoute) {
            jdbcTemplate.update(DELETE_ROUTE_TAIL,
                    documentId,
                    currStage
            );

            jdbcTemplate.update(INSERT_NEW_ROUTE_TAIL,
                    documentId,
                    currStage,
                    newRouteId
            );
        }

        int routePointSignStatus = jdbcTemplate.query(SELECT_MIN_SIGNED_ROUTE_POINT,
                new Object[]{documentId},
                (rs) -> {
                    if (!rs.next()) {
                        throw new NotFoundDaoException("Active route point for Document with documentId = " +
                                documentId + " is in not found");
                    }
                    return rs.getInt("MIN_SIGNED");
                }
        );

        if (routePointSignStatus == RoutePoint.Signed.NOT_SIGNED) {
            return;
        }

        RoutePoint tempVal = jdbcTemplate.query(SELECT_NEXT_ROUTE_STAGE,
                new Object[]{currStage, documentId},
                (rs) -> {
                    RoutePoint values = null;
                    if(rs.next()) {
                        values = new RoutePoint();
                        values.setStage(rs.getInt("STAGE"));
                        values.setRoleId(rs.getInt("ROLE_ID"));
                    }
                    return values;
                }
        );

        if (tempVal != null) {
            jdbcTemplate.update(UPDATE_CURRENT_ROUTE_STAGE,
                    currStage,
                    documentId
            );

            jdbcTemplate.update(UPDATE_NEXT_ROUTE_STAGE,
                    Timestamp.from(uDate.toInstant()),
                    tempVal.getStage(),
                    documentId
            );

            jdbcTemplate.update(UPDATE_DOCUMENT_STATUS,
                    tempVal.getRoleId(),
                    Timestamp.from(uDate.toInstant()),
                    userId,
                    documentId
            );
        } else {
            jdbcTemplate.update(UPDATE_CURRENT_ROUTE_STAGE,
                    currStage,
                    documentId
            );

            jdbcTemplate.update(UPDATE_DOCUMENT_STATUS,
                    Status.Id.COMPLETED,
                    Timestamp.from(uDate.toInstant()),
                    userId,
                    documentId
            );
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void reject(int documentId, int userId, String message) throws DataAccessException {
        jdbcTemplate.query(SELECT_ACTIVE_ROUTE_POINT,
                new Object[]{userId, documentId},
                (rs) -> {
                    if (!rs.next()) {
                        throw new NotFoundDaoException("Document with documentId = " + documentId + " is in wrong route point");
                    }
                    return null;
                }
        );

        Date uDate = new Date();
        jdbcTemplate.update(UPDATE_ACTIVE_ROUTE_POINT,
                RoutePoint.Signed.REJECTED,
                Timestamp.from(uDate.toInstant()),
                userId,
                message,
                userId,
                documentId
        );

        jdbcTemplate.update(UPDATE_ROUTE_ALL_STAGE,
                documentId
        );

        jdbcTemplate.update(UPDATE_DOCUMENT_STATUS,
                Status.Id.CANCELED,
                Timestamp.from(uDate.toInstant()),
                userId,
                documentId
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void cancel(int documentId, int userId, String message) throws DataAccessException {
        jdbcTemplate.query(SELECT_ACTIVE_ROUTE_STAGE,
                new Object[]{documentId},
                (rs) -> {
                    if (!rs.next()) {
                        throw new NotFoundDaoException("Document with documentId = " + documentId + " is in finished state already");
                    }
                    return null;
                }
        );

        Date uDate = new Date();
        jdbcTemplate.update(UPDATE_UNSIGNED_ROUTE_POINT,
                RoutePoint.Signed.CANCELED,
                Timestamp.from(uDate.toInstant()),
                userId,
                message,
                documentId
        );

        jdbcTemplate.update(UPDATE_DOCUMENT_STATUS,
                Status.Id.CANCELED,
                Timestamp.from(uDate.toInstant()),
                userId,
                documentId
        );
    }
}
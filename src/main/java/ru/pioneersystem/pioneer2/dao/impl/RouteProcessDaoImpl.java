package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RouteProcessDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Status;

import java.sql.Timestamp;
import java.util.Date;

@Repository(value = "routeProcessDao")
public class RouteProcessDaoImpl implements RouteProcessDao {
    private static final String INSERT_ROUTE_PROCESS =
            "INSERT INTO DOC.DOCUMENTS_SIGN (ID, STAGE, GROUP_ID, ROLE_ID, SIGNED, SIGN_DATE, SIGN_USER, " +
                    "SIGN_MESSAGE, ACTIVE, RECEIPT_DATE) SELECT ?, STAGE, GROUP_ID, ROLE_ID, 0, NULL, NULL, NULL, 0, " +
                    "NULL FROM DOC.ROUTES_POINT RP, DOC.GROUPS G WHERE GROUP_ID = G.ID AND RP.ID = ?";
    private static final String DELETE_ROUTE_PROCESS = "DELETE FROM DOC.DOCUMENTS_SIGN WHERE ID = ?";
    private static final String START_ROUTE_PROCESS1 =
            "SELECT STAGE, ROLE_ID FROM DOC.DOCUMENTS_SIGN WHERE ID = ? ORDER BY STAGE";
    private static final String START_ROUTE_PROCESS2 =
            "UPDATE DOC.DOCUMENTS_SIGN SET ACTIVE = 1, RECEIPT_DATE = ? WHERE ID = ? AND STAGE = ?";
    private static final String START_ROUTE_PROCESS3 =
            "UPDATE DOC.DOCUMENTS SET STATUS = ?, U_DATE = ? WHERE ID = ?";
    private static final String CANCEL_ROUTE_PROCESS1 =
            "SELECT ID FROM DOC.DOCUMENTS WHERE STATUS > 9 AND ID = ?";
    private static final String CANCEL_ROUTE_PROCESS2 =
            "UPDATE DOC.DOCUMENTS_SIGN SET SIGNED = ?, ACTIVE = 0, SIGN_DATE = ?, SIGN_USER = ?, SIGN_MESSAGE = ? " +
                    "WHERE SIGNED = 0 AND ID = ?";
    private static final String CANCEL_ROUTE_PROCESS3 =
            "UPDATE DOC.DOCUMENTS SET STATUS = ?, U_DATE = ?, U_USER = ? WHERE ID = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
    public void start(int documentId) throws DataAccessException {
        Document.RoutePoint tempVal = jdbcTemplate.query(START_ROUTE_PROCESS1,
                new Object[]{documentId},
                (rs) -> {
                    Document.RoutePoint values = null;
                    if(rs.next()) {
                        int stage = rs.getInt("STAGE");
                        int roleId = rs.getInt("ROLE_ID");

                        values = new Document.RoutePoint();
                        values.setStage(stage);
                        values.setRoleId(roleId);
                    }

                    return values;
                }
        );

        Date uDate = new Date();
        if (tempVal != null) {
            jdbcTemplate.update(START_ROUTE_PROCESS2,
                    Timestamp.from(uDate.toInstant()),
                    documentId,
                    tempVal.getStage()
            );
        } else {
            tempVal.setRoleId(Status.Id.COMPLETED);
        }

        jdbcTemplate.update(START_ROUTE_PROCESS3,
                tempVal.getRoleId(),
                Timestamp.from(uDate.toInstant()),
                documentId
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void cancel(int documentId, int userId, String message) throws DataAccessException {
        jdbcTemplate.query(CANCEL_ROUTE_PROCESS1,
                new Object[]{documentId},
                (rs) -> {
                    if (!rs.next()) {
                        throw new NotFoundDaoException("Document with documentId = " + documentId + " is in finished state already");
                    }
                }
        );

        Date uDate = new Date();
        jdbcTemplate.update(CANCEL_ROUTE_PROCESS2,
                Document.RoutePoint.Signed.CANCELED,
                Timestamp.from(uDate.toInstant()),
                userId,
                message,
                documentId
        );

        jdbcTemplate.update(CANCEL_ROUTE_PROCESS3,
                Status.Id.CANCELED,
                Timestamp.from(uDate.toInstant()),
                userId,
                documentId
        );
    }
}
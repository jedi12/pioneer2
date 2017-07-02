package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RouteProcessDao;
import ru.pioneersystem.pioneer2.dao.exception.LockException;
import ru.pioneersystem.pioneer2.model.Document;

import java.util.Date;

@Repository(value = "routeProcessDao")
public class RouteProcessDaoImpl implements RouteProcessDao {
    private static final String INSERT_ROUTE_PROCESS =
            "INSERT INTO DOC.DOCUMENTS_SIGN (ID, STAGE, GROUP_ID, ROLE_ID, SIGNED, SIGN_DATE, SIGN_USER, " +
                    "SIGN_MESSAGE, ACTIVE, RECEIPT_DATE) SELECT ?, STAGE, GROUP_ID, ROLE_ID, 0, NULL, NULL, NULL, 0, " +
                    "NULL FROM DOC.ROUTES_POINT RP, DOC.GROUPS G WHERE GROUP_ID = G.ID AND RP.ID = ?";
    private static final String DELETE_ROUTE_PROCESS = "DELETE FROM DOC.DOCUMENTS_SIGN WHERE ID = ?";
    private static final String LOCK_DOCUMENT = "SELECT U_DATE, U_USER FROM DOC.DOCUMENTS WHERE ID = ? FOR UPDATE";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void create(Document document, int routeId) throws DataAccessException, LockException {
        Document tempVal = jdbcTemplate.queryForObject(LOCK_DOCUMENT,
                new Object[]{document.getId()},
                (rs, rowNum) -> {
                    Date uDate = Date.from(rs.getTimestamp("U_DATE").toInstant());
                    int uUserId = rs.getInt("U_USER");

                    if (document.getChangeDate().compareTo(uDate) != 0) {
                        Document values = new Document();
                        values.setChangeDate(uDate);
                        values.setChangeUserId(uUserId);
                        return values;
                    }
                    return null;
                }
        );

        if (tempVal != null) {
            throw new LockException("Current Document has already changed by another user",
                    tempVal.getChangeUserId(), tempVal.getChangeDate());
        }

        jdbcTemplate.update(DELETE_ROUTE_PROCESS,
                document.getId()
        );

        jdbcTemplate.update(INSERT_ROUTE_PROCESS,
                document.getId(),
                routeId
        );
    }
}
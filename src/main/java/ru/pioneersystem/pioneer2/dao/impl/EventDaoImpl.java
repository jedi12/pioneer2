package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.EventDao;
import ru.pioneersystem.pioneer2.dao.exception.TooManyRowsDaoException;
import ru.pioneersystem.pioneer2.model.Event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Repository(value = "eventDao")
public class EventDaoImpl implements EventDao {
    private static final String INSERT_EVENT =
            "INSERT INTO DOC.EVENTS (E_DATE, E_USER, E_TYPE, OBJ_ID, DETAIL1, DETAIL2, COMPANY) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ADMIN_EVENT_LIST =
            "SELECT E_DATE, E_USER, LOGIN, E_TYPE, OBJ_ID, DETAIL1 FROM DOC.EVENTS E " +
                    "LEFT JOIN DOC.USERS U ON U.ID = E.E_USER " +
                    "WHERE E_DATE >= ? AND E_DATE < ? AND E.COMPANY = ? ORDER BY E_DATE ASC";
    private static final String SELECT_SUPER_EVENT_LIST =
            "SELECT E_DATE, E_USER, LOGIN, E_TYPE, OBJ_ID, DETAIL1 FROM DOC.EVENTS E " +
                    "LEFT JOIN DOC.USERS U ON U.ID = E.E_USER " +
                    "WHERE E_DATE >= ? AND E_DATE < ? ORDER BY E_DATE ASC";
    private static final String SELECT_EVENT_DETAIL =
            "SELECT DETAIL2 FROM DOC.EVENTS WHERE E_DATE = ? AND E_USER = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> getAdminList(Date beginDate, Date endDate, int companyId) throws DataAccessException {
        List<Object> params = new ArrayList<>(Arrays.asList(beginDate, endDate, companyId));
        return getList(SELECT_ADMIN_EVENT_LIST, params);
    }

    @Override
    public List<Event> getSuperList(Date beginDate, Date endDate) throws DataAccessException {
        List<Object> params = new ArrayList<>(Arrays.asList(beginDate, endDate));
        return getList(SELECT_SUPER_EVENT_LIST, params);
    }

    private List<Event> getList(String query, List<Object> params) throws DataAccessException {
        return jdbcTemplate.query(query, params.toArray(),
                (rs) -> {
                    int count = 0;
                    List<Event> events = new ArrayList<>();
                    while(rs.next()){
                        Event event = new Event();
                        event.setDate(Date.from(rs.getTimestamp("E_DATE").toInstant()));
                        event.setUserId(rs.getInt("E_USER"));
                        event.setUserLogin(rs.getString("LOGIN"));
                        event.setTypeId(rs.getInt("E_TYPE"));
                        event.setObjectId(rs.getInt("OBJ_ID"));
                        event.setDetail1(rs.getString("DETAIL1"));

                        events.add(event);
                        count = count + 1;

                        if (count >= 10000) {
                            throw new TooManyRowsDaoException("Over 10000 events found", events);
                        }
                    }

                    return events;
                }
        );
    }

    @Override
    public String getDetail(Date eventDate, int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_EVENT_DETAIL,
                new Object[]{eventDate, userId},
                (rs) -> {
                    if (rs.next()) {
                        return rs.getString("DETAIL2");
                    }
                    return null;
                }
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(Event event, int companyId) throws DataAccessException {
        jdbcTemplate.update(INSERT_EVENT,
                Timestamp.from(event.getDate().toInstant()),
                event.getUserId(),
                event.getTypeId(),
                event.getObjectId(),
                event.getDetail1(),
                event.getDetail2(),
                companyId
        );
    }
}
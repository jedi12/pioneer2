package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.MailProcessDao;
import ru.pioneersystem.pioneer2.dao.exception.TooManyRowsDaoException;
import ru.pioneersystem.pioneer2.model.Notice;
import ru.pioneersystem.pioneer2.model.Event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Repository(value = "mailProcessDao")
public class MailProcessDaoImpl implements MailProcessDao {
    private static final String SELECT_INFO =
            "SELECT INFO FROM DOC.EMAIL_SENDED WHERE ID = ?";
    private static final String SELECT_ADMIN_EMAIL_SEND_LIST =
            "SELECT ES.ID AS ID, EMAIL, STATUS, DOC, TRY, U_DATE, DOC_STATUS_ID, NAME AS DOC_STATUS_NAME, EVENT " +
                    "FROM DOC.EMAIL_SENDED ES LEFT JOIN DOC.DOCUMENTS_STATUS DS ON DS.ID = ES.DOC_STATUS_ID " +
                    "WHERE U_DATE >= ? AND U_DATE < ? AND ES.COMPANY = ? ORDER BY U_DATE ASC";
    private static final String SELECT_SUPER_EMAIL_SEND_LIST =
            "SELECT ES.ID AS ID, EMAIL, STATUS, DOC, TRY, U_DATE, DOC_STATUS_ID, NAME AS DOC_STATUS_NAME, EVENT " +
                    "FROM DOC.EMAIL_SENDED ES LEFT JOIN DOC.DOCUMENTS_STATUS DS ON DS.ID = ES.DOC_STATUS_ID " +
                    "WHERE U_DATE >= ? AND U_DATE < ? ORDER BY U_DATE ASC";
    private static final String INSERT_NOTICE_DOCUMENT_IN =
            "INSERT INTO DOC.EMAIL_SENDED (EMAIL, STATUS, DOC, TRY, U_DATE, DOC_STATUS_ID, EVENT, COMPANY) (" +
                    "SELECT EMAIL, ?, DS.ID, 0, ?, ROLE_ID, ?, ? FROM DOC.DOCUMENTS_SIGN DS " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON GU.ID = DS.GROUP_ID LEFT JOIN DOC.USERS U ON U.ID = GU.USER_ID " +
                    "WHERE ACTIVE = 1 AND NOTICE_DOC_IN = 1 AND U.STATE = 1 AND DS.ID = ?)";
    private static final String INSERT_NOTICE_STATUS_CHANGED =
            "INSERT INTO DOC.EMAIL_SENDED (EMAIL, STATUS, DOC, TRY, U_DATE, DOC_STATUS_ID, EVENT, COMPANY) (" +
                    "SELECT EMAIL, ?, D.ID, 0, ?, STATUS, ?, ? FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON GU.ID = D.DOC_GROUP LEFT JOIN DOC.USERS U ON U.ID = GU.USER_ID " +
                    "WHERE NOTICE_STATUS_CH = 1 AND U.STATE = 1 AND D.ID = ?)";
    private static final String SELECT_NOTICES_FOR_SENDING =
            "SELECT ES.ID AS ES_ID, EMAIL, DOC, DOC_STATUS_ID, DS.NAME AS DOC_STATUS_NAME, TRY, EVENT, " +
                    "D.NAME AS DOC_NAME, G.NAME AS DOC_GROUP_NAME, M.ID AS MENU_ID FROM DOC.EMAIL_SENDED ES " +
                    "LEFT JOIN DOC.DOCUMENTS_STATUS DS ON DS.ID = ES.DOC_STATUS_ID " +
                    "LEFT JOIN DOC.DOCUMENTS D ON D.ID = ES.DOC LEFT JOIN DOC.GROUPS G ON G.ID = D.DOC_GROUP " +
                    "LEFT JOIN DOC.MENU M ON M.ROLE_ID = ES.DOC_STATUS_ID " +
                    "WHERE ES.STATUS = 0 AND TRY <= 3 ORDER BY ES.ID";
    private static final String UPDATE_SEND_INFO =
            "UPDATE DOC.EMAIL_SENDED SET STATUS = ?, TRY = ?, U_DATE = ?, INFO = ? WHERE ID = ?";
    private static final String UPDATE_DELIVERY_STATUS =
            "UPDATE DOC.EMAIL_SENDED SET STATUS = ?, U_DATE = ?, INFO = ? WHERE ID = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Notice> getAdminList(Date beginDate, Date endDate, int companyId) throws DataAccessException {
        List<Object> params = new ArrayList<>(Arrays.asList(beginDate, endDate, companyId));
        return getList(SELECT_ADMIN_EMAIL_SEND_LIST, params);
    }

    @Override
    public List<Notice> getSuperList(Date beginDate, Date endDate) throws DataAccessException {
        List<Object> params = new ArrayList<>(Arrays.asList(beginDate, endDate));
        return getList(SELECT_SUPER_EMAIL_SEND_LIST, params);
    }

    private List<Notice> getList(String query, List<Object> params) throws DataAccessException {
        return jdbcTemplate.query(query, params.toArray(),
                (rs) -> {
                    int count = 0;
                    List<Notice> notices = new ArrayList<>();
                    while(rs.next()){
                        Notice notice = new Notice();
                        notice.setId(rs.getInt("ID"));
                        notice.setEmail(rs.getString("EMAIL"));
                        notice.setSendStatusId(rs.getInt("STATUS"));
                        notice.setDocId(rs.getInt("DOC"));
                        notice.setAttempt(rs.getInt("TRY"));
                        notice.setChangeDate(Date.from(rs.getTimestamp("U_DATE").toInstant()));
                        notice.setDocStatusId(rs.getInt("DOC_STATUS_ID"));
                        notice.setDocStatusName(rs.getString("DOC_STATUS_NAME"));
                        notice.setEventId(rs.getInt("EVENT"));

                        notices.add(notice);
                        count = count + 1;

                        if (count >= 10000) {
                            throw new TooManyRowsDaoException("Over 10000 email send info found", notices);
                        }
                    }

                    return notices;
                }
        );
    }

    @Override
    public String getInfo(int noticeId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_INFO,
                new Object[]{noticeId},
                (rs) -> {
                    if (rs.next()) {
                        return rs.getString("INFO");
                    }
                    return null;
                }
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void prepareNotice(int documentId, int companyId) throws DataAccessException {
        Date currDate = new Date();

        jdbcTemplate.update(INSERT_NOTICE_DOCUMENT_IN,
                Notice.Status.PREPARED_TO_SENDED,
                Timestamp.from(currDate.toInstant()),
                Event.Type.NOTICE_DOC_RECEIVED,
                companyId,
                documentId
        );

        jdbcTemplate.update(INSERT_NOTICE_STATUS_CHANGED,
                Notice.Status.PREPARED_TO_SENDED,
                Timestamp.from(currDate.toInstant()),
                Event.Type.NOTICE_DOC_STATUS_CHANGED,
                companyId,
                documentId
        );
    }

    @Override
    public List<Notice> getForSendList() throws DataAccessException {
        return jdbcTemplate.query(SELECT_NOTICES_FOR_SENDING,
                (rs, rowNum) -> {
                    Notice notice = new Notice();
                    notice.setId(rs.getInt("ES_ID"));
                    notice.setEmail(rs.getString("EMAIL"));
                    notice.setDocId(rs.getInt("DOC"));
                    notice.setDocStatusId(rs.getInt("DOC_STATUS_ID"));
                    notice.setDocStatusName(rs.getString("DOC_STATUS_NAME"));
                    notice.setAttempt(rs.getInt("TRY"));
                    notice.setEventId(rs.getInt("EVENT"));
                    notice.setDocName(rs.getString("DOC_NAME"));
                    notice.setDocGroupName(rs.getString("DOC_GROUP_NAME"));
                    notice.setMenuId(rs.getInt("MENU_ID"));
                    return notice;
                }
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateEmailSend(Notice notice) throws DataAccessException {
        jdbcTemplate.update(UPDATE_SEND_INFO,
                notice.getSendStatusId(),
                notice.getAttempt(),
                notice.getChangeDate(),
                notice.getInfo(),
                notice.getId()
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateDeliveryStatus(Notice notice) throws DataAccessException {
        jdbcTemplate.update(UPDATE_DELIVERY_STATUS,
                notice.getSendStatusId(),
                notice.getChangeDate(),
                notice.getInfo(),
                notice.getId()
        );
    }
}
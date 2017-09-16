package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.MailProcessDao;
import ru.pioneersystem.pioneer2.model.EmailSendInfo;
import ru.pioneersystem.pioneer2.model.Event;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository(value = "mailProcessDao")
public class MailProcessDaoImpl implements MailProcessDao {
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
    public List getList() throws DataAccessException {
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void prepareNotice(int documentId, int companyId) throws DataAccessException {
        Date currDate = new Date();

        jdbcTemplate.update(INSERT_NOTICE_DOCUMENT_IN,
                EmailSendInfo.SendStatus.NOT_SENDED,
                Timestamp.from(currDate.toInstant()),
                Event.Type.NOTICE_DOC_RECEIVED,
                companyId,
                documentId
        );

        jdbcTemplate.update(INSERT_NOTICE_STATUS_CHANGED,
                EmailSendInfo.SendStatus.NOT_SENDED,
                Timestamp.from(currDate.toInstant()),
                Event.Type.NOTICE_DOC_STATUS_CHANGED,
                companyId,
                documentId
        );
    }

    @Override
    public List<EmailSendInfo> getForSendList() throws DataAccessException {
        return jdbcTemplate.query(SELECT_NOTICES_FOR_SENDING,
                (rs, rowNum) -> {
                    EmailSendInfo emailSendInfo = new EmailSendInfo();
                    emailSendInfo.setId(rs.getInt("ES_ID"));
                    emailSendInfo.setEmail(rs.getString("EMAIL"));
                    emailSendInfo.setDocId(rs.getInt("DOC"));
                    emailSendInfo.setDocStatusId(rs.getInt("DOC_STATUS_ID"));
                    emailSendInfo.setDocStatusName(rs.getString("DOC_STATUS_NAME"));
                    emailSendInfo.setAttempt(rs.getInt("TRY"));
                    emailSendInfo.setEventId(rs.getInt("EVENT"));
                    emailSendInfo.setDocName(rs.getString("DOC_NAME"));
                    emailSendInfo.setDocGroupName(rs.getString("DOC_GROUP_NAME"));
                    emailSendInfo.setMenuId(rs.getInt("MENU_ID"));
                    return emailSendInfo;
                }
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateNoticeInfo(EmailSendInfo emailSendInfo) throws DataAccessException {
        jdbcTemplate.update(UPDATE_SEND_INFO,
                emailSendInfo.getSendStatusId(),
                emailSendInfo.getAttempt(),
                emailSendInfo.getChangeDate(),
                emailSendInfo.getInfo(),
                emailSendInfo.getId()
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateDeliveryStatus(EmailSendInfo emailSendInfo) throws DataAccessException {
        jdbcTemplate.update(UPDATE_DELIVERY_STATUS,
                emailSendInfo.getSendStatusId(),
                emailSendInfo.getChangeDate(),
                emailSendInfo.getInfo(),
                emailSendInfo.getId()
        );
    }
}
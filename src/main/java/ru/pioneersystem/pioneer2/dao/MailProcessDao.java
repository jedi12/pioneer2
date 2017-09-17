package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Notice;

import java.util.Date;
import java.util.List;

public interface MailProcessDao {

    List<Notice> getAdminList(Date beginDate, Date endDate, int companyId) throws DataAccessException;

    List<Notice> getSuperList(Date beginDate, Date endDate) throws DataAccessException;

    String getInfo(int noticeId) throws DataAccessException;

    void prepareNotice(int documentId, int companyId) throws DataAccessException;

    List<Notice> getForSendList() throws DataAccessException;

    void updateEmailSend(Notice notice) throws DataAccessException;

    void updateDeliveryStatus(Notice notice) throws DataAccessException;
}
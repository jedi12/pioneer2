package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.EmailSendInfo;

import java.util.List;

public interface MailProcessDao {

    List getList() throws DataAccessException;

    void prepareNotice(int documentId, int companyId) throws DataAccessException;

    List<EmailSendInfo> getForSendList() throws DataAccessException;

    void updateNoticeInfo(EmailSendInfo emailSendInfo) throws DataAccessException;

    void updateDeliveryStatus(EmailSendInfo emailSendInfo) throws DataAccessException;
}
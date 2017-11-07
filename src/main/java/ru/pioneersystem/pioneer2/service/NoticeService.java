package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Notice;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.Date;
import java.util.List;

public interface NoticeService {

    List<Notice> getNoticeList(Date fromDate, Date toDate) throws ServiceException;

    String getNoticeInfo(int noticeId) throws ServiceException;
}
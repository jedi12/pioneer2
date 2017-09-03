package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.Date;
import java.util.List;

public interface EventService {

    void logError(String detail1, String detail2);

    void logError(String detail1, String detail2, int objectId);

    void logEvent(int eventType, int objectId);

    void logEvent(int eventType, int objectId, String detail1);

    void logEvent(int eventType, int objectId, String detail1, String detail2);

    List<Event> getEventList(Date fromDate, Date toDate) throws ServiceException;

    String getEventDetail(Date eventDate, int eventUser) throws ServiceException;
}
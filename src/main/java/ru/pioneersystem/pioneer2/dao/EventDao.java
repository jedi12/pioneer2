package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Event;

import java.util.Date;
import java.util.List;

public interface EventDao {

    List<Event> getAdminList(Date beginDate, Date endDate, int companyId) throws DataAccessException;

    List<Event> getSuperList(Date beginDate, Date endDate) throws DataAccessException;

    String getDetail(Date eventDate, int userId, int companyId) throws DataAccessException;

    void create(Event event, int companyId) throws DataAccessException;
}
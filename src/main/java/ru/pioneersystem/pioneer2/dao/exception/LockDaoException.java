package ru.pioneersystem.pioneer2.dao.exception;

import org.springframework.dao.DataAccessException;

import java.util.Date;

public class LockDaoException extends DataAccessException {
    private int userId;
    private Date date;

    public LockDaoException(String message, int userId, Date date) {
        super(message);
        this.userId = userId;
        this.date = date;
    }

    public int getUserId() {
        return userId;
    }

    public Date getDate() {
        return date;
    }
}

package ru.pioneersystem.pioneer2.dao.exception;

import java.util.Date;

public class LockDaoException extends Exception {
    private int userId;
    private Date date;

    public LockDaoException(int userId, Date date) {
        super();
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

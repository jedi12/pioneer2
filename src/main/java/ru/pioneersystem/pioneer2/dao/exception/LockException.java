package ru.pioneersystem.pioneer2.dao.exception;

import java.util.Date;

public class LockException extends Exception {
    private int userId;
    private Date date;

    public LockException(String message, int userId, Date date) {
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

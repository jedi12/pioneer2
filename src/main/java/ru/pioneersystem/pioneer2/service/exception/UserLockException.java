package ru.pioneersystem.pioneer2.service.exception;

import ru.pioneersystem.pioneer2.model.User;

import java.util.Date;

public class UserLockException extends Exception {
    private User user;
    private Date date;

    public UserLockException(User user, Date date, Throwable cause) {
        super(cause);
        this.user = user;
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public Date getDate() {
        return date;
    }
}

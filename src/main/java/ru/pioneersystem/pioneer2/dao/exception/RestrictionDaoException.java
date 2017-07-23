package ru.pioneersystem.pioneer2.dao.exception;

import org.springframework.dao.DataAccessException;

public class RestrictionDaoException extends DataAccessException {
    public RestrictionDaoException(String msg) {
        super(msg);
    }

    public RestrictionDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}

package ru.pioneersystem.pioneer2.dao.exception;

import org.springframework.dao.DataAccessException;

public class NotFoundDaoException extends DataAccessException {
    public NotFoundDaoException(String message) {
        super(message);
    }
}

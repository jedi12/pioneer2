package ru.pioneersystem.pioneer2.dao.exception;

import org.springframework.dao.DataAccessException;

import java.util.List;

public class TooManyRowsDaoException extends DataAccessException {
    private List objects;

    public TooManyRowsDaoException(String message, List objects) {
        super(message);
        this.objects = objects;
    }

    public List getObject() {
        return objects;
    }
}

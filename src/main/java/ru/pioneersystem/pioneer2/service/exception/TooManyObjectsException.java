package ru.pioneersystem.pioneer2.service.exception;

import java.util.List;

public class TooManyObjectsException extends ServiceException {
    private List objects;

    public TooManyObjectsException(String message, List objects) {
        super(message);
        this.objects = objects;
    }

    public List getObjects() {
        return objects;
    }
}

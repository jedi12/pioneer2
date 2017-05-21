package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.FieldType;
import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface FieldTypeService {
    FieldType getFieldType(int id) throws ServiceException;

    List<FieldType> getFieldTypeList() throws ServiceException;

    Map<Integer, FieldType> getFieldTypeMap() throws ServiceException;

    Template setLocalizedFieldTypeName(Template template);
}
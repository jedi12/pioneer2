package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.FieldTypeDao;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.FieldType;
import ru.pioneersystem.pioneer2.model.Template;
import ru.pioneersystem.pioneer2.service.FieldTypeService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("fieldTypeService")
public class FieldTypeServiceImpl implements FieldTypeService {
    private Logger log = LoggerFactory.getLogger(FieldTypeServiceImpl.class);

    private FieldTypeDao fieldTypeDao;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public FieldTypeServiceImpl(FieldTypeDao fieldTypeDao, LocaleBean localeBean, MessageSource messageSource) {
        this.fieldTypeDao = fieldTypeDao;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public FieldType getFieldType(int id) throws ServiceException {
        try {
            return setLocalizedFieldTypeTypeName(setLocalizedFieldTypeName(fieldTypeDao.get(id)));
        } catch (DataAccessException e) {
            log.error("Can't get FieldType by id", e);
            throw new ServiceException("Can't get FieldType by id", e);
        }
    }

    @Override
    public List<FieldType> getFieldTypeList() throws ServiceException {
        try {
            List<FieldType> fieldTypes = fieldTypeDao.getList();
            for (FieldType fieldType : fieldTypes) {
                setLocalizedFieldTypeTypeName(setLocalizedFieldTypeName(fieldType));
            }
            return fieldTypes;
        } catch (DataAccessException e) {
            log.error("Can't get list of FieldType", e);
            throw new ServiceException("Can't get list of FieldType", e);
        }
    }

    @Override
    public Map<Integer, FieldType> getFieldTypeMap() throws ServiceException {
        Map<Integer, FieldType> fieldTypeMap = new LinkedHashMap<>();
        for (FieldType fieldType : getFieldTypeList()) {
            fieldTypeMap.put(fieldType.getId(), fieldType);
        }
        return fieldTypeMap;
    }

    public Template setLocalizedFieldTypeName(Template template) {
        for (Document.Field field: template.getFields()) {
            field.setTypeName(getLocalizedFieldTypeName(field.getTypeId()));
            if (field.getTypeId() == FieldType.Id.LIST) {
                field.setTypeName(field.getTypeName() + " (" + field.getChoiceListName() + ")");
            }
        }
        return template;
    }

    private FieldType setLocalizedFieldTypeName(FieldType fieldType) {
        fieldType.setTypeName(getLocalizedFieldTypeName(fieldType.getTypeId()));
        return fieldType;
    }

    private String getLocalizedFieldTypeName(int fieldType) {
        switch (fieldType) {
            case FieldType.Id.TEXT_STRING:
                return messageSource.getMessage("fieldType.name.textString", null, localeBean.getLocale());
            case FieldType.Id.LIST:
                return messageSource.getMessage("fieldType.name.list", null, localeBean.getLocale());
            case FieldType.Id.CALENDAR:
                return messageSource.getMessage("fieldType.name.calendar", null, localeBean.getLocale());
            case FieldType.Id.CHECKBOX:
                return messageSource.getMessage("fieldType.name.checkbox", null, localeBean.getLocale());
            case FieldType.Id.TEXT_AREA:
                return messageSource.getMessage("fieldType.name.textArea", null, localeBean.getLocale());
            case FieldType.Id.FILE:
                return messageSource.getMessage("fieldType.name.file", null, localeBean.getLocale());
            case FieldType.Id.LINE:
                return messageSource.getMessage("fieldType.name.line", null, localeBean.getLocale());
            case FieldType.Id.EMPTY_STRING:
                return messageSource.getMessage("fieldType.name.emptyString", null, localeBean.getLocale());
            case FieldType.Id.TITLE:
                return messageSource.getMessage("fieldType.name.title", null, localeBean.getLocale());
            default:
                return "Unknown";
        }
    }

    private FieldType setLocalizedFieldTypeTypeName(FieldType fieldType) {
        switch (fieldType.getTypeId()) {
            case FieldType.Type.INPUT:
                fieldType.setTypeName(messageSource.getMessage("fieldType.type.input", null, localeBean.getLocale()));
                break;
            case FieldType.Type.DECORATE:
                fieldType.setTypeName(messageSource.getMessage("fieldType.type.decorate", null, localeBean.getLocale()));
                break;
            default:
                fieldType.setTypeName("Unknown");
        }
        return fieldType;
    }
}

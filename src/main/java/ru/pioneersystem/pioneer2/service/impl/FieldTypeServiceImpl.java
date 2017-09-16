package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.FieldTypeDao;
import ru.pioneersystem.pioneer2.model.FieldType;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.FieldTypeService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("fieldTypeService")
public class FieldTypeServiceImpl implements FieldTypeService {
    private EventService eventService;
    private FieldTypeDao fieldTypeDao;
    private DictionaryService dictionaryService;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public FieldTypeServiceImpl(EventService eventService, FieldTypeDao fieldTypeDao,
                                DictionaryService dictionaryService, LocaleBean localeBean,
                                MessageSource messageSource) {
        this.eventService = eventService;
        this.fieldTypeDao = fieldTypeDao;
        this.dictionaryService = dictionaryService;
        this.messageSource = messageSource;
        this.localeBean = localeBean;
    }

    @Override
    public FieldType getFieldType(int fieldTypeId) throws ServiceException {
        try {
            FieldType fieldType = fieldTypeDao.get(fieldTypeId);
            String fieldTypeName = dictionaryService.getLocalizedFieldTypeName(fieldType.getId(), localeBean.getLocale());
            if (fieldTypeName != null) {
                fieldType.setName(fieldTypeName);
            }
            String fieldTypeTypeName = dictionaryService.getLocalizedFieldTypeTypeName(fieldType.getTypeId(), localeBean.getLocale());
            if (fieldTypeTypeName != null) {
                fieldType.setTypeName(fieldTypeTypeName);
            }
            return fieldType;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.fieldType.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), fieldTypeId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<FieldType> getFieldTypeList() throws ServiceException {
        try {
            List<FieldType> fieldTypes = fieldTypeDao.getList();
            for (FieldType fieldType : fieldTypes) {
                String fieldTypeName = dictionaryService.getLocalizedFieldTypeName(fieldType.getId(), localeBean.getLocale());
                if (fieldTypeName != null) {
                    fieldType.setName(fieldTypeName);
                }
                String fieldTypeTypeName = dictionaryService.getLocalizedFieldTypeTypeName(fieldType.getTypeId(), localeBean.getLocale());
                if (fieldTypeTypeName != null) {
                    fieldType.setTypeName(fieldTypeTypeName);
                }
            }
            return fieldTypes;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.fieldType.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
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
}

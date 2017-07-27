package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.FieldType;

import java.util.List;

public interface FieldTypeDao {

    FieldType get(int fieldTypeId) throws DataAccessException;

    List<FieldType> getList() throws DataAccessException;
}
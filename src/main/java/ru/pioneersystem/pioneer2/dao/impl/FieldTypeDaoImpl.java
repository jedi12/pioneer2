package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.pioneersystem.pioneer2.dao.FieldTypeDao;
import ru.pioneersystem.pioneer2.model.FieldType;

import java.util.List;

@Repository(value = "fieldTypeDao")
public class FieldTypeDaoImpl implements FieldTypeDao {
    private static final String SELECT_FIELD_TYPE =
            "SELECT ID, NAME, TYPE FROM DOC.FIELDS_TYPE WHERE ID = ?";
    private static final String SELECT_FIELD_TYPE_LIST =
            "SELECT ID, NAME, TYPE FROM DOC.FIELDS_TYPE ORDER BY TYPE ASC, NAME ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public FieldType get(int fieldTypeId) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_FIELD_TYPE,
                new Object[]{fieldTypeId},
                (rs, rowNum) -> {
                    FieldType fieldType = new FieldType();
                    fieldType.setId(rs.getInt("ID"));
                    fieldType.setName(rs.getString("NAME"));
                    fieldType.setTypeId(rs.getInt("TYPE"));
                    return fieldType;
                }
        );
    }

    @Override
    public List<FieldType> getList() throws DataAccessException {
        return jdbcTemplate.query(SELECT_FIELD_TYPE_LIST,
                new Object[]{},
                (rs, rowNum) -> {
                    FieldType fieldType = new FieldType();
                    fieldType.setId(rs.getInt("ID"));
                    fieldType.setName(rs.getString("NAME"));
                    fieldType.setTypeId(rs.getInt("TYPE"));
                    return fieldType;
                }
        );
    }
}
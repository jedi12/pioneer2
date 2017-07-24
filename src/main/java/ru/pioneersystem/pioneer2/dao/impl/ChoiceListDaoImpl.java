package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.ChoiceListDao;
import ru.pioneersystem.pioneer2.dao.exception.RestrictionDaoException;
import ru.pioneersystem.pioneer2.model.ChoiceList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository(value = "choiceListDao")
public class ChoiceListDaoImpl implements ChoiceListDao {
    private static final String INSERT_LIST = "INSERT INTO DOC.LISTS (NAME, STATE, COMPANY) VALUES (?, ?, ?)";
    private static final String INSERT_LIST_FIELD = "INSERT INTO DOC.LISTS_FIELD (ID, VALUE) VALUES (?, ?)";
    private static final String UPDATE_LIST = "UPDATE DOC.LISTS SET NAME = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_LIST = "UPDATE DOC.LISTS SET STATE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_LIST_FIELD = "DELETE FROM DOC.LISTS_FIELD WHERE ID = ?";
    private static final String SELECT_LIST = "SELECT ID, NAME FROM DOC.LISTS WHERE ID = ? AND COMPANY = ?";
    private static final String SELECT_LIST_FIELD = "SELECT VALUE FROM DOC.LISTS_FIELD WHERE ID = ?";
    private static final String SELECT_LIST_LIST = "SELECT ID, NAME FROM DOC.LISTS WHERE STATE = ? AND COMPANY = ?";
    private static final String MAP_FIELD_FOR_DOC = "SELECT ID, VALUE FROM DOC.LISTS_FIELD WHERE ID IN (" +
            "SELECT VALUE_LIST FROM DOC.DOCUMENTS_FIELD WHERE ID = ? AND VALUE_LIST IS NOT NULL) " +
            "ORDER BY ID ASC, VALUE ASC";
    private static final String MAP_FIELD_FOR_TEMP = "SELECT ID, VALUE FROM DOC.LISTS_FIELD WHERE ID IN (" +
            "SELECT FIELD_LIST FROM DOC.TEMPLATES_FIELD WHERE ID = ? AND FIELD_LIST IS NOT NULL) " +
            "ORDER BY ID ASC, VALUE ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ChoiceList get(int choiceListId, int companyId) throws DataAccessException {
        ChoiceList resultChoiceList = jdbcTemplate.queryForObject(SELECT_LIST,
                new Object[]{choiceListId, companyId},
                (rs, rowNum) -> {
                    ChoiceList choiceList = new ChoiceList();
                    choiceList.setId(rs.getInt("ID"));
                    choiceList.setName(rs.getString("NAME"));
                    return choiceList;
                }
        );

        if (resultChoiceList == null) {
            throw new RestrictionDaoException("ChoiceList id=" + choiceListId +
                    " does not belong to the company id=" + companyId);
        }

        List<String> resultValues = jdbcTemplate.query(SELECT_LIST_FIELD,
                new Object[]{choiceListId},
                rs -> {
                    List<String> values = new LinkedList<>();
                    while(rs.next()){
                        values.add(rs.getString("VALUE"));
                    }
                    return values;
                }
        );

        resultChoiceList.setValues(resultValues);
        return resultChoiceList;
    }

    @Override
    public Map<Integer, List<String>> getForTemplate(int templateId) throws DataAccessException {
        return jdbcTemplate.query(MAP_FIELD_FOR_TEMP,
                new Object[]{templateId},
                rs -> {
                    Map<Integer, List<String>> result = new HashMap<>();
                    int oldlistId = 0;
                    while(rs.next()){
                        int id = rs.getInt("ID");
                        String value = rs.getString("VALUE");

                        if (id != oldlistId) {
                            result.put(id, new LinkedList<>());
                            oldlistId = id;
                        }
                        result.get(id).add(value);
                    }
                    return result;
                }
        );
    }

    @Override
    public Map<Integer, List<String>> getForDocument(int documentId) throws DataAccessException {
        List<String> choiceListValues = jdbcTemplate.query(MAP_FIELD_FOR_DOC,
                new Object[]{documentId},
                new ChoiceListValuesMapper()
        );

        Map<Integer, List<String>> listMap = new HashMap<>();
        listMap.put(documentId, choiceListValues);

        return listMap;
    }

    @Override
    public List<ChoiceList> getList(int companyId) throws DataAccessException {
        List<ChoiceList> choiceList = jdbcTemplate.query(SELECT_LIST_LIST,
                new Object[]{ChoiceList.State.EXISTS, companyId},
                new ChoiceListMapper()
        );

        return choiceList;
    }

    @Override
    @Transactional
    public void create(ChoiceList choiceList, int companyId) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_LIST, new String[] {"id"});
                    pstmt.setString(1, choiceList.getName());
                    pstmt.setInt(2, ChoiceList.State.EXISTS);
                    pstmt.setInt(3, companyId);
                    return pstmt;
                }, keyHolder
        );

        jdbcTemplate.batchUpdate(INSERT_LIST_FIELD,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setString(2, choiceList.getValues().get(i));
                    }
                    public int getBatchSize() {
                        return choiceList.getValues().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void update(ChoiceList choiceList, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_LIST,
                choiceList.getName(),
                choiceList.getId(),
                companyId
        );

        if (updatedRows == 0) {
            throw new RestrictionDaoException("ChoiceList id=" + choiceList.getId() +
                    " does not belong to the company id=" + companyId);
        }

        jdbcTemplate.update(DELETE_LIST_FIELD,
                choiceList.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_LIST_FIELD,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, choiceList.getId());
                        pstmt.setString(2, choiceList.getValues().get(i));
                    }
                    public int getBatchSize() {
                            return choiceList.getValues().size();
                        }
                }
        );
    }

    @Override
    @Transactional
    public void delete(int choiceListId, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(DELETE_LIST,
                ChoiceList.State.DELETED,
                choiceListId,
                companyId
        );

        if (updatedRows == 0) {
            throw new RestrictionDaoException("ChoiceList id=" + choiceListId +
                    " does not belong to the company id=" + companyId);
        }

        jdbcTemplate.update(DELETE_LIST_FIELD, choiceListId);
    }

    private static final class ChoiceListMapper implements RowMapper<ChoiceList> {
        public ChoiceList mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChoiceList choiceList = new ChoiceList();
            choiceList.setId(rs.getInt("ID"));
            choiceList.setName(rs.getString("NAME"));
            return choiceList;
        }
    }

    private static final class ChoiceListValuesMapper implements RowMapper<String> {
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("VALUE");
        }
    }
}
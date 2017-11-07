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
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.ChoiceList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository(value = "choiceListDao")
public class ChoiceListDaoImpl implements ChoiceListDao {
    private static final String INSERT_LIST = "INSERT INTO DOC.LISTS (NAME, STATE, COMPANY) VALUES (?, ?, ?)";
    private static final String INSERT_LIST_FIELD = "INSERT INTO DOC.LISTS_FIELD (ID, VALUE) VALUES (?, ?)";
    private static final String UPDATE_LIST = "UPDATE DOC.LISTS SET NAME = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_LIST = "UPDATE DOC.LISTS SET STATE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_LIST_FIELD = "DELETE FROM DOC.LISTS_FIELD WHERE ID = ?";
    private static final String SELECT_LIST = "SELECT ID, NAME FROM DOC.LISTS WHERE STATE > 0 AND ID = ? AND COMPANY = ?";
    private static final String SELECT_LIST_FIELD = "SELECT VALUE FROM DOC.LISTS_FIELD WHERE ID = ?";
    private static final String SELECT_SUPER_CHOICE_LIST_LIST =
            "SELECT L.ID AS ID, L.NAME AS NAME, COMPANY, C.NAME AS COMPANY_NAME FROM DOC.LISTS L " +
                    "LEFT JOIN DOC.COMPANY C ON C.ID = L.COMPANY WHERE L.STATE > 0 ORDER BY L.COMPANY ASC, L.NAME ASC";
    private static final String SELECT_ADMIN_CHOICE_LIST_LIST =
            "SELECT ID, NAME, COMPANY, NULL AS COMPANY_NAME FROM DOC.LISTS WHERE STATE > 0 AND COMPANY = ? " +
                    "ORDER BY NAME ASC";
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
        ChoiceList resultChoiceList = jdbcTemplate.query(SELECT_LIST,
                new Object[]{choiceListId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        ChoiceList choiceList = new ChoiceList();
                        choiceList.setId(rs.getInt("ID"));
                        choiceList.setName(rs.getString("NAME"));
                        return choiceList;
                    } else {
                        throw new NotFoundDaoException("Not found ChoiceList with choiceListId = " + choiceListId +
                                " and companyId = " + companyId);
                    }
                }
        );

        List<String> resultValues = jdbcTemplate.query(SELECT_LIST_FIELD,
                new Object[]{choiceListId},
                rs -> {
                    List<String> values = new ArrayList<>();
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
        Object[] params = new Object[]{templateId};
        return getMap(MAP_FIELD_FOR_TEMP, params);
    }

    @Override
    public Map<Integer, List<String>> getForDocument(int documentId) throws DataAccessException {
        Object[] params = new Object[]{documentId};
        return getMap(MAP_FIELD_FOR_DOC, params);
    }

    private Map<Integer, List<String>> getMap(String query, Object[] params) throws DataAccessException {
        return jdbcTemplate.query(query, params,
                (rs) -> {
                    Map<Integer, List<String>> result = new HashMap<>();
                    int oldlistId = 0;
                    while(rs.next()){
                        int id = rs.getInt("ID");
                        String value = rs.getString("VALUE");

                        if (id != oldlistId) {
                            result.put(id, new ArrayList<>());
                            oldlistId = id;
                        }
                        result.get(id).add(value);
                    }
                    return result;
                }
        );
    }

    @Override
    public List<ChoiceList> getSuperList() throws DataAccessException {
        Object[] params = new Object[]{};
        return getList(SELECT_SUPER_CHOICE_LIST_LIST, params);
    }

    @Override
    public List<ChoiceList> getAdminList(int companyId) throws DataAccessException {
        Object[] params = new Object[]{companyId};
        return getList(SELECT_ADMIN_CHOICE_LIST_LIST, params);
    }

    private List<ChoiceList> getList(String query, Object[] params) throws DataAccessException {
        return jdbcTemplate.query(query, params,
                (rs) -> {
                    List<ChoiceList> choiceLists = new ArrayList<>();
                    while(rs.next()){
                        ChoiceList choiceList = new ChoiceList();
                        choiceList.setId(rs.getInt("ID"));
                        choiceList.setName(rs.getString("NAME"));
                        choiceList.setCompanyId(rs.getInt("COMPANY"));
                        choiceList.setCompanyName(rs.getString("COMPANY_NAME"));

                        choiceLists.add(choiceList);
                    }
                    return choiceLists;
                }
        );
    }

    @Override
    @Transactional
    public int create(ChoiceList choiceList, int companyId) throws DataAccessException {
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
        return keyHolder.getKey().intValue();
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
            throw new NotFoundDaoException("Not found ChoiceList with choiceListId = " + choiceList.getId() +
                    " and companyId = " + companyId);
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
            throw new NotFoundDaoException("Not found ChoiceList with choiceListId = " + choiceListId +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_LIST_FIELD, choiceListId);
    }
}
package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.DocumentDao;
import ru.pioneersystem.pioneer2.model.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Repository(value = "documentDao")
public class DocumentDaoImpl implements DocumentDao {
    private static final String INSERT_TEMPLATE =
            "INSERT INTO DOC.TEMPLATES (NAME, ROUTE, PART, STATE, COMPANY) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_TEMPLATE_FIELD =
            "INSERT INTO DOC.TEMPLATES_FIELD (ID, FIELD_NAME, FIELD_NUM, FIELD_TYPE, FIELD_LIST) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_TEMPLATE_CONDITION =
            "INSERT INTO DOC.TEMPLATES_COND (ID, COND_NUM, FIELD_NUM, COND, VALUE, ROUTE) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_TEMPLATE = "UPDATE DOC.TEMPLATES SET NAME = ?, ROUTE = ?, PART = ? WHERE ID = ?";
    private static final String DELETE_TEMPLATE = "UPDATE DOC.TEMPLATES SET STATE = ? WHERE ID = ?";
    private static final String DELETE_TEMPLATE_FIELD = "DELETE FROM DOC.TEMPLATES_FIELD WHERE ID = ?";
    private static final String DELETE_TEMPLATE_CONDITION = "DELETE FROM DOC.TEMPLATES_COND WHERE ID = ?";
    private static final String SELECT_TEMPLATE = "SELECT ID, NAME, ROUTE, PART FROM DOC.TEMPLATES WHERE ID = ?";
    private static final String SELECT_TEMPLATE_FIELD =
            "SELECT FIELD_NAME, FIELD_NUM, FIELD_TYPE, FIELD_LIST, NAME AS FIELD_LIST_NAME FROM DOC.TEMPLATES_FIELD TF " +
                    "LEFT JOIN DOC.LISTS L ON TF.FIELD_LIST = L.ID WHERE TF.ID = ? ORDER BY FIELD_NUM ASC";
    private static final String SELECT_TEMPLATE_CONDITION =
            "SELECT COND_NUM, TC.FIELD_NUM AS FIELD_NUM, FIELD_NAME, COND, VALUE, ROUTE, NAME AS ROUTE_NAME " +
                    "FROM DOC.TEMPLATES_COND TC LEFT JOIN DOC.ROUTES R ON TC.ROUTE = R.ID " +
                    "LEFT JOIN DOC.TEMPLATES_FIELD TF ON TC.ID = TF.ID AND TF.FIELD_NUM = TC.FIELD_NUM " +
                    "WHERE TC.ID = ? ORDER BY FIELD_NUM ASC";
    private static final String SELECT_TEMPLATE_LIST =
            "SELECT T.ID AS ID, T.NAME AS NAME, T.STATE AS STATE, R.ID AS ROUTE_ID, R.NAME AS ROUTE_NAME, " +
                    "P.ID AS PART_ID, P.NAME AS PART_NAME FROM DOC.TEMPLATES T LEFT JOIN DOC.ROUTES R " +
                    "ON T.ROUTE = R.ID LEFT JOIN DOC.PARTS P ON T.PART = P.ID WHERE T.STATE > 0 AND T.COMPANY = ? " +
                    "ORDER BY STATE DESC, NAME ASC";
    private static final String SELECT_ON_ROUTE_DOCUMENT_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, U_DATE FROM DOC.DOCUMENTS D, DOC.GROUPS G " +
                    "WHERE D.DOC_GROUP = G.ID AND D.ID IN (SELECT ID FROM DOC.DOCUMENTS_SIGN  WHERE ACTIVE = 1 " +
                    "AND ROLE_ID = ? AND SIGNED = 0 AND GROUP_ID IN (SELECT ID FROM DOC.GROUPS_USER " +
                    "WHERE USER_ID = ?)) ORDER BY U_DATE";
    private static final String SELECT_DOCUMENT_LIST_BY_PART =
            "SELECT ID, NAME FROM DOC.DOCUMENTS WHERE PUB_PART = ? AND STATUS = ? ORDER BY NAME ASC";
    private static final String SELECT_MY_ON_DATE_DOCUMENT_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, DS.NAME AS STATUS_NAME, G.NAME AS GROUP_NAME FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.DOCUMENTS_STATUS DS ON D.STATUS = DS.ID LEFT JOIN DOC.GROUPS G ON D.DOC_GROUP = G.ID " +
                    "WHERE DS.ID <> 1 AND D.U_DATE >= ? AND D.U_DATE < ? AND D.DOC_GROUP IN (SELECT G.ID FROM " +
                    "DOC.GROUPS G, DOC.GROUPS_USER GU WHERE G.ID = GU.ID AND USER_ID = ? AND ROLE_ID = 9 " +
                    "AND STATE > 0) ORDER BY D.U_DATE DESC";
    private static final String SELECT_MY_ON_WORK_DOCUMENT_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, DS.NAME AS STATUS_NAME, G.NAME AS GROUP_NAME FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.DOCUMENTS_STATUS DS ON D.STATUS = DS.ID LEFT JOIN DOC.GROUPS G ON D.DOC_GROUP = G.ID " +
                    "WHERE D.STATUS >= 9 AND D.DOC_GROUP IN (SELECT G.ID FROM " +
                    "DOC.GROUPS G, DOC.GROUPS_USER GU WHERE G.ID = GU.ID AND USER_ID = ? AND ROLE_ID = 9 " +
                    "AND STATE > 0) ORDER BY D.U_DATE DESC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Document get(int id) throws DataAccessException {
//        Template resultTemplate = jdbcTemplate.queryForObject(SELECT_TEMPLATE,
//                new Object[]{id},
//                (rs, rowNum) -> {
//                    Template template = new Template();
//                    template.setId(rs.getInt("ID"));
//                    template.setName(rs.getString("NAME"));
//                    template.setRouteId(rs.getInt("ROUTE"));
//                    template.setPartId(rs.getInt("PART"));
//                    return template;
//                }
//        );
//
//        LinkedList<Document.Field> resultFields = jdbcTemplate.query(SELECT_TEMPLATE_FIELD,
//                new Object[]{id},
//                rs -> {
//                    LinkedList<Document.Field> fields = new LinkedList<>();
//                    while(rs.next()){
//                        Document.Field field = new Document.Field();
//                        field.setName(rs.getString("FIELD_NAME"));
//                        field.setNum(rs.getInt("FIELD_NUM"));
//                        field.setTypeId(rs.getInt("FIELD_TYPE"));
//                        field.setChoiceListId(rs.getInt("FIELD_LIST"));
//                        field.setChoiceListName(rs.getString("FIELD_LIST_NAME"));
//
//                        fields.add(field);
//                    }
//                    return fields;
//                }
//        );
//
//        List<Document.Condition> resultConditions = jdbcTemplate.query(SELECT_TEMPLATE_CONDITION,
//                new Object[]{id},
//                rs -> {
//                    List<Document.Condition> conditions = new LinkedList<>();
//                    while(rs.next()){
//                        Document.Condition condition = new Document.Condition();
//                        condition.setCondNum(rs.getInt("COND_NUM"));
//                        condition.setFieldNum(rs.getInt("FIELD_NUM"));
//                        condition.setFieldName(rs.getString("FIELD_NAME"));
//                        condition.setCond(rs.getString("COND"));
//                        condition.setValue(rs.getString("VALUE"));
//                        condition.setRouteId(rs.getInt("ROUTE"));
//                        condition.setRouteName(rs.getString("ROUTE_NAME"));
//
//                        conditions.add(condition);
//                    }
//                    return conditions;
//                }
//        );
//
//        resultTemplate.setFields(resultFields);
//        resultTemplate.setConditions(resultConditions);
//        return resultTemplate;
        return null;
    }

    @Override
    public List<Document> getOnRouteList(int roleId, int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ON_ROUTE_DOCUMENT_LIST,
                new Object[]{roleId, userId},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setId(rs.getInt("ID"));
                    document.setName(rs.getString("NAME"));
                    document.setChangeDate(rs.getTimestamp("U_DATE"));
                    return document;
                }
        );
    }

    @Override
    public List<Document> getListByPartId(int partId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_DOCUMENT_LIST_BY_PART,
                new Object[]{partId, Status.Id.PUBLISHED},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setId(rs.getInt("ID"));
                    document.setName(rs.getString("NAME"));
                    return document;
                }
        );
    }

    @Override
    public List<Document> getMyOnDateList(Date beginDate, Date endDate, int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_MY_ON_DATE_DOCUMENT_LIST,
                new Object[]{beginDate, endDate, userId},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setId(rs.getInt("ID"));
                    document.setName(rs.getString("NAME"));
                    document.setStatusName(rs.getString("STATUS_NAME"));
                    document.setDocumentGroupName(rs.getString("GROUP_NAME"));
                    return document;
                }
        );
    }

    @Override
    public List<Document> getMyOnWorkingList(int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_MY_ON_WORK_DOCUMENT_LIST,
                new Object[]{userId},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setId(rs.getInt("ID"));
                    document.setName(rs.getString("NAME"));
                    document.setStatusName(rs.getString("STATUS_NAME"));
                    document.setDocumentGroupName(rs.getString("GROUP_NAME"));
                    return document;
                }
        );
    }

    @Override
    @Transactional
    public void create(Document document, int company) throws DataAccessException {
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        jdbcTemplate.update(
//                connection -> {
//                    PreparedStatement pstmt = connection.prepareStatement(INSERT_TEMPLATE, new String[] {"id"});
//                    pstmt.setString(1, template.getName());
//                    pstmt.setInt(2, template.getRouteId());
//                    pstmt.setInt(3, template.getPartId());
//                    pstmt.setInt(4, Template.State.EXISTS);
//                    pstmt.setInt(5, company);
//                    return pstmt;
//                }, keyHolder
//        );
//
//        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_FIELD,
//                new BatchPreparedStatementSetter() {
//                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
//                        pstmt.setInt(1, keyHolder.getKey().intValue());
//                        pstmt.setString(2, template.getFields().get(i).getName());
//                        pstmt.setInt(3, template.getFields().get(i).getNum());
//                        pstmt.setInt(4, template.getFields().get(i).getTypeId());
//                        pstmt.setInt(5, template.getFields().get(i).getChoiceListId());
//                    }
//                    public int getBatchSize() {
//                        return template.getFields().size();
//                    }
//                }
//        );
//
//        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_CONDITION,
//                new BatchPreparedStatementSetter() {
//                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
//                        pstmt.setInt(1, keyHolder.getKey().intValue());
//                        pstmt.setInt(2, template.getConditions().get(i).getCondNum());
//                        pstmt.setInt(3, template.getConditions().get(i).getFieldNum());
//                        pstmt.setString(4, template.getConditions().get(i).getCond());
//                        pstmt.setString(5, template.getConditions().get(i).getValue());
//                        pstmt.setInt(6, template.getConditions().get(i).getRouteId());
//                    }
//                    public int getBatchSize() {
//                        return template.getConditions().size();
//                    }
//                }
//        );
        return;
    }

    @Override
    @Transactional
    public void update(Document document) throws DataAccessException {
//        jdbcTemplate.update(UPDATE_TEMPLATE,
//                template.getName(),
//                template.getRouteId(),
//                template.getPartId(),
//                template.getId()
//        );
//
//        jdbcTemplate.update(DELETE_TEMPLATE_FIELD,
//                template.getId()
//        );
//
//        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_FIELD,
//                new BatchPreparedStatementSetter() {
//                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
//                        pstmt.setInt(1, template.getId());
//                        pstmt.setString(2, template.getFields().get(i).getName());
//                        pstmt.setInt(3, template.getFields().get(i).getNum());
//                        pstmt.setInt(4, template.getFields().get(i).getTypeId());
//                        pstmt.setInt(5, template.getFields().get(i).getChoiceListId());
//                    }
//                    public int getBatchSize() {
//                        return template.getFields().size();
//                    }
//                }
//        );
//
//        jdbcTemplate.update(DELETE_TEMPLATE_CONDITION,
//                template.getId()
//        );
//
//        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_CONDITION,
//                new BatchPreparedStatementSetter() {
//                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
//                        pstmt.setInt(1, template.getId());
//                        pstmt.setInt(2, template.getConditions().get(i).getCondNum());
//                        pstmt.setInt(3, template.getConditions().get(i).getFieldNum());
//                        pstmt.setString(4, template.getConditions().get(i).getCond());
//                        pstmt.setString(5, template.getConditions().get(i).getValue());
//                        pstmt.setInt(6, template.getConditions().get(i).getRouteId());
//                    }
//                    public int getBatchSize() {
//                        return template.getConditions().size();
//                    }
//                }
//        );
        return;
    }

    @Override
    @Transactional
    public void delete(int id) throws DataAccessException {
//        jdbcTemplate.update(DELETE_TEMPLATE, Template.State.DELETED, id);
//        jdbcTemplate.update(DELETE_TEMPLATE_FIELD, id);
//        jdbcTemplate.update(DELETE_TEMPLATE_CONDITION, id);
        return;
    }
}
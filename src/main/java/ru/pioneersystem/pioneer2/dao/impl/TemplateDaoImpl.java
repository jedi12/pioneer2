package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.TemplateDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.model.Template;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository(value = "templateDao")
public class TemplateDaoImpl implements TemplateDao {
    private static final String INSERT_TEMPLATE =
            "INSERT INTO DOC.TEMPLATES (NAME, ROUTE, PART, STATE, COMPANY) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_TEMPLATE_FIELD =
            "INSERT INTO DOC.TEMPLATES_FIELD (ID, FIELD_NAME, FIELD_NUM, FIELD_TYPE, FIELD_LIST) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_TEMPLATE_CONDITION =
            "INSERT INTO DOC.TEMPLATES_COND (ID, COND_NUM, FIELD_NUM, COND, VALUE, ROUTE) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_TEMPLATE =
            "UPDATE DOC.TEMPLATES SET NAME = ?, ROUTE = ?, PART = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_TEMPLATE = "UPDATE DOC.TEMPLATES SET STATE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_TEMPLATE_FIELD = "DELETE FROM DOC.TEMPLATES_FIELD WHERE ID = ?";
    private static final String DELETE_TEMPLATE_CONDITION = "DELETE FROM DOC.TEMPLATES_COND WHERE ID = ?";
    private static final String SELECT_TEMPLATE =
            "SELECT ID, NAME, ROUTE, PART FROM DOC.TEMPLATES WHERE ID = ? AND COMPANY = ?";
    private static final String SELECT_TEMPLATE_FIELD =
            "SELECT FIELD_NAME, FIELD_NUM, FIELD_TYPE, FIELD_LIST, NAME AS FIELD_LIST_NAME FROM DOC.TEMPLATES_FIELD TF " +
                    "LEFT JOIN DOC.LISTS L ON TF.FIELD_LIST = L.ID WHERE TF.ID = ? ORDER BY FIELD_NUM ASC";
    private static final String SELECT_TEMPLATE_CONDITION =
            "SELECT COND_NUM, TC.FIELD_NUM AS FIELD_NUM, FIELD_NAME, COND, VALUE, ROUTE, NAME AS ROUTE_NAME " +
                    "FROM DOC.TEMPLATES_COND TC LEFT JOIN DOC.ROUTES R ON TC.ROUTE = R.ID " +
                    "LEFT JOIN DOC.TEMPLATES_FIELD TF ON TC.ID = TF.ID AND TF.FIELD_NUM = TC.FIELD_NUM " +
                    "WHERE TC.ID = ? ORDER BY FIELD_NUM ASC";
    private static final String SELECT_SUPER_TEMPLATE_LIST =
            "SELECT T.ID AS ID, T.NAME AS NAME, T.STATE AS STATE, R.ID AS ROUTE_ID, R.NAME AS ROUTE_NAME, " +
                    "P.ID AS PART_ID, P.NAME AS PART_NAME, T.COMPANY, C.NAME AS COMPANY_NAME FROM DOC.TEMPLATES T " +
                    "LEFT JOIN DOC.ROUTES R ON T.ROUTE = R.ID LEFT JOIN DOC.PARTS P ON T.PART = P.ID " +
                    "LEFT JOIN DOC.COMPANY C ON C.ID = T.COMPANY WHERE T.STATE > 0 ORDER BY T.COMPANY ASC, NAME ASC";
    private static final String SELECT_ADMIN_TEMPLATE_LIST =
            "SELECT T.ID AS ID, T.NAME AS NAME, T.STATE AS STATE, R.ID AS ROUTE_ID, R.NAME AS ROUTE_NAME, " +
                    "P.ID AS PART_ID, P.NAME AS PART_NAME, T.COMPANY, NULL AS COMPANY_NAME FROM DOC.TEMPLATES T " +
                    "LEFT JOIN DOC.ROUTES R ON T.ROUTE = R.ID LEFT JOIN DOC.PARTS P ON T.PART = P.ID " +
                    "WHERE T.STATE > 0 AND T.COMPANY = ? ORDER BY STATE DESC, NAME ASC";
    private static final String SELECT_TEMPLATE_LIST_BY_PART =
            "SELECT ID, NAME FROM DOC.TEMPLATES WHERE STATE > 0 AND PART = ? AND COMPANY = ? ORDER BY NAME ASC";
    private static final String SELECT_TEMPLATE_LIST_CONTAINING_CHOICE_LIST =
            "SELECT DISTINCT NAME FROM DOC.TEMPLATES T, DOC.TEMPLATES_FIELD TF WHERE T.ID = TF.ID AND STATE > 0 " +
                    "AND FIELD_LIST = ? AND COMPANY = ? ORDER BY NAME";
    private static final String UPDATE_TEMPLATE_PARTS =
            "UPDATE DOC.TEMPLATES SET PART = 0 WHERE STATE > 0 AND PART = ? AND COMPANY = ?";
    private static final String SELECT_TEMPLATE_LIST_CONTAINING_ROUTE =
            "SELECT DISTINCT NAME FROM DOC.TEMPLATES T LEFT JOIN DOC.TEMPLATES_COND TC ON T.ID = TC.ID " +
                    "WHERE T.STATE = 1 AND (T.ROUTE = ? OR TC.ROUTE = ?) AND COMPANY = ?";
    private static final String SELECT_USER_TEMPLATE_MAP =
            "SELECT DISTINCT T.ID AS ID, T.NAME AS NANE FROM DOC.TEMPLATES T LEFT JOIN DOC.PARTS P ON P.ID = T.PART " +
                    "LEFT JOIN DOC.PARTS_GROUP PG ON P.ID = PG.ID LEFT JOIN DOC.GROUPS_USER GU ON PG.GROUP_ID = GU.ID " +
                    "WHERE TYPE = 1 AND T.STATE > 0 AND (GU.USER_ID = ? OR GU.USER_ID IS NULL) AND T.COMPANY = ? " +
                    "ORDER BY T.NAME ASC";
    private static final String SELECT_TEMPLATE_MAP =
            "SELECT ID, NAME FROM DOC.TEMPLATES WHERE STATE > 0 AND COMPANY = ? ORDER BY NAME ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Template get(int templateId, int companyId) throws DataAccessException {
        Template resultTemplate = jdbcTemplate.query(SELECT_TEMPLATE,
                new Object[]{templateId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        Template template = new Template();
                        template.setId(rs.getInt("ID"));
                        template.setName(rs.getString("NAME"));
                        template.setRouteId(rs.getInt("ROUTE"));
                        template.setPartId(rs.getInt("PART"));
                        return template;
                    } else {
                        throw new NotFoundDaoException("Not found Template with templateId = " + templateId +
                                " and companyId = " + companyId);
                    }
                }
        );

        LinkedList<Document.Field> resultFields = jdbcTemplate.query(SELECT_TEMPLATE_FIELD,
                new Object[]{templateId},
                rs -> {
                    LinkedList<Document.Field> fields = new LinkedList<>();
                    while(rs.next()){
                        Document.Field field = new Document.Field();
                        field.setName(rs.getString("FIELD_NAME"));
                        field.setNum(rs.getInt("FIELD_NUM"));
                        field.setTypeId(rs.getInt("FIELD_TYPE"));
                        field.setChoiceListId(rs.getObject(("FIELD_LIST"), Integer.class));
                        field.setChoiceListName(rs.getString("FIELD_LIST_NAME"));

                        fields.add(field);
                    }
                    return fields;
                }
        );

        List<Document.Condition> resultConditions = jdbcTemplate.query(SELECT_TEMPLATE_CONDITION,
                new Object[]{templateId},
                rs -> {
                    List<Document.Condition> conditions = new ArrayList<>();
                    while(rs.next()){
                        Document.Condition condition = new Document.Condition();
                        condition.setCondNum(rs.getInt("COND_NUM"));
                        condition.setFieldNum(rs.getInt("FIELD_NUM"));
                        condition.setFieldName(rs.getString("FIELD_NAME"));
                        condition.setCond(rs.getString("COND"));
                        condition.setValue(rs.getString("VALUE"));
                        condition.setRouteId(rs.getInt("ROUTE"));
                        condition.setRouteName(rs.getString("ROUTE_NAME"));

                        conditions.add(condition);
                    }
                    return conditions;
                }
        );

        resultTemplate.setFields(resultFields);
        resultTemplate.setConditions(resultConditions);
        return resultTemplate;
    }

    @Override
    public List<Template> getSuperList() throws DataAccessException {
        Object[] params = new Object[]{};
        return getList(SELECT_SUPER_TEMPLATE_LIST, params);
    }

    @Override
    public List<Template> getAdminList(int companyId) throws DataAccessException {
        Object[] params = new Object[]{companyId};
        return getList(SELECT_ADMIN_TEMPLATE_LIST, params);
    }

    private List<Template> getList(String query, Object[] params) throws DataAccessException {
        return jdbcTemplate.query(query, params,
                (rs) -> {
                    List<Template> templates = new ArrayList<>();
                    while(rs.next()){
                        Template template = new Template();
                        template.setId(rs.getInt("ID"));
                        template.setName(rs.getString("NAME"));
                        template.setState(rs.getInt("STATE"));
                        template.setRouteId(rs.getInt("ROUTE_ID"));
                        template.setRouteName(rs.getString("ROUTE_NAME"));
                        template.setPartId(rs.getInt("PART_ID"));
                        template.setPartName(rs.getString("PART_NAME"));
                        template.setCompanyId(rs.getInt("COMPANY"));
                        template.setCompanyName(rs.getString("COMPANY_NAME"));

                        templates.add(template);
                    }
                    return templates;
                }
        );
    }

    @Override
    public List<Template> getListByPartId(int partId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_TEMPLATE_LIST_BY_PART,
                new Object[]{partId, companyId},
                (rs, rowNum) -> {
                    Template template = new Template();
                    template.setId(rs.getInt("ID"));
                    template.setName(rs.getString("NAME"));
                    return template;
                }
        );
    }

    @Override
    public List<String> getListContainingChoiceList(int choiceListId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_TEMPLATE_LIST_CONTAINING_CHOICE_LIST,
                new Object[]{choiceListId, companyId},
                (rs, rowNum) -> rs.getString("NAME")
        );
    }

    @Override
    public List<String> getListContainingRoute(int routeId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_TEMPLATE_LIST_CONTAINING_ROUTE,
                new Object[]{routeId, routeId, companyId},
                (rs, rowNum) -> rs.getString("NAME")
        );
    }

    @Override
    public Map<String, Integer> getTemplateMap(int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_TEMPLATE_MAP,
                new Object[]{companyId},
                (rs) -> {
                    Map<String, Integer> routes = new LinkedHashMap<>();
                    while(rs.next()){
                        routes.put(rs.getString("NAME"), rs.getInt("ID"));
                    }
                    return routes;
                }
        );
    }

    @Override
    public Map<String, Integer> getUserTemplateMap(int userId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USER_TEMPLATE_MAP,
                new Object[]{userId, companyId},
                (rs) -> {
                    Map<String, Integer> routes = new LinkedHashMap<>();
                    while(rs.next()){
                        routes.put(rs.getString("NAME"), rs.getInt("ID"));
                    }
                    return routes;
                }
        );
    }

    @Override
    @Transactional
    public void removeFromParts(List<Part> parts, int companyId) throws DataAccessException {
        jdbcTemplate.batchUpdate(UPDATE_TEMPLATE_PARTS,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, parts.get(i).getId());
                        pstmt.setObject(2, companyId);
                    }
                    public int getBatchSize() {
                        return parts.size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public int create(Template template, int companyId) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_TEMPLATE, new String[] {"id"});
                    pstmt.setString(1, template.getName());
                    pstmt.setInt(2, template.getRouteId());
                    pstmt.setInt(3, template.getPartId());
                    pstmt.setInt(4, Template.State.EXISTS);
                    pstmt.setInt(5, companyId);
                    return pstmt;
                }, keyHolder
        );

        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_FIELD,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Field templateField = template.getFields().get(i);

                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setString(2, templateField.getName());
                        pstmt.setInt(3, templateField.getNum());
                        pstmt.setInt(4, templateField.getTypeId());
                        pstmt.setObject(5, templateField.getChoiceListId());
                    }
                    public int getBatchSize() {
                        return template.getFields().size();
                    }
                }
        );

        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_CONDITION,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Condition templateCondition = template.getConditions().get(i);

                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setInt(2, templateCondition.getCondNum());
                        pstmt.setInt(3, templateCondition.getFieldNum());
                        pstmt.setString(4, templateCondition.getCond());
                        pstmt.setString(5, templateCondition.getValue());
                        pstmt.setInt(6, templateCondition.getRouteId());
                    }
                    public int getBatchSize() {
                        return template.getConditions().size();
                    }
                }
        );
        return keyHolder.getKey().intValue();
    }

    @Override
    @Transactional
    public void update(Template template, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_TEMPLATE,
                template.getName(),
                template.getRouteId(),
                template.getPartId(),
                template.getId(),
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Template with templateId = " + template.getId() +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_TEMPLATE_FIELD,
                template.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_FIELD,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Field templateField = template.getFields().get(i);

                        pstmt.setInt(1, template.getId());
                        pstmt.setString(2, templateField.getName());
                        pstmt.setInt(3, templateField.getNum());
                        pstmt.setInt(4, templateField.getTypeId());
                        pstmt.setObject(5, templateField.getChoiceListId());
                    }
                    public int getBatchSize() {
                        return template.getFields().size();
                    }
                }
        );

        jdbcTemplate.update(DELETE_TEMPLATE_CONDITION,
                template.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_CONDITION,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Condition templateCondition = template.getConditions().get(i);

                        pstmt.setInt(1, template.getId());
                        pstmt.setInt(2, templateCondition.getCondNum());
                        pstmt.setInt(3, templateCondition.getFieldNum());
                        pstmt.setString(4, templateCondition.getCond());
                        pstmt.setString(5, templateCondition.getValue());
                        pstmt.setInt(6, templateCondition.getRouteId());
                    }
                    public int getBatchSize() {
                        return template.getConditions().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void delete(int templateId, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(DELETE_TEMPLATE,
                Template.State.DELETED,
                templateId,
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found TemplateId with templateId = " + templateId +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_TEMPLATE_FIELD,
                templateId
        );

        jdbcTemplate.update(DELETE_TEMPLATE_CONDITION,
                templateId
        );
    }
}
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
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.model.Template;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Repository(value = "templateDao")
public class TemplateDaoImpl implements TemplateDao {
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

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Template get(int id) throws DataAccessException {
        Template resultTemplate = jdbcTemplate.queryForObject(SELECT_TEMPLATE,
                new Object[]{id},
                (rs, rowNum) -> {
                    Template template = new Template();
                    template.setId(rs.getInt("ID"));
                    template.setName(rs.getString("NAME"));
                    template.setRouteId(rs.getInt("ROUTE"));
                    template.setPartId(rs.getInt("PART"));
                    return template;
                }
        );

        LinkedList<Template.Field> resultFields = jdbcTemplate.query(SELECT_TEMPLATE_FIELD,
                new Object[]{id},
                rs -> {
                    LinkedList<Template.Field> fields = new LinkedList<>();
                    while(rs.next()){
                        Template.Field field = new Template.Field();
                        field.setName(rs.getString("FIELD_NAME"));
                        field.setNum(rs.getInt("FIELD_NUM"));
                        field.setTypeId(rs.getInt("FIELD_TYPE"));
                        field.setChoiceListId(rs.getInt("FIELD_LIST"));
                        field.setChoiceListName(rs.getString("FIELD_LIST_NAME"));

                        fields.add(field);
                    }
                    return fields;
                }
        );

        List<Template.Condition> resultConditions = jdbcTemplate.query(SELECT_TEMPLATE_CONDITION,
                new Object[]{id},
                rs -> {
                    List<Template.Condition> conditions = new LinkedList<>();
                    while(rs.next()){
                        Template.Condition condition = new Template.Condition();
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
    public List<Template> getList(int company) throws DataAccessException {
        return jdbcTemplate.query(SELECT_TEMPLATE_LIST,
                new Object[]{company},
                (rs, rowNum) -> {
                    Route route = new Route();
                    route.setId(rs.getInt("ROUTE_ID"));
                    route.setName(rs.getString("ROUTE_NAME"));

                    Part part = new Part();
                    part.setId(rs.getInt("PART_ID"));
                    part.setName(rs.getString("PART_NAME"));

                    Template template = new Template();
                    template.setId(rs.getInt("ID"));
                    template.setName(rs.getString("NAME"));
                    template.setState(rs.getInt("STATE"));
                    template.setRoute(route);
                    template.setPart(part);
                    return template;
                }
        );
    }

    @Override
    @Transactional
    public void create(Template template, int company) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_TEMPLATE, new String[] {"id"});
                    pstmt.setString(1, template.getName());
                    pstmt.setInt(2, template.getRouteId());
                    pstmt.setInt(3, template.getPartId());
                    pstmt.setInt(4, Template.State.EXISTS);
                    pstmt.setInt(5, company);
                    return pstmt;
                }, keyHolder
        );

        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_FIELD,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setString(2, template.getFields().get(i).getName());
                        pstmt.setInt(3, template.getFields().get(i).getNum());
                        pstmt.setInt(4, template.getFields().get(i).getTypeId());
                        pstmt.setInt(5, template.getFields().get(i).getChoiceListId());
                    }
                    public int getBatchSize() {
                        return template.getFields().size();
                    }
                }
        );

        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_CONDITION,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setInt(2, template.getConditions().get(i).getCondNum());
                        pstmt.setInt(3, template.getConditions().get(i).getFieldNum());
                        pstmt.setString(4, template.getConditions().get(i).getCond());
                        pstmt.setString(5, template.getConditions().get(i).getValue());
                        pstmt.setInt(6, template.getConditions().get(i).getRouteId());
                    }
                    public int getBatchSize() {
                        return template.getConditions().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void update(Template template) throws DataAccessException {
        jdbcTemplate.update(UPDATE_TEMPLATE,
                template.getName(),
                template.getRouteId(),
                template.getPartId(),
                template.getId()
        );

        jdbcTemplate.update(DELETE_TEMPLATE_FIELD,
                template.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_TEMPLATE_FIELD,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, template.getId());
                        pstmt.setString(2, template.getFields().get(i).getName());
                        pstmt.setInt(3, template.getFields().get(i).getNum());
                        pstmt.setInt(4, template.getFields().get(i).getTypeId());
                        pstmt.setInt(5, template.getFields().get(i).getChoiceListId());
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
                        pstmt.setInt(1, template.getId());
                        pstmt.setInt(2, template.getConditions().get(i).getCondNum());
                        pstmt.setInt(3, template.getConditions().get(i).getFieldNum());
                        pstmt.setString(4, template.getConditions().get(i).getCond());
                        pstmt.setString(5, template.getConditions().get(i).getValue());
                        pstmt.setInt(6, template.getConditions().get(i).getRouteId());
                    }
                    public int getBatchSize() {
                        return template.getConditions().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void delete(int id) throws DataAccessException {
        jdbcTemplate.update(DELETE_TEMPLATE, Template.State.DELETED, id);
        jdbcTemplate.update(DELETE_TEMPLATE_FIELD, id);
        jdbcTemplate.update(DELETE_TEMPLATE_CONDITION, id);
    }
}
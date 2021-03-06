package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.DocumentDao;
import ru.pioneersystem.pioneer2.dao.exception.LockDaoException;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.*;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Repository(value = "documentDao")
public class DocumentDaoImpl implements DocumentDao {
    private static final String INSERT_FILE =
            "INSERT INTO DOC.FILES (NAME, MIME_TYPE, LENGTH, DATA) VALUES (?, ?, ?, ?)";
    private static final String INSERT_DOCUMENT =
            "INSERT INTO DOC.DOCUMENTS (NAME, STATUS, U_DATE, TEMPLATE, U_USER, DOC_GROUP, PUB_PART, " +
                    "ROUTE, COMPANY, I_DATE, I_USER) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_DOCUMENT_FIELD =
            "INSERT INTO DOC.DOCUMENTS_FIELD (ID, FIELD_NAME, FIELD_NUM, FIELD_TYPE, VALUE_TEXTFIELD, " +
                    "VALUE_LIST_SELECTED, VALUE_CALENDAR, VALUE_CHECKBOX, VALUE_TEXTAREA, VALUE_LIST, VALUE_FILE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_DOCUMENT_CONDITION =
            "INSERT INTO DOC.DOCUMENTS_COND (ID, COND_NUM, FIELD_NUM, COND, VALUE, ROUTE) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_FILE =
            "UPDATE DOC.FILES SET NAME = ?, MIME_TYPE = ?, LENGTH = ?, DATA = ? WHERE ID = ?";
    private static final String UPDATE_DOCUMENT =
            "UPDATE DOC.DOCUMENTS SET NAME = ?, U_DATE = ?, U_USER = ?, DOC_GROUP = ?, PUB_PART = ? WHERE ID = ? AND COMPANY = ?";
    private static final String LOCK_DOCUMENT =
            "SELECT U_DATE, U_USER FROM DOC.DOCUMENTS WHERE ID = ? AND COMPANY = ? FOR UPDATE";
    private static final String DELETE_DOCUMENT =
            "UPDATE DOC.DOCUMENTS SET STATUS = ?, U_DATE = ?, U_USER = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_DOCUMENT_FIELD = "DELETE FROM DOC.DOCUMENTS_FIELD WHERE ID = ?";
    private static final String DELETE_DOCUMENT_CONDITION = "DELETE FROM DOC.TEMPLATES_COND WHERE ID = ?";
    private static final String DELETE_DOCUMENT_FILE = "DELETE FROM DOC.FILES WHERE ID IN (SELECT VALUE_FILE " +
            "FROM DOC.DOCUMENTS_FIELD WHERE ID = ? AND FIELD_TYPE = ?)";
    private static final String PUBLICATE_DOCUMENT =
            "UPDATE DOC.DOCUMENTS SET PUB_PART = ?, U_DATE = ?, U_USER = ?, STATUS = ? WHERE ID = ? AND COMPANY = ?";
    private static final String SELECT_TEMPLATE = "SELECT ID, NAME, ROUTE FROM DOC.TEMPLATES WHERE ID = ? AND COMPANY = ?";
    private static final String SELECT_TEMPLATE_FIELD = "SELECT FIELD_NAME, FIELD_NUM, FIELD_TYPE, FIELD_LIST " +
            "FROM DOC.TEMPLATES_FIELD WHERE ID = ? ORDER BY FIELD_NUM ASC";
    private static final String SELECT_TEMPLATE_CONDITION = "SELECT COND_NUM, FIELD_NUM, COND, VALUE, ROUTE " +
            "FROM DOC.TEMPLATES_COND WHERE ID = ? ORDER BY COND_NUM ASC";
    private static final String SELECT_DOCUMENT = "SELECT ID, NAME, STATUS, U_DATE, TEMPLATE, U_USER, DOC_GROUP, " +
            "PUB_PART, ROUTE, I_DATE, I_USER FROM DOC.DOCUMENTS WHERE ID = ? AND COMPANY = ?";
    private static final String SELECT_DOCUMENT_FIELD = "SELECT FIELD_NAME, FIELD_NUM, FIELD_TYPE, VALUE_TEXTFIELD, " +
            "VALUE_LIST_SELECTED, VALUE_CALENDAR, VALUE_CHECKBOX, VALUE_TEXTAREA, VALUE_LIST, VALUE_FILE, " +
            "NAME AS FILE_NAME FROM DOC.DOCUMENTS_FIELD DF LEFT JOIN DOC.FILES F ON DF.VALUE_FILE = F.ID " +
            "WHERE DF.ID = ? ORDER BY FIELD_NUM ASC";
    private static final String SELECT_DOCUMENT_CONDITION = "SELECT COND_NUM, FIELD_NUM, COND, VALUE, ROUTE " +
            "FROM DOC.DOCUMENTS_COND WHERE ID = ? ORDER BY COND_NUM ASC";
    private static final String SELECT_ON_ROUTE_DOCUMENT_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, U_DATE FROM DOC.DOCUMENTS D LEFT JOIN DOC.DOCUMENTS_SIGN DS ON DS.ID = D.ID " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = D.DOC_GROUP WHERE DS.ACTIVE = 1 AND DS.ROLE_ID = ? AND DS.SIGNED = 0 " +
                    "AND DS.GROUP_ID IN (SELECT ID FROM DOC.GROUPS_USER WHERE USER_ID = ?) AND D.COMPANY = ? ORDER BY U_DATE";
    private static final String SELECT_DOCUMENT_LIST_BY_PART =
            "SELECT ID, NAME FROM DOC.DOCUMENTS WHERE PUB_PART = ? AND STATUS = ? AND COMPANY = ? ORDER BY NAME ASC";
    private static final String SELECT_MY_ON_DATE_DOCUMENT_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, DS.NAME AS STATUS_NAME, G.NAME AS GROUP_NAME, D.STATUS AS STATUS_ID " +
                    "FROM DOC.DOCUMENTS D LEFT JOIN DOC.DOCUMENTS_STATUS DS ON D.STATUS = DS.ID LEFT JOIN " +
                    "DOC.GROUPS G ON D.DOC_GROUP = G.ID WHERE DS.ID <> ? AND D.U_DATE >= ? AND D.U_DATE < ? " +
                    "AND D.DOC_GROUP IN (SELECT G.ID FROM DOC.GROUPS G, DOC.GROUPS_USER GU WHERE G.ID = GU.ID " +
                    "AND USER_ID = ? AND ROLE_ID = 9 AND STATE > 0) AND D.COMPANY = ? ORDER BY D.U_DATE DESC";
    private static final String SELECT_MY_ON_WORK_DOCUMENT_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, DS.NAME AS STATUS_NAME, G.NAME AS GROUP_NAME, D.STATUS AS STATUS_ID " +
                    "FROM DOC.DOCUMENTS D LEFT JOIN DOC.DOCUMENTS_STATUS DS ON D.STATUS = DS.ID LEFT JOIN " +
                    "DOC.GROUPS G ON D.DOC_GROUP = G.ID WHERE D.STATUS >= 9 AND D.DOC_GROUP IN (SELECT G.ID FROM " +
                    "DOC.GROUPS G, DOC.GROUPS_USER GU WHERE G.ID = GU.ID AND USER_ID = ? AND ROLE_ID = 9 " +
                    "AND STATE > 0) AND D.COMPANY = ? ORDER BY D.U_DATE DESC";
    private static final String UPDATE_DOCUMENT_PART_AND_STATUS =
            "UPDATE DOC.DOCUMENTS SET PUB_PART = 0, U_DATE = ?, U_USER = ?, STATUS = ? WHERE PUB_PART = ? " +
                    "AND STATUS = ? AND COMPANY = ?";
    private static final String SELECT_DOC_TO_CANCEL_BY_GROUP =
            "SELECT DISTINCT D.ID AS ID, NAME FROM DOC.DOCUMENTS D, DOC.DOCUMENTS_SIGN DS WHERE D.ID = DS.ID " +
                    "AND D.STATUS > 9 AND DS.GROUP_ID = ? AND COMPANY = ?";
    private static final String SELECT_DOC_TO_CANCEL_BY_ROLE =
            "SELECT DISTINCT D.ID AS ID, NAME FROM DOC.DOCUMENTS D, DOC.DOCUMENTS_SIGN DS WHERE D.ID = DS.ID " +
                    "AND D.STATUS > 9 AND DS.ROLE_ID = ? AND COMPANY = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Document getTemplateBased(int templateId, int companyId) throws DataAccessException {
        Document resultDocument = jdbcTemplate.query(SELECT_TEMPLATE,
                new Object[]{templateId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        Document document = new Document();
                        document.setName(rs.getString("NAME"));
                        document.setRouteId(rs.getInt("ROUTE"));
                        document.setTemplateId(rs.getInt("ID"));
                        return document;
                    } else {
                        throw new NotFoundDaoException("Not found Document with templateId = " + templateId +
                                " and companyId = " + companyId);
                    }
                }
        );

        ArrayList<Document.Field> resultFields = jdbcTemplate.query(SELECT_TEMPLATE_FIELD,
                new Object[]{templateId},
                rs -> {
                    ArrayList<Document.Field> fields = new ArrayList<>();
                    while(rs.next()){
                        Document.Field field = new Document.Field();
                        field.setName(rs.getString("FIELD_NAME"));
                        field.setNum(rs.getInt("FIELD_NUM"));
                        field.setTypeId(rs.getInt("FIELD_TYPE"));
                        field.setChoiceListId(rs.getObject("FIELD_LIST", Integer.class));

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
                        condition.setCond(rs.getString("COND"));
                        condition.setValue(rs.getString("VALUE"));
                        condition.setRouteId(rs.getInt("ROUTE"));

                        conditions.add(condition);
                    }
                    return conditions;
                }
        );

        resultDocument.setFields(resultFields);
        resultDocument.setConditions(resultConditions);

        return resultDocument;
    }

    @Override
    public Document get(int documentId, int companyId) throws DataAccessException {
        Document resultDocument = jdbcTemplate.query(SELECT_DOCUMENT,
                new Object[]{documentId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        Document document = new Document();
                        document.setId(rs.getInt("ID"));
                        document.setName(rs.getString("NAME"));
                        document.setStatusId(rs.getInt("STATUS"));
                        document.setChangeDate(Date.from(rs.getTimestamp("U_DATE").toInstant()));
                        document.setTemplateId(rs.getInt("TEMPLATE"));
                        document.setChangeUserId(rs.getInt("U_USER"));
                        document.setDocumentGroupId(rs.getInt("DOC_GROUP"));
                        document.setPartId(rs.getInt("PUB_PART"));
                        document.setRouteId(rs.getInt("ROUTE"));
                        document.setCreateDate(Date.from(rs.getTimestamp("I_DATE").toInstant()));
                        document.setCreateUserId(rs.getInt("I_USER"));
                        return document;
                    } else {
                        throw new NotFoundDaoException("Not found Document with documentId = " + documentId +
                                " and companyId = " + companyId);
                    }
                }
        );

        ArrayList<Document.Field> resultFields = jdbcTemplate.query(SELECT_DOCUMENT_FIELD,
                new Object[]{documentId},
                rs -> {
                    ArrayList<Document.Field> fields = new ArrayList<>();
                    while(rs.next()){
                        Document.Field field = new Document.Field();
                        field.setName(rs.getString("FIELD_NAME"));
                        field.setNum(rs.getInt("FIELD_NUM"));
                        int fieldType = rs.getInt("FIELD_TYPE");
                        field.setTypeId(fieldType);

                        switch (fieldType) {
                            case FieldType.Id.TEXT_STRING:
                                field.setValueTextField(rs.getString("VALUE_TEXTFIELD"));
                                break;
                            case FieldType.Id.CHOICE_LIST:
                                field.setValueChoiceList(rs.getString("VALUE_LIST_SELECTED"));
                                field.setChoiceListId(rs.getObject("VALUE_LIST", Integer.class));
                                List<String> choiceListValues = new ArrayList<>();
                                choiceListValues.add(rs.getString("VALUE_LIST_SELECTED"));
                                field.setChoiceListValues(choiceListValues);
                                break;
                            case FieldType.Id.CALENDAR:
                                Timestamp timestamp = rs.getTimestamp("VALUE_CALENDAR");
                                field.setValueCalendar(timestamp == null ? null : Date.from(timestamp.toInstant()));
                                break;
                            case FieldType.Id.CHECKBOX:
                                field.setValueCheckBox(rs.getObject("VALUE_CHECKBOX", Boolean.class));
                                break;
                            case FieldType.Id.TEXT_AREA:
                                field.setValueTextArea(rs.getString("VALUE_TEXTAREA"));
                                break;
                            case FieldType.Id.FILE:
                                field.setFileId(rs.getObject("VALUE_FILE", Integer.class));
                                field.setFileName(rs.getString("FILE_NAME"));
                                break;
                        }
                        fields.add(field);
                    }
                    return fields;
                }
        );

        List<Document.Condition> resultConditions = jdbcTemplate.query(SELECT_DOCUMENT_CONDITION,
                new Object[]{documentId},
                rs -> {
                    List<Document.Condition> conditions = new ArrayList<>();
                    while(rs.next()){
                        Document.Condition condition = new Document.Condition();
                        condition.setCondNum(rs.getInt("COND_NUM"));
                        condition.setFieldNum(rs.getInt("FIELD_NUM"));
                        condition.setCond(rs.getString("COND"));
                        condition.setValue(rs.getString("VALUE"));
                        condition.setRouteId(rs.getInt("ROUTE"));

                        conditions.add(condition);
                    }
                    return conditions;
                }
        );

        resultDocument.setFields(resultFields);
        resultDocument.setConditions(resultConditions);

        return resultDocument;
    }

    @Override
    public ArrayList<Document.Field> getDocFields(int documentId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_DOCUMENT_FIELD,
                new Object[]{documentId},
                rs -> {
                    ArrayList<Document.Field> fields = new ArrayList<>();
                    while(rs.next()){
                        Document.Field field = new Document.Field();
                        field.setName(rs.getString("FIELD_NAME"));
                        field.setNum(rs.getInt("FIELD_NUM"));
                        int fieldType = rs.getInt("FIELD_TYPE");
                        field.setTypeId(fieldType);

                        switch (fieldType) {
                            case FieldType.Id.TEXT_STRING:
                                field.setValueTextField(rs.getString("VALUE_TEXTFIELD"));
                                break;
                            case FieldType.Id.CHOICE_LIST:
                                field.setValueChoiceList(rs.getString("VALUE_LIST_SELECTED"));
                                field.setChoiceListId(rs.getObject("VALUE_LIST", Integer.class));
                                List<String> choiceListValues = new ArrayList<>();
                                choiceListValues.add(rs.getString("VALUE_LIST_SELECTED"));
                                field.setChoiceListValues(choiceListValues);
                                break;
                            case FieldType.Id.CALENDAR:
                                Timestamp timestamp = rs.getTimestamp("VALUE_CALENDAR");
                                field.setValueCalendar(timestamp == null ? null : Date.from(timestamp.toInstant()));
                                break;
                            case FieldType.Id.CHECKBOX:
                                field.setValueCheckBox(rs.getObject("VALUE_CHECKBOX", Boolean.class));
                                break;
                            case FieldType.Id.TEXT_AREA:
                                field.setValueTextArea(rs.getString("VALUE_TEXTAREA"));
                                break;
                            case FieldType.Id.FILE:
                                field.setFileId(rs.getObject("VALUE_FILE", Integer.class));
                                field.setFileName(rs.getString("FILE_NAME"));
                                break;
                        }
                        fields.add(field);
                    }
                    return fields;
                }
        );
    }

    @Override
    public List<Document> getOnRouteList(int roleId, int userId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ON_ROUTE_DOCUMENT_LIST,
                new Object[]{roleId, userId, companyId},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setId(rs.getInt("ID"));
                    document.setName(rs.getString("NAME"));
                    document.setChangeDate(Date.from(rs.getTimestamp("U_DATE").toInstant()));
                    return document;
                }
        );
    }

    @Override
    public List<Document> getListByPartId(int partId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_DOCUMENT_LIST_BY_PART,
                new Object[]{partId, Status.Id.PUBLISHED, companyId},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setId(rs.getInt("ID"));
                    document.setName(rs.getString("NAME"));
                    return document;
                }
        );
    }

    @Override
    public List<Document> getMyOnDateList(Date beginDate, Date endDate, int userId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_MY_ON_DATE_DOCUMENT_LIST,
                new Object[]{Status.Id.DELETED, beginDate, endDate, userId, companyId},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setId(rs.getInt("ID"));
                    document.setName(rs.getString("NAME"));
                    document.setStatusName(rs.getString("STATUS_NAME"));
                    document.setDocumentGroupName(rs.getString("GROUP_NAME"));
                    document.setStatusId(rs.getInt("STATUS_ID"));
                    return document;
                }
        );
    }

    @Override
    public List<Document> getMyOnWorkingList(int userId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_MY_ON_WORK_DOCUMENT_LIST,
                new Object[]{userId, companyId},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setId(rs.getInt("ID"));
                    document.setName(rs.getString("NAME"));
                    document.setStatusName(rs.getString("STATUS_NAME"));
                    document.setDocumentGroupName(rs.getString("GROUP_NAME"));
                    document.setStatusId(rs.getInt("STATUS_ID"));
                    return document;
                }
        );
    }

    @Override
    public List<String> getDocToCancelByGroup(int groupId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_DOC_TO_CANCEL_BY_GROUP,
                new Object[]{groupId, companyId},
                (rs, rowNum) -> rs.getString("NAME") + " (Id: " + rs.getString("ID") + ")"
        );
    }

    @Override
    public List<String> getDocToCancelByRole(int roleId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_DOC_TO_CANCEL_BY_ROLE,
                new Object[]{roleId, companyId},
                (rs, rowNum) -> rs.getString("NAME") + " (Id: " + rs.getString("ID") + ")"
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void cancelPublish(List<Part> parts, int userId, int companyId) throws DataAccessException {
        Timestamp timestamp = Timestamp.from((new Date()).toInstant());
        jdbcTemplate.batchUpdate(UPDATE_DOCUMENT_PART_AND_STATUS,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setTimestamp(1, timestamp);
                        pstmt.setInt(2, userId);
                        pstmt.setInt(3, Status.Id.COMPLETED);
                        pstmt.setInt(4, parts.get(i).getId());
                        pstmt.setInt(5, Status.Id.PUBLISHED);
                        pstmt.setInt(6, companyId);
                    }
                    public int getBatchSize() {
                        return parts.size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public int create(Document document, int userId, int companyId) throws DataAccessException {
        Date currDate = new Date();

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_DOCUMENT, new String[] {"id"});
                    pstmt.setString(1, document.getName());
                    pstmt.setInt(2, Status.Id.CREATED);
                    pstmt.setTimestamp(3, Timestamp.from(currDate.toInstant()));
                    pstmt.setInt(4, document.getTemplateId());
                    pstmt.setInt(5, userId);
                    pstmt.setInt(6, document.getDocumentGroupId());
                    pstmt.setInt(7, 0);
                    pstmt.setInt(8, document.getRouteId());
                    pstmt.setInt(9, companyId);
                    pstmt.setTimestamp(10, Timestamp.from(currDate.toInstant()));
                    pstmt.setInt(11, userId);
                    return pstmt;
                }, keyHolder
        );

        document.setId(keyHolder.getKey().intValue());
        document.setChangeUserId(userId);
        document.setChangeDate(currDate);

        Map<Integer, Integer> fileIds = new HashMap<>();
        for (Document.Field field: document.getFields()) {
            if (field.getFile() == null) {
                fileIds.put(field.getNum(), null);
                continue;
            }

            KeyHolder fileKeyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement pstmt = connection.prepareStatement(INSERT_FILE, new String[] {"id"});
                        pstmt.setString(1, field.getFile().getName());
                        pstmt.setString(2, field.getFile().getMimeType());
                        pstmt.setLong(3, field.getFile().getLength());
                        pstmt.setBinaryStream(4, new ByteArrayInputStream(field.getFile().getContent()), field.getFile().getLength());
                        return pstmt;
                    }, fileKeyHolder
            );
            fileIds.put(field.getNum(), fileKeyHolder.getKey().intValue());
        }

        jdbcTemplate.batchUpdate(INSERT_DOCUMENT_FIELD,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Field documentField = document.getFields().get(i);

                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setString(2, documentField.getName());
                        pstmt.setInt(3, documentField.getNum());
                        pstmt.setInt(4, documentField.getTypeId());
                        pstmt.setObject(5, documentField.getValueTextField());
                        pstmt.setObject(6, documentField.getValueChoiceList());
                        pstmt.setObject(7, documentField.getValueCalendar() == null ? null : Timestamp.from(documentField.getValueCalendar().toInstant()));
                        pstmt.setObject(8, documentField.getValueCheckBox() == null ? null : documentField.getValueCheckBox() ? 1 : 0);
                        pstmt.setObject(9, documentField.getValueTextArea());
                        pstmt.setObject(10, documentField.getChoiceListId());
                        pstmt.setObject(11, fileIds.get(documentField.getNum()));
                    }
                    public int getBatchSize() {
                        return document.getFields().size();
                    }
                }
        );

        jdbcTemplate.batchUpdate(INSERT_DOCUMENT_CONDITION,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Condition documentCondition = document.getConditions().get(i);

                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setInt(2, documentCondition.getCondNum());
                        pstmt.setInt(3, documentCondition.getFieldNum());
                        pstmt.setString(4, documentCondition.getCond());
                        pstmt.setString(5, documentCondition.getValue());
                        pstmt.setInt(6, documentCondition.getRouteId());
                    }
                    public int getBatchSize() {
                        return document.getConditions().size();
                    }
                }
        );
        return keyHolder.getKey().intValue();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void update(Document document, int userId, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_DOCUMENT,
                document.getName(),
                Timestamp.from((new Date()).toInstant()),
                userId,
                document.getDocumentGroupId(),
                document.getPartId(),
                document.getId(),
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Document with documentId = " + document.getId() +
                    " and companyId = " + companyId);
        }

        for (Document.Field field: document.getFields()) {
            if (field.getTypeId() == FieldType.Id.FILE && field.getFile() != null) {
                jdbcTemplate.update(
                        connection -> {
                            PreparedStatement pstmt = connection.prepareStatement(UPDATE_FILE);
                            pstmt.setString(1, field.getFile().getName());
                            pstmt.setString(2, field.getFile().getMimeType());
                            pstmt.setLong(3, field.getFile().getLength());
                            pstmt.setBinaryStream(4, new ByteArrayInputStream(field.getFile().getContent()), field.getFile().getLength());
                            pstmt.setInt(5, field.getFileId());
                            return pstmt;
                        }
                );
            }
        }

        jdbcTemplate.update(DELETE_DOCUMENT_FIELD,
                document.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_DOCUMENT_FIELD,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Field documentField = document.getFields().get(i);

                        pstmt.setInt(1, document.getId());
                        pstmt.setString(2, documentField.getName());
                        pstmt.setInt(3, documentField.getNum());
                        pstmt.setInt(4, documentField.getTypeId());
                        pstmt.setObject(5, documentField.getValueTextField());
                        pstmt.setObject(6, documentField.getValueChoiceList());
                        pstmt.setObject(7, documentField.getValueCalendar() == null ? null : Timestamp.from(documentField.getValueCalendar().toInstant()));
                        pstmt.setObject(8, documentField.getValueCheckBox() == null ? null : documentField.getValueCheckBox() ? 1 : 0);
                        pstmt.setObject(9, documentField.getValueTextArea());
                        pstmt.setObject(10, documentField.getChoiceListId());
                        pstmt.setObject(11, documentField.getFileId());
                    }
                    public int getBatchSize() {
                        return document.getFields().size();
                    }
                }
        );

        jdbcTemplate.update(DELETE_DOCUMENT_CONDITION,
                document.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_DOCUMENT_CONDITION,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Condition documentCondition = document.getConditions().get(i);

                        pstmt.setInt(1, document.getId());
                        pstmt.setInt(2, documentCondition.getCondNum());
                        pstmt.setInt(3, documentCondition.getFieldNum());
                        pstmt.setString(4, documentCondition.getCond());
                        pstmt.setString(5, documentCondition.getValue());
                        pstmt.setInt(6, documentCondition.getRouteId());
                    }
                    public int getBatchSize() {
                        return document.getConditions().size();
                    }
                }
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(int documentId, int userId, int companyId) throws DataAccessException {
        jdbcTemplate.update(DELETE_DOCUMENT,
                Status.Id.DELETED,
                Timestamp.from((new Date()).toInstant()),
                userId,
                documentId,
                companyId
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void publish(Document document, int userId, int companyId, boolean isPublic) throws DataAccessException {
        jdbcTemplate.update(PUBLICATE_DOCUMENT,
                document.getPartId(),
                Timestamp.from((new Date()).toInstant()),
                userId,
                isPublic ? Status.Id.PUBLISHED : Status.Id.COMPLETED,
                document.getId(),
                companyId
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void lock(Document document, int companyId) throws DataAccessException {
        jdbcTemplate.query(LOCK_DOCUMENT,
                new Object[]{document.getId(), companyId},
                (rs) -> {
                    if (rs.next()) {
                        Date uDate = Date.from(rs.getTimestamp("U_DATE").toInstant());
                        int uUserId = rs.getInt("U_USER");

                        if (document.getChangeDate().compareTo(uDate) != 0) {
                            throw new LockDaoException("Document with documentId = " + document.getId() +
                                    " was changed while attempting to lock", uUserId, uDate);
                        }
                        return null;
                    } else {
                        throw new NotFoundDaoException("Not found Document with documentId = " +
                                document.getId() + " and companyId = " + companyId + " while attempting to lock");
                    }
                }
        );
    }
}
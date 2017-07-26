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
            "INSERT INTO DOC.DOCUMENTS (NAME, STATUS, U_DATE, TEMPLATE, U_USER, DOC_GROUP, PUB_PART, ROUTE, COMPANY) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_DOCUMENT_FIELD =
            "INSERT INTO DOC.DOCUMENTS_FIELD (ID, FIELD_NAME, FIELD_NUM, FIELD_TYPE, VALUE_TEXTFIELD, " +
                    "VALUE_LIST_SELECTED, VALUE_CALENDAR, VALUE_CHECKBOX, VALUE_TEXTAREA, VALUE_LIST, VALUE_FILE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_DOCUMENT_CONDITION =
            "INSERT INTO DOC.DOCUMENTS_COND (ID, COND_NUM, FIELD_NUM, COND, VALUE, ROUTE) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_FILE =
            "UPDATE DOC.FILES SET NAME = ?, MIME_TYPE = ?, LENGTH = ?, DATA = ? WHERE ID = ?";
    private static final String UPDATE_DOCUMENT =
            "UPDATE DOC.DOCUMENTS SET NAME = ?, U_DATE = ?, U_USER = ?, DOC_GROUP = ?, PUB_PART = ? WHERE ID = ?";
    private static final String LOCK_DOCUMENT = "SELECT U_DATE, U_USER FROM DOC.DOCUMENTS WHERE ID = ? AND COMPANY = ? FOR UPDATE";
    private static final String DELETE_DOCUMENT = "UPDATE DOC.DOCUMENTS SET STATUS = ?, U_DATE = ?, U_USER = ? WHERE ID = ?";
    private static final String DELETE_DOCUMENT_FIELD = "DELETE FROM DOC.DOCUMENTS_FIELD WHERE ID = ?";
    private static final String DELETE_DOCUMENT_CONDITION = "DELETE FROM DOC.TEMPLATES_COND WHERE ID = ?";
    private static final String DELETE_DOCUMENT_FILE = "DELETE FROM DOC.FILES WHERE ID IN (SELECT VALUE_FILE " +
            "FROM DOC.DOCUMENTS_FIELD WHERE ID = ? AND FIELD_TYPE = ?)";
    private static final String PUBLICATE_DOCUMENT =
            "UPDATE DOC.DOCUMENTS SET PUB_PART = ?, U_DATE = ?, U_USER = ?, STATUS = ? WHERE ID = ?";
    private static final String SELECT_TEMPLATE = "SELECT ID, NAME, ROUTE FROM DOC.TEMPLATES WHERE ID = ?";
    private static final String SELECT_TEMPLATE_FIELD = "SELECT FIELD_NAME, FIELD_NUM, FIELD_TYPE, FIELD_LIST " +
            "FROM DOC.TEMPLATES_FIELD WHERE ID = ? ORDER BY FIELD_NUM ASC";
    private static final String SELECT_TEMPLATE_CONDITION = "SELECT COND_NUM, FIELD_NUM, COND, VALUE, ROUTE " +
            "FROM DOC.TEMPLATES_COND WHERE ID = ?";
    private static final String SELECT_DOCUMENT = "SELECT ID, NAME, STATUS, U_DATE, TEMPLATE, U_USER, DOC_GROUP, " +
            "PUB_PART, ROUTE FROM DOC.DOCUMENTS WHERE ID = ?";
    private static final String SELECT_DOCUMENT_FIELD = "SELECT FIELD_NAME, FIELD_NUM, FIELD_TYPE, VALUE_TEXTFIELD, " +
            "VALUE_LIST_SELECTED, VALUE_CALENDAR, VALUE_CHECKBOX, VALUE_TEXTAREA, VALUE_LIST, VALUE_FILE, " +
            "NAME AS FILE_NAME FROM DOC.DOCUMENTS_FIELD DF LEFT JOIN DOC.FILES F ON DF.VALUE_FILE = F.ID " +
            "WHERE DF.ID = ? ORDER BY FIELD_NUM ASC";
    private static final String SELECT_DOCUMENT_CONDITION = "SELECT COND_NUM, FIELD_NUM, COND, VALUE, ROUTE " +
            "FROM DOC.DOCUMENTS_COND WHERE ID = ?";
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
    public Document getTemplateBased(int templateId, Map<Integer, List<String>> choiceLists) throws DataAccessException {
        Document resultDocument = jdbcTemplate.queryForObject(SELECT_TEMPLATE,
                new Object[]{templateId},
                (rs, rowNum) -> {
                    Document document = new Document();
                    document.setName(rs.getString("NAME"));
                    document.setRouteId(rs.getInt("ROUTE"));
                    document.setTemplateId(rs.getInt("ID"));
                    return document;
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
                        field.setChoiceListId(rs.getObject("FIELD_LIST", Integer.class));
                        field.setChoiceListValues(choiceLists.get(rs.getObject("FIELD_LIST", Integer.class)));

                        fields.add(field);
                    }
                    return fields;
                }
        );

        List<Document.Condition> resultConditions = jdbcTemplate.query(SELECT_TEMPLATE_CONDITION,
                new Object[]{templateId},
                rs -> {
                    List<Document.Condition> conditions = new LinkedList<>();
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
    public Document get(int documentId) throws DataAccessException {
        return getForEdit(documentId, new HashMap<>());
    }

    @Override
    public Document getForEdit(int documentId, Map<Integer, List<String>> choiceLists) throws DataAccessException {
        Document resultDocument = jdbcTemplate.queryForObject(SELECT_DOCUMENT,
                new Object[]{documentId},
                (rs, rowNum) -> {
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
                    return document;
                }
        );

        LinkedList<Document.Field> resultFields = jdbcTemplate.query(SELECT_DOCUMENT_FIELD,
                new Object[]{documentId},
                rs -> {
                    LinkedList<Document.Field> fields = new LinkedList<>();
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
                            case FieldType.Id.LIST:
                                field.setValueChoiceList(rs.getString("VALUE_LIST_SELECTED"));
                                field.setChoiceListId(rs.getObject("VALUE_LIST", Integer.class));
                                field.setChoiceListValues(choiceLists.get(documentId));
                                break;
                            case FieldType.Id.CALENDAR:
                                field.setValueCalendar(Date.from(rs.getTimestamp("VALUE_CALENDAR").toInstant()));
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
                    List<Document.Condition> conditions = new LinkedList<>();
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
    public List<Document> getOnRouteList(int roleId, int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_ON_ROUTE_DOCUMENT_LIST,
                new Object[]{roleId, userId},
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
    public void create(Document document, int userId, int company) throws DataAccessException {
        document.setChangeUserId(userId);
        document.setChangeDate(new Date());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_DOCUMENT, new String[] {"id"});
                    pstmt.setString(1, document.getName());
                    pstmt.setInt(2, Status.Id.CREATED);
                    pstmt.setTimestamp(3, Timestamp.from(document.getChangeDate().toInstant()));
                    pstmt.setInt(4, document.getTemplateId());
                    pstmt.setInt(5, document.getChangeUserId());
                    pstmt.setInt(6, document.getDocumentGroupId());
                    pstmt.setInt(7, 0);
                    pstmt.setInt(8, document.getRouteId());
                    pstmt.setInt(9, company);
                    return pstmt;
                }, keyHolder
        );

        document.setId(keyHolder.getKey().intValue());

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
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void update(Document document, int userId) throws DataAccessException {
        jdbcTemplate.update(UPDATE_DOCUMENT,
                document.getName(),
                Timestamp.from((new Date()).toInstant()),
                userId,
                document.getDocumentGroupId(),
                document.getPartId(),
                document.getId()
        );

        jdbcTemplate.batchUpdate(UPDATE_FILE,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        Document.Field field = document.getFields().get(i);
                        if (field.getTypeId() == FieldType.Id.FILE && field.getFile() != null) {
                            pstmt.setString(1, field.getFile().getName());
                            pstmt.setString(2, field.getFile().getMimeType());
                            pstmt.setLong(3, field.getFile().getLength());
                            pstmt.setBinaryStream(4, new ByteArrayInputStream(field.getFile().getContent()), field.getFile().getLength());
                            pstmt.setInt(5, field.getFileId());
                        }
                    }
                    public int getBatchSize() {
                        return document.getFields().size();
                    }
                }
        );

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
    public void delete(int documentId, int userId) throws DataAccessException {
        jdbcTemplate.update(DELETE_DOCUMENT,
                Status.Id.DELETED,
                Timestamp.from((new Date()).toInstant()),
                userId,
                documentId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void publish(int documentId, int userId, int partId, boolean isPublic) throws DataAccessException {
        jdbcTemplate.update(PUBLICATE_DOCUMENT,
                partId,
                Timestamp.from((new Date()).toInstant()),
                userId,
                isPublic ? Status.Id.PUBLISHED : Status.Id.COMPLETED,
                documentId);
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
                    } else {
                        throw new NotFoundDaoException("Not found Document with documentId = " +
                                document.getId() + " and companyId = " + companyId + " while attempting to lock");
                    }
                }
        );
    }
}
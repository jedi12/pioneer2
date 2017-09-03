package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.pioneersystem.pioneer2.dao.SearchDao;
import ru.pioneersystem.pioneer2.dao.exception.TooManyRowsDaoException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.SearchDoc;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository(value = "searchDao")
public class SearchDaoImpl implements SearchDao {
    private static final String SELECT_USER_PUB_DOC_LIST =
            "SELECT DISTINCT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, " +
                        "D.DOC_GROUP AS DOC_GROUP, D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.PARTS P ON P.ID = D.PUB_PART " +
                    "LEFT JOIN DOC.PARTS_GROUP PG ON PG.ID = D.PUB_PART " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = GROUP_ID " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON GU.ID = GROUP_ID " +
                    "WHERE STATUS = 5 AND P.STATE > 0 AND TYPE = 2 AND (USER_ID = ? OR GROUP_ID IS NULL) AND D.COMPANY = ?";
    private static final String SELECT_MY_DOC_LIST =
            "SELECT DISTINCT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, " +
                        "D.DOC_GROUP AS DOC_GROUP, D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = DOC_GROUP " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON GU.ID = DOC_GROUP " +
                    "WHERE ROLE_ID = 9 AND USER_ID = ? AND D.COMPANY = ?";
    private static final String SELECT_ON_ROUTE_LIST =
            "SELECT DISTINCT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, " +
                        "D.DOC_GROUP AS DOC_GROUP, D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.DOCUMENTS_SIGN DS ON DS.ID = D.ID " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON GU.ID = GROUP_ID " +
                    "WHERE USER_ID = ? AND D.COMPANY = ?";
    private static final String SELECT_ALL_USER_ALLOWED_DOC_LIST =
            "SELECT DOCS.ID AS ID, DOCS.NAME AS DOC_NAME, DS.NAME AS STATUS_NAME, DOCS.STATUS AS STATUS, " +
                    "G.NAME AS GROUP_NAME, DOCS.DOC_GROUP AS DOC_GROUP, DOCS.U_DATE AS U_DATE, " +
                    "DOCS.TEMPLATE AS TEMPLATE FROM (" +  SELECT_USER_PUB_DOC_LIST + " UNION " + SELECT_MY_DOC_LIST +
                    " UNION " + SELECT_ON_ROUTE_LIST + ") DOCS LEFT JOIN DOC.DOCUMENTS_STATUS DS ON DS.ID = DOCS.STATUS " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = DOCS.DOC_GROUP";
    private static final String SELECT_FOR_ADMIN =
            "SELECT DISTINCT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, " +
                    "D.DOC_GROUP AS DOC_GROUP, D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE FROM DOC.DOCUMENTS D " +
                    "WHERE D.COMPANY = ?";
    private static final String SELECT_ALL_ADMIN_ALLOWED_DOC_LIST =
            "SELECT DOCS.ID AS ID, DOCS.NAME AS DOC_NAME, DS.NAME AS STATUS_NAME, DOCS.STATUS AS STATUS, " +
                    "G.NAME AS GROUP_NAME, DOCS.DOC_GROUP AS DOC_GROUP, DOCS.U_DATE AS U_DATE, " +
                    "DOCS.TEMPLATE AS TEMPLATE FROM (" +  SELECT_FOR_ADMIN + ") DOCS " +
                    "LEFT JOIN DOC.DOCUMENTS_STATUS DS ON DS.ID = DOCS.STATUS " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = DOCS.DOC_GROUP";
    private static final String SELECT_FOR_SUPER =
            "SELECT DISTINCT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, " +
                    "D.DOC_GROUP AS DOC_GROUP, D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE FROM DOC.DOCUMENTS D ";
    private static final String SELECT_ALL_SUPER_ALLOWED_DOC_LIST =
            "SELECT DOCS.ID AS ID, DOCS.NAME AS DOC_NAME, DS.NAME AS STATUS_NAME, DOCS.STATUS AS STATUS, " +
                    "G.NAME AS GROUP_NAME, DOCS.DOC_GROUP AS DOC_GROUP, DOCS.U_DATE AS U_DATE, " +
                    "DOCS.TEMPLATE AS TEMPLATE FROM (" +  SELECT_FOR_SUPER + ") DOCS " +
                    "LEFT JOIN DOC.DOCUMENTS_STATUS DS ON DS.ID = DOCS.STATUS " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = DOCS.DOC_GROUP";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Document> findForSuperList(SearchDoc searchDoc) throws DataAccessException {
        List<Object> params = new ArrayList<>();
        String query = createQuery(searchDoc, SELECT_ALL_SUPER_ALLOWED_DOC_LIST, params);
        return findList(query, params);
    }

    @Override
    public List<Document> findForAdminList(SearchDoc searchDoc, int companyId) throws DataAccessException {
        List<Object> params = new ArrayList<>(Arrays.asList(companyId));
        String query = createQuery(searchDoc, SELECT_ALL_ADMIN_ALLOWED_DOC_LIST, params);
        return findList(query, params);
    }

    @Override
    public List<Document> findForUserList(SearchDoc searchDoc, int userId, int companyId) throws DataAccessException {
        List<Object> params = new ArrayList<>(Arrays.asList(userId, companyId, userId, companyId, userId, companyId));
        String query = createQuery(searchDoc, SELECT_ALL_USER_ALLOWED_DOC_LIST, params);
        return findList(query, params);
    }

    public List<Document> findList(String query, List<Object> params) throws DataAccessException {
        return jdbcTemplate.query(query, params.toArray(),
                (rs) -> {
                    int count = 0;
                    List<Document> documents = new ArrayList<>();
                    while(rs.next()){
                        Document document = new Document();
                        document.setId(rs.getInt("ID"));
                        document.setName(rs.getString("NAME"));
                        document.setStatusId(rs.getInt("STATUS"));
                        document.setStatusName(rs.getString("STATUS_NAME"));
                        document.setDocumentGroupName(rs.getString("GROUP_NAME"));
                        document.setChangeDate(Date.from(rs.getTimestamp("U_DATE").toInstant()));

                        documents.add(document);
                        count = count + 1;

                        if (count >= 1000) {
                            throw new TooManyRowsDaoException("Over 1000 documents found", documents);
                        }
                    }

                    return documents;
                }
        );
    }

    private String createQuery(SearchDoc searchDoc, String baseQuery, List<Object> params) {
        String query;

        if (searchDoc.isById()) {
            query = baseQuery + " WHERE DOCS.ID = ?";
            params.add(searchDoc.getId());
        } else {
            query = baseQuery + " WHERE DOCS.U_DATE >= ? AND DOCS.U_DATE < ?";
            Timestamp fromDate = Timestamp.from(searchDoc.getFromDate().toInstant());
            Timestamp toDate = Timestamp.from(searchDoc.getToDate().toInstant().plus(1, ChronoUnit.DAYS));
            params.add(fromDate);
            params.add(toDate);

            if (searchDoc.isByName()) {
                query = query + " AND DOCS.NAME LIKE ?";
                params.add("%" + searchDoc.getName().trim() + "%");
            }

            if (searchDoc.isByContent()) {
                query = query + " AND DOCS.ID IN (SELECT DISTINCT ID FROM DOC.DOCUMENTS_FIELD WHERE FIELD_NAME LIKE ? " +
                        "OR VALUE_TEXTFIELD LIKE ? OR VALUE_LIST_SELECTED LIKE ? OR VALUE_TEXTAREA LIKE ?)";
                params.add("%" + searchDoc.getContent().trim() + "%");
                params.add("%" + searchDoc.getContent().trim() + "%");
                params.add("%" + searchDoc.getContent().trim() + "%");
                params.add("%" + searchDoc.getContent().trim() + "%");
            }

            if (searchDoc.isByTemplate()) {
                query = query + " AND DOCS.TEMPLATE = ?";
                params.add(searchDoc.getTemplateId());
            }

            if (searchDoc.isByStatus()) {
                query = query + " AND DOCS.STATUS = ?";
                params.add(searchDoc.getStatusId());
            }

            if (searchDoc.isByOwner()) {
                query = query + " AND DOCS.DOC_GROUP = ?";
                params.add(searchDoc.getOwnerId());
            }
        }
        return query;
    }
}
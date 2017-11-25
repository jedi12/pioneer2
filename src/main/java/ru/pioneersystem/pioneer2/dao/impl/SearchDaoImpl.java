package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.pioneersystem.pioneer2.dao.SearchDao;
import ru.pioneersystem.pioneer2.dao.exception.TooManyRowsDaoException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.SearchFilter;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository(value = "searchDao")
public class SearchDaoImpl implements SearchDao {
    private static final String SELECT_USER_PUB_DOC_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, D.DOC_GROUP AS DOC_GROUP, " +
                    "D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE, D.COMPANY AS COMPANY_ID FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.PARTS P ON P.ID = D.PUB_PART " +
                    "LEFT JOIN DOC.PARTS_GROUP PG ON PG.ID = D.PUB_PART " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = GROUP_ID " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON GU.ID = GROUP_ID " +
                    "WHERE STATUS = 8 AND P.STATE > 0 AND TYPE = 2 AND (USER_ID = ? OR GROUP_ID IS NULL) AND D.COMPANY = ?";
    private static final String SELECT_MY_DOC_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, D.DOC_GROUP AS DOC_GROUP, " +
                    "D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE, D.COMPANY AS COMPANY_ID FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = DOC_GROUP " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON GU.ID = DOC_GROUP " +
                    "WHERE ROLE_ID = 9 AND USER_ID = ? AND D.COMPANY = ?";
    private static final String SELECT_ON_ROUTE_LIST =
            "SELECT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, D.DOC_GROUP AS DOC_GROUP, " +
                    "D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE, D.COMPANY AS COMPANY_ID FROM DOC.DOCUMENTS D " +
                    "LEFT JOIN DOC.DOCUMENTS_SIGN DS ON DS.ID = D.ID " +
                    "LEFT JOIN DOC.GROUPS_USER GU ON GU.ID = GROUP_ID " +
                    "WHERE USER_ID = ? AND D.COMPANY = ?";
    private static final String SELECT_ALL_USER_ALLOWED_DOC_LIST =
            "SELECT DISTINCT DOCS.ID AS ID, DOCS.NAME AS DOC_NAME, DS.NAME AS STATUS_NAME, DOCS.STATUS AS STATUS, " +
                    "G.NAME AS GROUP_NAME, DOCS.DOC_GROUP AS DOC_GROUP, DOCS.U_DATE AS U_DATE, DOCS.TEMPLATE AS TEMPLATE, " +
                    "DOCS.COMPANY_ID AS COMPANY_ID FROM (" +  SELECT_USER_PUB_DOC_LIST + " UNION " + SELECT_MY_DOC_LIST +
                    " UNION " + SELECT_ON_ROUTE_LIST + ") DOCS LEFT JOIN DOC.DOCUMENTS_STATUS DS ON DS.ID = DOCS.STATUS " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = DOCS.DOC_GROUP LEFT JOIN DOC.DOCUMENTS_FIELD DF ON DF.ID = DOCS.ID";
    private static final String SELECT_FOR_ADMIN =
            "SELECT D.ID AS ID, D.NAME AS NAME, D.STATUS AS STATUS, D.DOC_GROUP AS DOC_GROUP, " +
                    "D.U_DATE AS U_DATE, D.TEMPLATE AS TEMPLATE, D.COMPANY AS COMPANY_ID FROM DOC.DOCUMENTS D " +
                    "WHERE D.COMPANY = ?";
    private static final String SELECT_ALL_ADMIN_ALLOWED_DOC_LIST =
            "SELECT DISTINCT DOCS.ID AS ID, DOCS.NAME AS DOC_NAME, DS.NAME AS STATUS_NAME, DOCS.STATUS AS STATUS, " +
                    "G.NAME AS GROUP_NAME, DOCS.DOC_GROUP AS DOC_GROUP, DOCS.U_DATE AS U_DATE, " +
                    "DOCS.TEMPLATE AS TEMPLATE, DOCS.COMPANY_ID AS COMPANY_ID FROM (" +  SELECT_FOR_ADMIN + ") DOCS " +
                    "LEFT JOIN DOC.DOCUMENTS_STATUS DS ON DS.ID = DOCS.STATUS " +
                    "LEFT JOIN DOC.GROUPS G ON G.ID = DOCS.DOC_GROUP LEFT JOIN DOC.DOCUMENTS_FIELD DF ON DF.ID = DOCS.ID";
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Document> findForAdminList(SearchFilter searchFilter, int companyId) throws DataAccessException {
        List<Object> params = new ArrayList<>(Arrays.asList(companyId));
        String query = createQuery(searchFilter, SELECT_ALL_ADMIN_ALLOWED_DOC_LIST, params);
        return findList(query, params);
    }

    @Override
    public List<Document> findForUserList(SearchFilter searchFilter, int userId, int companyId) throws DataAccessException {
        List<Object> params = new ArrayList<>(Arrays.asList(userId, companyId, userId, companyId, userId, companyId));
        String query = createQuery(searchFilter, SELECT_ALL_USER_ALLOWED_DOC_LIST, params);
        return findList(query, params);
    }

    private List<Document> findList(String query, List<Object> params) throws DataAccessException {
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
                        document.setCompanyId(rs.getInt("COMPANY_ID"));

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

    private String createQuery(SearchFilter searchFilter, String baseQuery, List<Object> params) {
        String query;

        if (searchFilter.isById()) {
            query = baseQuery + " WHERE DOCS.ID = ?";
            params.add(searchFilter.getId());
        } else {
            query = baseQuery + " WHERE DOCS.U_DATE >= ? AND DOCS.U_DATE < ?";
            Timestamp fromDate = Timestamp.from(searchFilter.getFromDate().toInstant());
            Timestamp toDate = Timestamp.from(searchFilter.getToDate().toInstant().plus(1, ChronoUnit.DAYS));
            params.add(fromDate);
            params.add(toDate);

            if (searchFilter.isByName()) {
                query = query + " AND DOCS.NAME LIKE ?";
                params.add("%" + searchFilter.getName().trim() + "%");
            }

            if (searchFilter.isByContent()) {
                query = query + " AND (DF.FIELD_NAME LIKE ? OR DF.VALUE_TEXTFIELD LIKE ? " +
                        "OR DF.VALUE_LIST_SELECTED LIKE ? OR DF.VALUE_TEXTAREA LIKE ?)";
                params.add("%" + searchFilter.getContent().trim() + "%");
                params.add("%" + searchFilter.getContent().trim() + "%");
                params.add("%" + searchFilter.getContent().trim() + "%");
                params.add("%" + searchFilter.getContent().trim() + "%");
            }

            if (searchFilter.isByTemplate()) {
                query = query + " AND DOCS.TEMPLATE = ?";
                params.add(searchFilter.getTemplateId());
            }

            if (searchFilter.isByStatus()) {
                query = query + " AND DOCS.STATUS = ?";
                params.add(searchFilter.getStatusId());
            }

            if (searchFilter.isByOwner()) {
                query = query + " AND DOCS.DOC_GROUP = ?";
                params.add(searchFilter.getOwnerId());
            }
        }
        query = query + " FETCH FIRST 1000 ROWS ONLY";

        return query;
    }
}
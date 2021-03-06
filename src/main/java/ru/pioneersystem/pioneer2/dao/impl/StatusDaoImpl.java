package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.pioneersystem.pioneer2.dao.StatusDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Status;

import java.util.List;

@Repository(value = "statusDao")
public class StatusDaoImpl implements StatusDao {
    private static final String SELECT_STATUS =
            "SELECT ID, NAME, STATE, TYPE FROM DOC.DOCUMENTS_STATUS WHERE ID = ? AND COMPANY IN (0, ?)";
    private static final String SELECT_STATUS_LIST =
            "SELECT ID, NAME, STATE, TYPE FROM DOC.DOCUMENTS_STATUS WHERE STATE > 0 AND COMPANY = ? OR STATE = ? ORDER BY NAME ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Status get(int statusId, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_STATUS,
                new Object[]{statusId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        Status status = new Status();
                        status.setId(rs.getInt("ID"));
                        status.setName(rs.getString("NAME"));
                        status.setState(rs.getInt("STATE"));
                        status.setType(rs.getInt("TYPE"));
                        return status;
                    } else {
                        throw new NotFoundDaoException("Not found Status with statusId = " + statusId +
                                " and companyId = " + companyId);
                    }
                }
        );
    }

    @Override
    public List<Status> getList(int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_STATUS_LIST,
                new Object[]{companyId, Status.State.SYSTEM},
                (rs, rowNum) -> {
                    Status status = new Status();
                    status.setId(rs.getInt("ID"));
                    status.setName(rs.getString("NAME"));
                    status.setState(rs.getInt("STATE"));
                    status.setType(rs.getInt("TYPE"));
                    return status;
                }
        );
    }
}
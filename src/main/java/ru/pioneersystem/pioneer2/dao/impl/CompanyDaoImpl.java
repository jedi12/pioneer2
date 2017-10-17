package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.CompanyDao;
import ru.pioneersystem.pioneer2.model.Company;

import java.sql.PreparedStatement;
import java.util.List;

@Repository(value = "companyDao")
public class CompanyDaoImpl implements CompanyDao {
    private static final int LOCKED = 0;
    private static final int ACTIVE = 1;

    private static final String INSERT_COMPANY = "INSERT INTO DOC.COMPANY (NAME, FULL_NAME, PHONE, EMAIL, ADDRESS, " +
            "STATE, MAX_USERS, SITE, COMMENT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_COMPANY = "UPDATE DOC.COMPANY SET NAME = ?, FULL_NAME = ?, PHONE = ?, " +
            "EMAIL = ?, ADDRESS = ?, MAX_USERS = ?, SITE = ?, COMMENT = ? WHERE ID = ?";
    private static final String UPDATE_COMPANY_LOCK = "UPDATE DOC.COMPANY SET STATE = ? WHERE ID = ?";
    private static final String UPDATE_COMPANY_UNLOCK = "UPDATE DOC.COMPANY SET STATE = ? WHERE ID = ?";
    private static final String SELECT_COMPANY = "SELECT ID, NAME, FULL_NAME, PHONE, EMAIL, ADDRESS, STATE, " +
            "MAX_USERS, SITE, COMMENT FROM DOC.COMPANY WHERE ID = ?";
    private static final String SELECT_COMPANY_LIST =
            "SELECT ID, NAME, STATE FROM DOC.COMPANY ORDER BY NAME ASC";
    private static final String SELECT_MAX_USER = "SELECT MAX_USERS FROM DOC.COMPANY WHERE ID = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Company get(int companyId) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_COMPANY,
                new Object[]{companyId},
                (rs, rowNum) -> {
                    Company company = new Company();
                    company.setId(rs.getInt("ID"));
                    company.setName(rs.getString("NAME"));
                    company.setFullName(rs.getString("FULL_NAME"));
                    company.setPhone(rs.getString("PHONE"));
                    company.setEmail(rs.getString("EMAIL"));
                    company.setAddress(rs.getString("ADDRESS"));
                    company.setMaxUsers(rs.getInt("MAX_USERS"));
                    company.setSite(rs.getString("SITE"));
                    company.setComment(rs.getString("COMMENT"));
                    company.setState(rs.getInt("STATE"));
                    return company;
                }
        );
    }

    @Override
    public List<Company> getList() throws DataAccessException {
        return jdbcTemplate.query(SELECT_COMPANY_LIST,
                (rs, rowNum) -> {
                    Company company = new Company();
                    company.setId(rs.getInt("ID"));
                    company.setName(rs.getString("NAME"));
                    company.setState(rs.getInt("STATE"));
                    return company;
                }
        );
    }

    @Override
    @Transactional
    public int create(Company company) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_COMPANY, new String[] {"id"});
                    pstmt.setString(1, company.getName());
                    pstmt.setString(2, company.getFullName());
                    pstmt.setString(3, company.getPhone());
                    pstmt.setString(4, company.getEmail());
                    pstmt.setString(5, company.getAddress());
                    pstmt.setInt(6, ACTIVE);
                    pstmt.setInt(7, company.getMaxUsers());
                    pstmt.setString(8, company.getSite());
                    pstmt.setString(9, company.getComment());
                    return pstmt;
                }, keyHolder
        );
        return keyHolder.getKey().intValue();
    }

    @Override
    @Transactional
    public void update(Company company) throws DataAccessException {
        jdbcTemplate.update(UPDATE_COMPANY,
                company.getName(),
                company.getFullName(),
                company.getPhone(),
                company.getEmail(),
                company.getAddress(),
                company.getMaxUsers(),
                company.getSite(),
                company.getComment(),
                company.getId()
        );
    }

    @Override
    @Transactional
    public void lock(int companyId) throws DataAccessException {
        jdbcTemplate.update(UPDATE_COMPANY_LOCK, LOCKED, companyId);
    }

    @Override
    @Transactional
    public void unlock(int companyId) throws DataAccessException {
        jdbcTemplate.update(UPDATE_COMPANY_UNLOCK, ACTIVE, companyId);
    }

    @Override
    public int getMaxUserCount(int companyId) throws DataAccessException {
        return jdbcTemplate.queryForObject(SELECT_MAX_USER,
                new Object[]{companyId},
                (rs, rowNum) -> rs.getInt(1)
        );
    }
}
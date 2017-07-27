package ru.pioneersystem.pioneer2.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.PartDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Part;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Repository(value = "partDao")
public class PartDaoImpl implements PartDao {

    private static final String INSERT_PART =
            "INSERT INTO DOC.PARTS (NAME, STATE, PARENT, TREE_LEVEL, OWNER_G, COMPANY, TYPE) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_PART_GROUP =
            "INSERT INTO DOC.PARTS_GROUP (ID, GROUP_ID) VALUES (?, ?)";
    private static final String UPDATE_PART =
            "UPDATE DOC.PARTS SET NAME = ?, PARENT = ?, TREE_LEVEL = ?, OWNER_G = ?, TYPE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_PART =
            "UPDATE DOC.PARTS SET STATE = ? WHERE ID = ? AND COMPANY = ?";
    private static final String DELETE_PART_GROUP =
            "DELETE FROM DOC.PARTS_GROUP WHERE ID = ?";
    private static final String SELECT_PART =
            "SELECT ID, NAME, STATE, PARENT, TREE_LEVEL, OWNER_G FROM DOC.PARTS WHERE ID = ? AND COMPANY = ?";
    private static final String SELECT_PART_GROUP =
            "SELECT GROUP_ID, NAME FROM DOC.PARTS_GROUP PG LEFT JOIN DOC.GROUPS G ON PG.GROUP_ID = G.ID WHERE PG.ID = ?";
    private static final String SELECT_PART_LIST =
            "SELECT ID, NAME, STATE, PARENT, TREE_LEVEL, OWNER_G FROM DOC.PARTS WHERE STATE > 0 AND TYPE = ? " +
                    "AND COMPANY = ? ORDER BY TREE_LEVEL DESC, NAME ASC";
    private static final String SELECT_USER_PART_LIST =
            "SELECT P.ID AS ID, NAME, STATE, PARENT, TREE_LEVEL, OWNER_G FROM DOC.PARTS P LEFT JOIN DOC.PARTS_GROUP " +
                    "PG ON P.ID = PG.ID WHERE STATE > 0 AND TYPE = ? AND (GROUP_ID IS NULL OR GROUP_ID " +
                    "IN (SELECT ID FROM DOC.GROUPS_USER WHERE USER_ID = ?)) ORDER BY TREE_LEVEL DESC, NAME ASC";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Part get(int partId, int companyId) throws DataAccessException {
        Part resultPart = jdbcTemplate.query(SELECT_PART,
                new Object[]{partId, companyId},
                (rs) -> {
                    if (rs.next()) {
                        Part part = new Part();
                        part.setId(rs.getInt("ID"));
                        part.setName(rs.getString("NAME"));
                        part.setState(rs.getInt("STATE"));
                        part.setParent(rs.getInt("PARENT"));
                        part.setTreeLevel(rs.getInt("TREE_LEVEL"));
                        part.setOwnerGroup(rs.getInt("OWNER_G"));
                        return part;
                    } else {
                        throw new NotFoundDaoException("Not found Part with partId = " + partId +
                                " and companyId = " + companyId);
                    }
                }
        );

        List<Part.LinkGroup> resultGroups = jdbcTemplate.query(SELECT_PART_GROUP,
                new Object[]{partId},
                rs -> {
                    List<Part.LinkGroup> linkGroups = new LinkedList<>();
                    while(rs.next()){
                        Part.LinkGroup linkGroup = new Part.LinkGroup();
                        linkGroup.setGroupId(rs.getInt("GROUP_ID"));
                        linkGroup.setGroupName(rs.getString("NAME"));

                        linkGroups.add(linkGroup);
                    }
                    return linkGroups;
                }
        );

        resultPart.setLinkGroups(resultGroups);
        return resultPart;
    }

    @Override
    public List<Part> getList(int type, int companyId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_PART_LIST,
                new Object[]{type, companyId},
                (rs, rowNum) -> {
                    Part part = new Part();
                    part.setId(rs.getInt("ID"));
                    part.setName(rs.getString("NAME"));
                    part.setState(rs.getInt("STATE"));
                    part.setParent(rs.getInt("PARENT"));
                    part.setTreeLevel(rs.getInt("TREE_LEVEL"));
                    part.setOwnerGroup(rs.getInt("OWNER_G"));
                    return part;
                }
        );
    }

    @Override
    public List<Part> getUserPart(int type, int userId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_USER_PART_LIST,
                new Object[]{type, userId},
                (rs, rowNum) -> {
                    Part part = new Part();
                    part.setId(rs.getInt("ID"));
                    part.setName(rs.getString("NAME"));
                    part.setState(rs.getInt("STATE"));
                    part.setParent(rs.getInt("PARENT"));
                    part.setTreeLevel(rs.getInt("TREE_LEVEL"));
                    part.setOwnerGroup(rs.getInt("OWNER_G"));
                    return part;
                }
        );
    }

    @Override
    @Transactional
    public void create(Part part, int type, int companyId) throws DataAccessException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement pstmt = connection.prepareStatement(INSERT_PART, new String[] {"id"});
                    pstmt.setString(1, part.getName());
                    pstmt.setInt(2, Part.State.EXISTS);
                    pstmt.setInt(3, part.getParent());
                    pstmt.setInt(4, part.getTreeLevel());
                    pstmt.setInt(5, part.getOwnerGroup());
                    pstmt.setInt(6, companyId);
                    pstmt.setInt(7, type);
                    return pstmt;
                }, keyHolder
        );

        jdbcTemplate.batchUpdate(INSERT_PART_GROUP,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, keyHolder.getKey().intValue());
                        pstmt.setInt(2, part.getLinkGroups().get(i).getGroupId());
                    }
                    public int getBatchSize() {
                        return part.getLinkGroups().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void update(Part part, int type, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(UPDATE_PART,
                part.getName(),
                part.getParent(),
                part.getTreeLevel(),
                part.getOwnerGroup(),
                type,
                part.getId(),
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Part with partId = " + part.getId() +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_PART_GROUP,
                part.getId()
        );

        jdbcTemplate.batchUpdate(INSERT_PART_GROUP,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, part.getId());
                        pstmt.setInt(2, part.getLinkGroups().get(i).getGroupId());
                    }
                    public int getBatchSize() {
                        return part.getLinkGroups().size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void update(List<Part> parts, int type, int companyId) throws DataAccessException {
        jdbcTemplate.batchUpdate(UPDATE_PART,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setString(1, parts.get(i).getName());
                        pstmt.setInt(2, parts.get(i).getParent());
                        pstmt.setInt(3, parts.get(i).getTreeLevel());
                        pstmt.setInt(4, parts.get(i).getOwnerGroup());
                        pstmt.setInt(5, type);
                        pstmt.setInt(6, parts.get(i).getId());
                        pstmt.setInt(7, companyId);
                    }
                    public int getBatchSize() {
                        return parts.size();
                    }
                }
        );
    }

    @Override
    @Transactional
    public void delete(int partId, int companyId) throws DataAccessException {
        int updatedRows = jdbcTemplate.update(DELETE_PART,
                Part.State.DELETED,
                partId,
                companyId
        );

        if (updatedRows == 0) {
            throw new NotFoundDaoException("Not found Part with partId = " + partId +
                    " and companyId = " + companyId);
        }

        jdbcTemplate.update(DELETE_PART_GROUP,
                partId
        );
    }

    @Override
    @Transactional
    public void delete(List<Part> parts, int companyId) throws DataAccessException {
        int[] updatedRows = jdbcTemplate.batchUpdate(DELETE_PART,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, Part.State.DELETED);
                        pstmt.setInt(2, parts.get(i).getId());
                        pstmt.setInt(3, companyId);
                    }
                    public int getBatchSize() {
                        return parts.size();
                    }
                }
        );

        for (int updatedRow : updatedRows) {
            if (updatedRow == 0) {
                throw new NotFoundDaoException("One ore more Part not found for companyId = " + companyId);
            }
        }

        jdbcTemplate.batchUpdate(DELETE_PART_GROUP,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setInt(1, parts.get(i).getId());
                    }
                    public int getBatchSize() {
                        return parts.size();
                    }
                }
        );
    }
}
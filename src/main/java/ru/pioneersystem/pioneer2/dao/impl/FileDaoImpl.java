package ru.pioneersystem.pioneer2.dao.impl;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.pioneersystem.pioneer2.dao.FileDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.File;

import java.io.IOException;
import java.io.InputStream;

@Repository(value = "fileDao")
public class FileDaoImpl implements FileDao {
    private static final String SELECT_FILE = "SELECT ID, NAME, MIME_TYPE, LENGTH, DATA FROM DOC.FILES WHERE ID = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public File get(int fileId) throws DataAccessException {
        return jdbcTemplate.query(SELECT_FILE,
                new Object[]{fileId},
                (rs) -> {
                    if (rs.next()) {
                        File file = new File();
                        file.setId(rs.getInt("ID"));
                        file.setName(rs.getString("NAME"));
                        file.setMimeType(rs.getString("MIME_TYPE"));
                        file.setLength(rs.getInt("LENGTH"));

                        try (InputStream contentStream = rs.getBinaryStream("DATA")) {
                            file.setContent(IOUtils.toByteArray(contentStream));
                        } catch (IOException e) {
                            throw new TransientDataAccessResourceException("Error while file content reading from database", e);
                        }

                        return file;
                    } else {
                        throw new NotFoundDaoException("Not found File with fileId = " + fileId);
                    }
                }
        );
    }
}
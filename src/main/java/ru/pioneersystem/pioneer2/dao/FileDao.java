package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.File;

public interface FileDao {

    File get(int fileId) throws DataAccessException;
}
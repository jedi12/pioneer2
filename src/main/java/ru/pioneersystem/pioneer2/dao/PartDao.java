package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Part;

import java.util.List;

public interface PartDao {

    Part get(int partId) throws DataAccessException;

    List<Part> getList(int type, int companyId) throws DataAccessException;

    List<Part> getUserPart(int type, int userId) throws DataAccessException;

    void create(Part part, int type, int companyId) throws DataAccessException;

    void update(Part part) throws DataAccessException;

    void update(List<Part> parts) throws DataAccessException;

    void delete(int partId) throws DataAccessException;

    void delete(List<Part> parts) throws DataAccessException;
}
package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Part;

import java.util.List;

public interface PartDao {

    List<Part> getList(int type, int companyId) throws DataAccessException;

    List<Part> getUserPart(int type, int userId) throws DataAccessException;

    Part get(int partId, int companyId) throws DataAccessException;

    void create(Part part, int type, int companyId) throws DataAccessException;

    void update(Part part, int type, int companyId) throws DataAccessException;

    void update(List<Part> parts, int type, int companyId) throws DataAccessException;

    void delete(int partId, int companyId) throws DataAccessException;

    void delete(List<Part> parts, int companyId) throws DataAccessException;
}
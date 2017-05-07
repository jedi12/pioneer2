package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Part;

import java.util.List;

public interface PartDao {

    Part get(int id) throws DataAccessException;

    List<Part> getList(int type, int company) throws DataAccessException;

    List<Part> getUserPart(int type, int userId) throws DataAccessException;

    void create(Part part, int type, int company) throws DataAccessException;

    void update(Part part) throws DataAccessException;

    void update(List<Part> parts) throws DataAccessException;

    void delete(int id) throws DataAccessException;

    void delete(List<Part> parts) throws DataAccessException;
}
package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Part;

import java.util.List;

public interface PartDao {

    List<Part> getSuperList(int type) throws DataAccessException;

    List<Part> getAdminList(int type, int companyId) throws DataAccessException;

    List<Part> getUserPart(int type, int userId, int companyId) throws DataAccessException;

    List<String> getTemplateListContainingInParts(List<Part> parts, int companyId) throws DataAccessException;

    int getPubDocContainingInParts(List<Part> parts, int companyId) throws DataAccessException;

    int getCountPartsWithRestriction(int groupId, int companyId) throws DataAccessException;

    void removeGroupRestriction(int groupId, int companyId) throws DataAccessException;

    Part get(int partId, int companyId) throws DataAccessException;

    int create(Part part, int type, int companyId) throws DataAccessException;

    void update(Part part, int type, int companyId) throws DataAccessException;

    void update(List<Part> parts, int type, int companyId) throws DataAccessException;

    void delete(int partId, int companyId) throws DataAccessException;

    void delete(List<Part> parts, int companyId) throws DataAccessException;
}
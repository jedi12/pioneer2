package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface PartService {

    List<Part> getPartList(int type) throws ServiceException;

    Map<String, Integer> getPartMap(int type) throws ServiceException;

    List<Part> getUserPartList(int type) throws ServiceException;

    Map<String, Integer> getUserPartMap(int type) throws ServiceException;

    List<String> getTemplateListContainingInParts(List<Part> parts) throws ServiceException;

    int getCountPubDocContainingInParts(List<Part> parts) throws ServiceException;

    int getCountPartsWithRestriction(int groupId) throws ServiceException;

    Part getNewPart();

    Part getPart(Part selectedPart) throws ServiceException;

    void savePart(Part part, int type) throws ServiceException;

    void updateParts(List<Part> parts, int type) throws ServiceException;

    void deletePart(Part part) throws ServiceException;

    void deleteParts(List<Part> parts, int partType) throws ServiceException;

    int createExamplePart(int partType, int adminGroupId, int companyId) throws ServiceException;
}
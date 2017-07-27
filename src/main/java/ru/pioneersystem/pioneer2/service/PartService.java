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

    Part getNewPart();

    Part getPart(int partId) throws ServiceException;

    void savePart(Part part, int type) throws ServiceException;

    void updateParts(List<Part> parts, int type) throws ServiceException;

    void deletePart(int partId) throws ServiceException;

    void deleteParts(List<Part> parts) throws ServiceException;
}
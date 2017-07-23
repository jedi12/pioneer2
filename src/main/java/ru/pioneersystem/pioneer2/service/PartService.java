package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface PartService {
    Part getPart(int id) throws ServiceException;

    List<Part> getPartList(int type) throws ServiceException;

    Map<String, Integer> getPartMap(int type) throws ServiceException;

    List<Part> getUserPartList(int type) throws ServiceException;

    Map<String, Integer> getUserPartMap(int type) throws ServiceException;

    void createPart(Part part, int type) throws ServiceException;

    void updatePart(Part part) throws ServiceException;

    void updateParts(List<Part> parts) throws ServiceException;

    void deletePart(int id) throws ServiceException;

    void deleteParts(List<Part> parts) throws ServiceException;
}
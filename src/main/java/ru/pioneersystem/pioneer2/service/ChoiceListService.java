package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface ChoiceListService {
    ChoiceList getChoiceList(int id) throws ServiceException;

    Map<Integer, List<String>> getChoiceListForTemplate(int templateId) throws ServiceException;

    Map<Integer, List<String>> getChoiceListForDocument(int documentId) throws ServiceException;

    List<ChoiceList> getChoiceListList() throws ServiceException;

    Map<String, ChoiceList> getChoiceListMap() throws ServiceException;

    void createChoiceList(ChoiceList choiceList) throws ServiceException;

    void updateChoiceList(ChoiceList choiceList) throws ServiceException;

    void deleteChoiceList(int id) throws ServiceException, RestrictException;
}
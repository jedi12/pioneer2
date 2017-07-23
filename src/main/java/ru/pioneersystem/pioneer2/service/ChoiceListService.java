package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface ChoiceListService {

    Map<Integer, List<String>> getChoiceListForTemplate(int templateId) throws ServiceException;

    Map<Integer, List<String>> getChoiceListForDocument(int documentId) throws ServiceException;

    List<ChoiceList> getChoiceListList() throws ServiceException;

    Map<String, ChoiceList> getChoiceListMap() throws ServiceException;

    ChoiceList getNewChoiceList();

    ChoiceList getChoiceList(int id) throws ServiceException;

    void saveChoiceList(ChoiceList choiceList) throws ServiceException;

    void deleteChoiceList(int id) throws ServiceException;
}
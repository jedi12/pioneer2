package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface ChoiceListService {

    List<ChoiceList> getChoiceListList() throws ServiceException;

    Map<String, ChoiceList> getChoiceListMap() throws ServiceException;

    void setChoiceListForTemplate(Document document) throws ServiceException;

    void setChoiceListsForDocument(Document document) throws ServiceException;

    ChoiceList getNewChoiceList();

    ChoiceList getChoiceList(int choiceListId) throws ServiceException;

    void saveChoiceList(ChoiceList choiceList) throws ServiceException;

    void deleteChoiceList(int choiceListId) throws ServiceException;
}
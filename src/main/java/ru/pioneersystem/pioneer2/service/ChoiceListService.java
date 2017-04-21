package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface ChoiceListService {
    ChoiceList getChoiceList(int id) throws ServiceException;

    List<ChoiceList> getChoiceListList() throws ServiceException;

    void createChoiceList(ChoiceList choiceList) throws ServiceException;

    void updateChoiceList(ChoiceList choiceList) throws ServiceException;

    void deleteChoiceList(int id) throws ServiceException, RestrictException;
}
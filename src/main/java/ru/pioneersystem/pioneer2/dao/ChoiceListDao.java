package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.ChoiceList;

import java.util.List;
import java.util.Map;

public interface ChoiceListDao {

    ChoiceList get(int choiceListId, int companyId) throws DataAccessException;

    Map<Integer, List<String>> getForTemplate(int templateId) throws DataAccessException;

    Map<Integer, List<String>> getForDocument(int documentId) throws DataAccessException;

    List<ChoiceList> getList(int companyId) throws DataAccessException;

    void create(ChoiceList choiceList, int companyId) throws DataAccessException;

    void update(ChoiceList choiceList, int companyId) throws DataAccessException;

    void delete(int choiceListId, int companyId) throws DataAccessException;
}
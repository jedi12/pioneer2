package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.ChoiceList;

import java.util.List;
import java.util.Map;

public interface ChoiceListDao {

    ChoiceList get(int id) throws DataAccessException;

    Map<Integer, List<String>> getForTemplate(int templateId) throws DataAccessException;

    Map<Integer, List<String>> getForDocument(int documentId) throws DataAccessException;

    List<ChoiceList> getList(int company) throws DataAccessException;

    void create(ChoiceList choiceList, int company) throws DataAccessException;

    void update(ChoiceList choiceList) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}
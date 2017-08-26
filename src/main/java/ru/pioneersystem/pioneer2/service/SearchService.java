package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.SearchDoc;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface SearchService {

    List<Document> findList(SearchDoc searchDoc) throws ServiceException;
}
package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.DocumentDao;
import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.service.DocumentService;
import ru.pioneersystem.pioneer2.service.FieldTypeService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;

import java.util.List;

@Service("documentService")
public class DocumentServiceImpl implements DocumentService {
    private Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private DocumentDao documentDao;
    private FieldTypeService fieldTypeService;
    private CurrentUser currentUser;

    @Autowired
    public DocumentServiceImpl(DocumentDao documentDao, FieldTypeService fieldTypeService, CurrentUser currentUser) {
        this.documentDao = documentDao;
        this.fieldTypeService = fieldTypeService;
        this.currentUser = currentUser;
    }

    @Override
    public Document getDocument(int id) throws ServiceException {
//        try {
//            return fieldTypeService.setLocalizedFieldTypeName(templateDao.get(id));
//        } catch (DataAccessException e) {
//            log.error("Can't get Template by id", e);
//            throw new ServiceException("Can't get Template by id", e);
//        }
        return null;
    }

    @Override
    public List<Document> getDocumentList() throws ServiceException {
//        try {
//            return templateDao.getList(currentUser.getUser().getCompanyId());
//        } catch (DataAccessException e) {
//            log.error("Can't get list of Template", e);
//            throw new ServiceException("Can't get list of Template", e);
//        }
        return null;
    }

    @Override
    public List<Document> getDocumentList(int partId) throws ServiceException {
        try {
            return documentDao.getListByPartId(partId);
        } catch (DataAccessException e) {
            log.error("Can't get list of Document by part id", e);
            throw new ServiceException("Can't get list of Document by part id", e);
        }
    }

    @Override
    public void createDocument(Document document) throws ServiceException {
//        try {
//            templateDao.create(template, currentUser.getUser().getCompanyId());
//        } catch (DataAccessException e) {
//            log.error("Can't create Template", e);
//            throw new ServiceException("Can't create Template", e);
//        }
        return;
    }

    @Override
    public void updateDocument(Document document) throws ServiceException {
//        try {
//            templateDao.update(template);
//        } catch (DataAccessException e) {
//            log.error("Can't update Template", e);
//            throw new ServiceException("Can't update Template", e);
//        }
        return;
    }

    @Override
    public void deleteDocument(int id) throws ServiceException, RestrictException {
//        // TODO: 28.02.2017 Сделать проверку, используется ли данный шаблон или не нужна проверка вообще
//        // пример:
//        // установить @Transactional(rollbackForClassName = DaoException.class)
//        // после проверки выбрасывать RestrictException("Нельзя удалять, пока используется в шаблоне")
//        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
//        try {
//            templateDao.delete(id);
//        } catch (DataAccessException e) {
//            log.error("Can't delete Template", e);
//            throw new ServiceException("Can't delete Template", e);
//        }
        return;
    }
}

package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.DocumentDao;
import ru.pioneersystem.pioneer2.dao.exception.LockException;
import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.UserLockException;
import ru.pioneersystem.pioneer2.view.CurrentUser;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("documentService")
public class DocumentServiceImpl implements DocumentService {
    private Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private DocumentDao documentDao;
    private FieldTypeService fieldTypeService;
    private ChoiceListService choiceListService;
    private RouteProcessService routeProcessService;
    private UserService userService;
    private CurrentUser currentUser;

    @Autowired
    public DocumentServiceImpl(DocumentDao documentDao, FieldTypeService fieldTypeService,
                               ChoiceListService choiceListService, RouteProcessService routeProcessService,
                               UserService userService, CurrentUser currentUser) {
        this.documentDao = documentDao;
        this.fieldTypeService = fieldTypeService;
        this.choiceListService = choiceListService;
        this.routeProcessService = routeProcessService;
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @Override
    public Document getDocumentTemplateBased(int templateId) throws ServiceException {
        try {
            Map<Integer, List<String>> choiceLists = choiceListService.getChoiceListForTemplate(templateId);
            Document document = documentDao.getTemplateBased(templateId, choiceLists);
            document.setEditMode(true);
            return document;
        } catch (DataAccessException e) {
            log.error("Can't get Document based on Template", e);
            throw new ServiceException("Can't get Document based on Template", e);
        }
    }

    @Override
    public Document getDocument(int id) throws ServiceException {
        try {
            Map<Integer, List<String>> choiceLists = choiceListService.getChoiceListForDocument(id);
            Document document = documentDao.getForEdit(id, choiceLists);
            if (document.getStatusId() == Status.Id.CREATED) {
                document.setEditMode(true);
            }
            return document;
        } catch (DataAccessException e) {
            log.error("Can't get Document by id", e);
            throw new ServiceException("Can't get Document by id", e);
        }
    }

    @Override
    public List<Document> getOnRouteDocumentList() throws ServiceException {
        try {
            return documentDao.getOnRouteList(currentUser.getCurrMenu().getRoleId(), currentUser.getUser().getId());
        } catch (DataAccessException e) {
            log.error("Can't get list of on route Document", e);
            throw new ServiceException("Can't get list of on route Document", e);
        }
    }

    @Override
    public List<Document> getDocumentListByPatrId(int partId) throws ServiceException {
        try {
            return documentDao.getListByPartId(partId);
        } catch (DataAccessException e) {
            log.error("Can't get list of Document by part id", e);
            throw new ServiceException("Can't get list of Document by part id", e);
        }
    }

    @Override
    public List<Document> getMyDocumentListOnDate(Date dateIn) throws ServiceException {
        try {
            Timestamp beginDate = Timestamp.from(dateIn.toInstant());
            Timestamp endDate = Timestamp.from(dateIn.toInstant().plus(1, ChronoUnit.DAYS));
            return documentDao.getMyOnDateList(beginDate, endDate, currentUser.getUser().getId());
        } catch (DataAccessException e) {
            log.error("Can't get list of my Document on date", e);
            throw new ServiceException("Can't get list of my Document on date", e);
        }
    }

    @Override
    public List<Document> getMyWorkingDocumentList() throws ServiceException {
        try {
            return documentDao.getMyOnWorkingList(currentUser.getUser().getId());
        } catch (DataAccessException e) {
            log.error("Can't get list of my working Document", e);
            throw new ServiceException("Can't get list of my working Document", e);
        }
    }

    @Override
    public void createDocument(Document document) throws ServiceException {
        try {
            documentDao.create(document, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            routeProcessService.createRouteProcess(document);
        } catch (DataAccessException | UserLockException e) {
            log.error("Can't create Document or Route Process", e);
            throw new ServiceException("Can't create Document or Route Process", e);
        }
    }

    @Override
    public void updateDocument(Document document) throws ServiceException, UserLockException {
        try {
            documentDao.update(document, currentUser.getUser().getId());
            routeProcessService.createRouteProcess(document);
        } catch (DataAccessException e) {
            log.error("Can't update Document", e);
            throw new ServiceException("Can't update Document", e);
        } catch (LockException e) {
            log.error("Can't update Document", e);
            try {
                throw new UserLockException(userService.getUser(e.getUserId()), e.getDate(), e);
            } catch (ServiceException se) {
                throw new UserLockException(new User(), e.getDate(), e);
            }
        }
    }

    @Override
    public void deleteDocument(int id) throws ServiceException, RestrictException {
//        // TODO: 28.02.2017 Сделать проверку на удаление
//        // пример:
//        // установить @Transactional(rollbackForClassName = DaoException.class)
//        // после проверки выбрасывать RestrictException("Нельзя удалять, пока используется в шаблоне")
//        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            documentDao.delete(id, currentUser.getUser().getId());
        } catch (DataAccessException e) {
            log.error("Can't delete Document", e);
            throw new ServiceException("Can't delete Document", e);
        }
    }
}

package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.AppProps;
import ru.pioneersystem.pioneer2.dao.DocumentDao;
import ru.pioneersystem.pioneer2.dao.exception.LockDaoException;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.LockException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service("documentService")
public class DocumentServiceImpl implements DocumentService {
    private EventService eventService;
    private DocumentDao documentDao;
    private ChoiceListService choiceListService;
    private RouteProcessService routeProcessService;
    private UserService userService;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private DictionaryService dictionaryService;
    private AppProps appProps;

    @Autowired
    public DocumentServiceImpl(EventService eventService, DocumentDao documentDao, ChoiceListService choiceListService,
                               RouteProcessService routeProcessService, UserService userService,
                               CurrentUser currentUser, LocaleBean localeBean, MessageSource messageSource,
                               DictionaryService dictionaryService, AppProps appProps) {
        this.eventService = eventService;
        this.documentDao = documentDao;
        this.choiceListService = choiceListService;
        this.routeProcessService = routeProcessService;
        this.userService = userService;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
        this.dictionaryService = dictionaryService;
        this.appProps = appProps;
    }

    @Override
    public List<Document> getOnRouteDocumentList() throws ServiceException {
        try {
            return documentDao.getOnRouteList(currentUser.getCurrRole().getId(), currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Document> getDocumentListByPatrId(int partId) throws ServiceException {
        try {
            return documentDao.getListByPartId(partId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), partId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Document> getMyDocumentListOnDate(Date dateIn) throws ServiceException {
        try {
            Timestamp beginDate = Timestamp.from(dateIn.toInstant());
            Timestamp endDate = Timestamp.from(dateIn.toInstant().plus(1, ChronoUnit.DAYS));
            List<Document> documents = documentDao.getMyOnDateList(beginDate, endDate, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            for (Document document : documents) {
                String statusName = dictionaryService.getLocalizedStatusName(document.getStatusId(), localeBean.getLocale());
                if (statusName != null) {
                    document.setStatusName(statusName);
                }
            }
            return documents;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Document> getMyWorkingDocumentList() throws ServiceException {
        try {
            List<Document> documents = documentDao.getMyOnWorkingList(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            for (Document document : documents) {
                String statusName = dictionaryService.getLocalizedStatusName(document.getStatusId(), localeBean.getLocale());
                if (statusName != null) {
                    document.setStatusName(statusName);
                }
            }
            return documents;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> getDocToCancelByGroup(int groupId) throws ServiceException {
        try {
            return documentDao.getDocToCancelByGroup(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.docToCanselByGroup", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), groupId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> getDocToCancelByRole(int roleId) throws ServiceException {
        try {
            return documentDao.getDocToCancelByRole(roleId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.docToCanselByRole", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), roleId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Document getNewDocument(int templateId) throws ServiceException {
        try {
            Document document = documentDao.getTemplateBased(templateId, currentUser.getUser().getCompanyId());
            choiceListService.setChoiceListForTemplate(document);
            document.setCreateFlag(true);
            document.setEditMode(true);
            setupViewElements(document, null);
            return document;
        } catch (NotFoundDaoException e) {
            String mess = messageSource.getMessage("error.document.templateNotFound", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), templateId);
            throw new ServiceException(mess, e);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.templateNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), templateId);
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.document.templateNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null, templateId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Document getDocument(int documentId) throws ServiceException {
        try {
            Document document = documentDao.get(documentId, currentUser.getUser().getCompanyId());
            offsetDateAndFormat(document);
            document.setCreateFlag(false);
            if (currentUser.getCurrRole().isCanEdit() || document.getStatusId() == Status.Id.CREATED) {
                choiceListService.setChoiceListsForDocument(document);
            }

            if (document.getStatusId() == Status.Id.CREATED) {
                document.setEditMode(true);
            }

            setupViewElements(document, routeProcessService.getCurrNotSignedRoutePointGroups(documentId));
            eventService.logEvent(Event.Type.DOC_GETED, documentId);
            return document;
        } catch (NotFoundDaoException e) {
            String mess = messageSource.getMessage("error.document.NotFound", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), documentId);
            throw new RestrictionException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), documentId);
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.document.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null, documentId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void saveDocument(Document document) throws ServiceException, LockException {
        try {
            if (document.isCreateFlag()) {
                int documentId = documentDao.create(document, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
                routeProcessService.createRouteProcess(document);
                eventService.logEvent(Event.Type.DOC_CREATED, documentId);
            } else {
                documentDao.lock(document, currentUser.getUser().getCompanyId());
                documentDao.update(document, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
                routeProcessService.createRouteProcess(document);
                eventService.logEvent(Event.Type.DOC_CHANGED, document.getId());
            }
        } catch (LockDaoException e) {
            String mess = messageSource.getMessage("warn.document.changed",
                    new Object[]{(new SimpleDateFormat(localeBean.getDateTimePattern())).format(e.getDate()),
                            userService.getUserById(e.getUserId()).getName()}, localeBean.getLocale());
            eventService.logError(mess, null, document.getId());
            throw new LockException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.document.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null, document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void saveAndSendDocument(Document document) throws ServiceException, LockException {
        saveDocument(document);
        try {
            routeProcessService.startRouteProcess(document);
            eventService.logEvent(Event.Type.DOC_SENDED, document.getId());
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.document.NotSavedAndSended", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null, document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteDocument(Document document) throws ServiceException, LockException {
        // TODO: 28.02.2017 Сделать проверку на удаление
        try {
            documentDao.lock(document, currentUser.getUser().getCompanyId());
            documentDao.delete(document.getId(), currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.DOC_DELETED, document.getId());
        } catch (LockDaoException e) {
            String mess = messageSource.getMessage("warn.document.changed",
                    new Object[]{(new SimpleDateFormat(localeBean.getDateTimePattern())).format(e.getDate()),
                            userService.getUserById(e.getUserId()).getName()}, localeBean.getLocale());
            eventService.logError(mess, null, document.getId());
            throw new LockException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void copyDocument(Document document) throws ServiceException, LockException {
        try {
            documentDao.lock(document, currentUser.getUser().getCompanyId());
            documentDao.create(document, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            routeProcessService.createRouteProcess(document);
            eventService.logEvent(Event.Type.DOC_COPIED, document.getId());
        } catch (LockDaoException e) {
            String mess = messageSource.getMessage("warn.document.changed",
                    new Object[]{(new SimpleDateFormat(localeBean.getDateTimePattern())).format(e.getDate()),
                            userService.getUserById(e.getUserId()).getName()}, localeBean.getLocale());
            eventService.logError(mess, null, document.getId());
            throw new LockException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotCopied", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.document.NotCopied", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null, document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void recallDocument(Document document) throws ServiceException, LockException {
        try {
            documentDao.lock(document, currentUser.getUser().getCompanyId());
            routeProcessService.cancelRouteProcess(document);
            eventService.logEvent(Event.Type.DOC_RECALLED, document.getId());
        } catch (LockDaoException e) {
            String mess = messageSource.getMessage("warn.document.processed",
                    new Object[]{(new SimpleDateFormat(localeBean.getDateTimePattern())).format(e.getDate()),
                            userService.getUserById(e.getUserId()).getName()}, localeBean.getLocale());
            eventService.logError(mess, null, document.getId());
            throw new LockException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotRecalled", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.document.NotRecalled", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null, document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void publishDocument(Document document) throws ServiceException {
        try {
            documentDao.publish(document, currentUser.getUser().getId(), currentUser.getUser().getCompanyId(), true);
            eventService.logEvent(Event.Type.DOC_PUBLISHED, document.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotPublished", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void cancelPublishDocument(Document document) throws ServiceException {
        try {
            documentDao.publish(document, currentUser.getUser().getId(), currentUser.getUser().getCompanyId(), false);
            eventService.logEvent(Event.Type.DOC_PUBLISH_CANCELED, document.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotPublishCanceled", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void acceptDocument(Document document) throws ServiceException, LockException {
        try {
            documentDao.lock(document, currentUser.getUser().getCompanyId());
            if (document.isEditMode()) {
                documentDao.update(document, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            }
            routeProcessService.acceptRoutePointProcess(document);
            eventService.logEvent(Event.Type.DOC_ACCEPTED, document.getId());
        } catch (LockDaoException e) {
            String mess = messageSource.getMessage("warn.document.processed",
                    new Object[]{(new SimpleDateFormat(localeBean.getDateTimePattern())).format(e.getDate()),
                            userService.getUserById(e.getUserId()).getName()}, localeBean.getLocale());
            eventService.logError(mess, null);
            throw new LockException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotAccepted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.document.NotAccepted", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void rejectDocument(Document document) throws ServiceException, LockException {
        try {
            documentDao.lock(document, currentUser.getUser().getCompanyId());
            routeProcessService.rejectRoutePointProcess(document);
            eventService.logEvent(Event.Type.DOC_REJECTED, document.getId());
        } catch (LockDaoException e) {
            String mess = messageSource.getMessage("warn.document.processed",
                    new Object[]{(new SimpleDateFormat(localeBean.getDateTimePattern())).format(e.getDate()),
                            userService.getUserById(e.getUserId()).getName()}, localeBean.getLocale());
            eventService.logError(mess, null);
            throw new LockException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.document.NotRejected", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.document.NotRejected", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null);
            throw new ServiceException(mess, e);
        }
    }

    private void setupViewElements(Document document, List<Integer> currNotSignedRoutePointGroups) {
        Map<Integer, Map<Integer, Integer>> userRolesGroupActivity = currentUser.getUserRolesGroupActivity();
        Map<Integer, Integer> userGroupsActivity = userRolesGroupActivity.get(currentUser.getCurrRole().getId());
        Document.ViewElements viewElements = new Document.ViewElements();

        switch (currentUser.getCurrPage()) {
            case Menu.Page.PUBLIC_DOC:
                if (currentUser.isPublicRole()) {
                    viewElements.setBtnPublishCancel(true);
                }
                break;
            case Menu.Page.SEARCH_DOC:
                //
                break;
            case Menu.Page.CREATE_DOC:
                viewElements.setBtnSaveAndSend(true);
                viewElements.setBtnSave(true);
                if (currentUser.getUserCreateGroups().size() > 1) {
                    viewElements.setElDocOwner(true);
                }
                break;
            case Menu.Page.ON_ROUTE_DOC:
                viewElements.setDisableBtn(true);
                for (int currRoutePointGroup: currNotSignedRoutePointGroups) {
                    if (userGroupsActivity.get(currRoutePointGroup) == null) {
                        continue;
                    }
                    boolean disable = userGroupsActivity.get(currRoutePointGroup) == Group.ActorType.SPECTATOR;
                    viewElements.setDisableBtn(viewElements.isDisableBtn() & disable);
                }

                viewElements.setBtnAccept(true);
                viewElements.setBtnReject(true);
                if (currentUser.getCurrRole().isCanRouteChange()) {
                    viewElements.setElChangeRoute(true);
                }
                if (currentUser.getCurrRole().isCanEdit()) {
                    viewElements.setElEditDoc(true);
                }
                if (currentUser.getCurrRole().isCanComment()) {
                    viewElements.setElSignerComment(true);
                }
                break;
            case Menu.Page.MY_DOC:
                if (userGroupsActivity.get(document.getDocumentGroupId()) == Group.ActorType.SPECTATOR) {
                    viewElements.setDisableBtn(true);
                }

                if (currentUser.getUserCreateGroups().size() > 1) {
                    viewElements.setElDocOwner(true);
                }

                switch (document.getStatusId()) {
                    case Status.Id.COMPLETED:
                        viewElements.setBtnCopy(true);
                        if (currentUser.isPublicRole()) {
                            viewElements.setBtnPublish(true);
                            viewElements.setElPublish(true);
                        }
                        break;
                    case Status.Id.PUBLISHED:
                        if (currentUser.isPublicRole()) {
                            viewElements.setBtnPublishCancel(true);
                        }
                        break;
                    case Status.Id.CANCELED:
                        viewElements.setBtnCopy(true);
                        break;
                    case Status.Id.CREATED:
                        viewElements.setBtnSaveAndSend(true);
                        viewElements.setBtnSave(true);
                        viewElements.setBtnDelete(true);
                        break;
                }

                if (document.getStatusId() >= Status.Id.ON_COORDINATION) {
                    viewElements.setBtnRecall(true);
                }

                break;
        }

        document.setElems(viewElements);
    }

    private void offsetDateAndFormat(Document document) {
        Date inputDate = document.getCreateDate();
        if (inputDate != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(inputDate.toInstant(), localeBean.getZoneId());
//            document.setCreateDate(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            document.setCreateDateFormatted(localDateTime.format(localeBean.getDateFormatter()));
        }
    }
}

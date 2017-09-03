package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.RouteProcessDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.FieldType;
import ru.pioneersystem.pioneer2.model.RoutePoint;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service("routeProcessService")
public class RouteProcessServiceImpl implements RouteProcessService {
    private EventService eventService;
    private RouteProcessDao routeProcessDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public RouteProcessServiceImpl(EventService eventService, RouteProcessDao routeProcessDao, CurrentUser currentUser,
                                   LocaleBean localeBean, MessageSource messageSource) {
        this.eventService = eventService;
        this.routeProcessDao = routeProcessDao;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<RoutePoint> getDocumentRoute(int documentId) throws ServiceException {
        try {
            List<RoutePoint> routePoints = routeProcessDao.getRoute(documentId);
            offsetDateAndFormat(routePoints);
            eventService.logEvent(Event.Type.ROUTE_PROCESS_GETED, documentId);
            return routePoints;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.routeProcess.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), documentId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<Integer> getCurrNotSignedRoutePointGroups(int documentId) throws ServiceException {
        try {
            return routeProcessDao.getCurrNotSignedRoutePointGroups(documentId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.routeProcess.CurrentRoutePointNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), documentId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void createRouteProcess(Document document) throws ServiceException {
        try {
            routeProcessDao.create(document.getId(), getCalculatedRoute(document));
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.routeProcess.NotCreated", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startRouteProcess(Document document) throws ServiceException {
        try {
            routeProcessDao.start(document.getId(), currentUser.getUser().getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.routeProcess.NotStarted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void acceptRoutePointProcess(Document document) throws ServiceException {
        try {
            routeProcessDao.accept(document.getId(), currentUser.getUser().getId(), document.getSignerComment(),
                    document.isNewRoute(), document.getNewRouteId());
        } catch (NotFoundDaoException e) {
            String mess = messageSource.getMessage("error.routeProcess.PointAlreadyProcessed", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.routeProcess.PointNotAccepted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void rejectRoutePointProcess(Document document) throws ServiceException {
        try {
            routeProcessDao.reject(document.getId(), currentUser.getUser().getId(), document.getSignerComment());
        } catch (NotFoundDaoException e) {
            String mess = messageSource.getMessage("error.routeProcess.PointAlreadyProcessed", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.routeProcess.PointNotRejected", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void cancelRouteProcess(Document document) throws ServiceException {
        try {
            String mess = messageSource.getMessage("routeProcess.mess.CreatorCancel", null, localeBean.getLocale());
            routeProcessDao.cancel(document.getId(), currentUser.getUser().getId(), mess);
        } catch (NotFoundDaoException e) {
            String mess = messageSource.getMessage("error.routeProcess.AlreadyFinished", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.routeProcess.NotCanceled", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), document.getId());
            throw new ServiceException(mess, e);
        }
    }

    private void offsetDateAndFormat(List<RoutePoint> routePoints) {
        for (RoutePoint routePoint: routePoints) {
            Date receiptDate = routePoint.getReceiptDate();
            if (receiptDate != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(receiptDate.toInstant(), localeBean.getZoneId());
                routePoint.setReceiptDate(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                routePoint.setReceiptDateFormatted(localDateTime.format(localeBean.getDateTimeFormatter()));
            }

            Date signDate = routePoint.getSignDate();
            if (signDate != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(signDate.toInstant(), localeBean.getZoneId());
                routePoint.setSignDate(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                routePoint.setSignDateFormatted(localDateTime.format(localeBean.getDateTimeFormatter()));
            }
        }
    }

    private int getCalculatedRoute(Document document) {
        int docRoute = document.getRouteId();

        if (document.isNewRoute()) {
            docRoute = document.getNewRouteId();
        } else {
            int oldRoute = docRoute;
            int oldCondNum = 0;
            boolean summConditionResult = true;
            boolean conditionResult;

            for (Document.Condition condition: document.getConditions()) {
                //fieldValue = getFieldValue(curDocFields.get(fieldNum -1));
                String fieldValue = getFieldValue(document, condition.getFieldNum());

                conditionResult = conditionResult(fieldValue, condition.getCond(), condition.getValue());

                if (oldCondNum == condition.getCondNum()) {
                    summConditionResult = summConditionResult & conditionResult;
                    if (summConditionResult) {
                        docRoute = condition.getRouteId();
                    }
                    else {
                        docRoute = oldRoute;
                    }
                }
                else {
                    summConditionResult = conditionResult;
                    if (summConditionResult) {
                        docRoute = condition.getRouteId();
                    }
                }

                oldCondNum = condition.getCondNum();
            }
        }

        return docRoute;
    }

    private String getFieldValue(Document document, int fieldNum) {
        String fieldValue = "";

        for (Document.Field fields : document.getFields()) {
            if (fields.getNum() == fieldNum) {
                switch (fields.getTypeId()) {
                    case FieldType.Id.TEXT_STRING:
                        fieldValue = fields.getValueTextField();
                        break;
                    case FieldType.Id.LIST:
                        fieldValue = fields.getValueChoiceList();
                        break;
                    case FieldType.Id.CALENDAR:
                        fieldValue = LocalDateTime.ofInstant(fields.getValueCalendar().toInstant(),
                                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                        break;
                    case FieldType.Id.CHECKBOX:
                        fieldValue = fields.getValueCheckBox() ? "1" : "0";
                        break;
                    case FieldType.Id.TEXT_AREA:
                        fieldValue = fields.getValueTextArea();
                        break;
                }
            }
        }

        return fieldValue;
    }

    private boolean conditionResult(String fieldValue, String condition, String conditionValue) {
        boolean result = false;

        if (Document.Condition.Operation.EQ.equals(condition)) {
            result = fieldValue.equals(conditionValue);
        } else if (Document.Condition.Operation.NE.equals(condition)) {
            result = !fieldValue.equals(conditionValue);
        } else if (Document.Condition.Operation.CNT.equals(condition)) {
            result = fieldValue.contains(conditionValue);
        } else if (Document.Condition.Operation.GT.equals(condition)) {
            result = (fieldValue.compareTo(conditionValue) > 0);
        } else if (Document.Condition.Operation.GE.equals(condition)) {
            result = (fieldValue.compareTo(conditionValue) >= 0);
        } else if (Document.Condition.Operation.LT.equals(condition)) {
            result = (fieldValue.compareTo(conditionValue) < 0);
        } else if (Document.Condition.Operation.LE.equals(condition)) {
            result = (fieldValue.compareTo(conditionValue) <= 0);
        }

        return result;
    }
}

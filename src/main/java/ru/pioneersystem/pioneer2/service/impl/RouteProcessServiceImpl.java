package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.RouteProcessDao;
import ru.pioneersystem.pioneer2.dao.exception.LockException;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.FieldType;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.UserLockException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service("routeProcessService")
public class RouteProcessServiceImpl implements RouteProcessService {
    private Logger log = LoggerFactory.getLogger(RouteProcessServiceImpl.class);

    private RouteProcessDao routeProcessDao;
    private UserService userService;

    @Autowired
    public RouteProcessServiceImpl(RouteProcessDao routeProcessDao, UserService userService) {
        this.routeProcessDao = routeProcessDao;
        this.userService = userService;
    }

    @Override
    public void createRouteProcess(Document document) throws ServiceException, UserLockException {
        try {
            routeProcessDao.create(document, getCalculatedRoute(document));
        } catch (DataAccessException e) {
            log.error("Can't update Route Process", e);
            throw new ServiceException("Can't update Route Process", e);
        } catch (LockException e) {
            log.error("Can't update Route Process", e);
            try {
                throw new UserLockException(userService.getUser(e.getUserId()), e.getDate(), e);
            } catch (ServiceException se) {
                throw new UserLockException(new User(), e.getDate(), e);
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

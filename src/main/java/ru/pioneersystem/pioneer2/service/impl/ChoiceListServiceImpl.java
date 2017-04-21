package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.ChoiceListDao;
import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.service.ChoiceListService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;

import java.util.List;

@Service("choiceListService")
public class ChoiceListServiceImpl implements ChoiceListService {
    private Logger log = LoggerFactory.getLogger(ChoiceListServiceImpl.class);

    private ChoiceListDao choiceListDao;
    private CurrentUser currentUser;

    @Autowired
    public void setChoiceListDao(ChoiceListDao choiceListDao, CurrentUser currentUser) {
        this.choiceListDao = choiceListDao;
        this.currentUser = currentUser;
    }

    @Override
    public ChoiceList getChoiceList(int id) throws ServiceException {
        try {
            return choiceListDao.get(id);
        } catch (DataAccessException e) {
            log.error("Can't get ChoiceList by id", e);
            throw new ServiceException("Can't get ChoiceList by id", e);
        }
    }

    @Override
    public List<ChoiceList> getChoiceListList() throws ServiceException {
        try {
            return choiceListDao.getList(currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't get list of ChoiceList", e);
            throw new ServiceException("Can't get list of ChoiceList", e);
        }
    }

    @Override
    public void createChoiceList(ChoiceList choiceList) throws ServiceException {
        try {
            choiceListDao.create(choiceList, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't create ChoiceList", e);
            throw new ServiceException("Can't create ChoiceList", e);
        }
    }

    @Override
    public void updateChoiceList(ChoiceList choiceList) throws ServiceException {
        try {
            choiceListDao.update(choiceList);
        } catch (DataAccessException e) {
            log.error("Can't update ChoiceList", e);
            throw new ServiceException("Can't update ChoiceList", e);
        }
    }

    @Override
    public void deleteChoiceList(int id) throws ServiceException {
        // TODO: 28.02.2017 Сделать проверку, используется ли данный список в шаблоне или нет
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            choiceListDao.delete(id);
        } catch (DataAccessException e) {
            log.error("Can't delete ChoiceList", e);
            throw new ServiceException("Can't delete ChoiceList", e);
        }
    }
}

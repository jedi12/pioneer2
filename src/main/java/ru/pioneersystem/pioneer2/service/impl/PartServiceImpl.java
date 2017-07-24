package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.PartDao;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.service.PartService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("partService")
public class PartServiceImpl implements PartService {
    private Logger log = LoggerFactory.getLogger(PartServiceImpl.class);

    private PartDao partDao;
    private CurrentUser currentUser;

    @Autowired
    public PartServiceImpl(PartDao partDao, CurrentUser currentUser) {
        this.partDao = partDao;
        this.currentUser = currentUser;
    }

    @Override
    public Part getPart(int partId) throws ServiceException {
        try {
            return partDao.get(partId);
        } catch (DataAccessException e) {
            log.error("Can't get Part by id", e);
            throw new ServiceException("Can't get Part by id", e);
        }
    }

    @Override
    public List<Part> getPartList(int type) throws ServiceException {
        try {
            return partDao.getList(type, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't get list of Part", e);
            throw new ServiceException("Can't get list of Part", e);
        }
    }

    @Override
    public Map<String, Integer> getPartMap(int type) throws ServiceException {
        Map<String, Integer> parts = new LinkedHashMap<>();
        for (Part part : getPartList(type)) {
            parts.put(part.getName(), part.getId());
        }
        return parts;
    }

    @Override
    public List<Part> getUserPartList(int type) throws ServiceException {
        try {
            return partDao.getUserPart(type, currentUser.getUser().getId());
        } catch (DataAccessException e) {
            log.error("Can't get list of user Part", e);
            throw new ServiceException("Can't get list of user Part", e);
        }
    }

    @Override
    public Map<String, Integer> getUserPartMap(int type) throws ServiceException {
        Map<String, Integer> parts = new LinkedHashMap<>();
        for (Part part : getUserPartList(type)) {
            parts.put(part.getName(), part.getId());
        }
        return parts;
    }

    @Override
    public void createPart(Part part, int type) throws ServiceException {
        try {
            partDao.create(part, type, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't create Part", e);
            throw new ServiceException("Can't create Part", e);
        }
    }

    @Override
    public void updatePart(Part part) throws ServiceException {
        try {
            partDao.update(part);
        } catch (DataAccessException e) {
            log.error("Can't update Part", e);
            throw new ServiceException("Can't update Part", e);
        }
    }

    @Override
    public void updateParts(List<Part> parts) throws ServiceException {
        try {
            partDao.update(parts);
        } catch (DataAccessException e) {
            log.error("Can't update Parts", e);
            throw new ServiceException("Can't update Parts", e);
        }
    }

    @Override
    public void deletePart(int partId) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного раздела плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            partDao.delete(partId);
        } catch (DataAccessException e) {
            log.error("Can't delete Part", e);
            throw new ServiceException("Can't delete Part", e);
        }
    }

    @Override
    public void deleteParts(List<Part> parts) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного раздела плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            partDao.delete(parts);
        } catch (DataAccessException e) {
            log.error("Can't delete Parts", e);
            throw new ServiceException("Can't delete Parts", e);
        }
    }
}
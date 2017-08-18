package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.DocumentDao;
import ru.pioneersystem.pioneer2.dao.PartDao;
import ru.pioneersystem.pioneer2.dao.TemplateDao;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.service.PartService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service("partService")
public class PartServiceImpl implements PartService {
    private Logger log = LoggerFactory.getLogger(PartServiceImpl.class);

    private PartDao partDao;
    private TemplateDao templadeDao;
    private DocumentDao documentDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public PartServiceImpl(PartDao partDao, TemplateDao templadeDao, DocumentDao documentDao, CurrentUser currentUser,
                           LocaleBean localeBean, MessageSource messageSource) {
        this.partDao = partDao;
        this.templadeDao = templadeDao;
        this.documentDao = documentDao;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<Part> getPartList(int type) throws ServiceException {
        try {
            return partDao.getList(type, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotLoadedList", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getPartMap(int type) throws ServiceException {
        Map<String, Integer> parts = new LinkedHashMap<>();
        for (Part part : getPartList(type)) {
            parts.put(part.getName(), part.getId());
        }
        parts.put(messageSource.getMessage("part.zero.name", null, localeBean.getLocale()), 0);
        return parts;
    }

    @Override
    public List<Part> getUserPartList(int type) throws ServiceException {
        try {
            return partDao.getUserPart(type, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.userPartNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
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
    public List<String> getTemplateListContainingInParts(List<Part> parts) throws ServiceException {
        try {
            return partDao.getTemplateListContainingInParts(parts, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.templateListContainingInPartsNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int getCountPubDocContainingInParts(List<Part> parts) throws ServiceException {
        try {
            return partDao.getPubDocContainingInParts(parts, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.countPubDocContainingInParts", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int getCountPartsWithRestriction(int groupId) throws ServiceException {
        try {
            return partDao.getCountPartsWithRestriction(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.countPartsWithRestriction", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Part getNewPart() {
        Part part = new Part();
        part.setLinkGroups(new LinkedList<>());
        part.setCreateFlag(true);
        return part;
    }

    @Override
    public Part getPart(int partId) throws ServiceException {
        try {
            Part part = partDao.get(partId, currentUser.getUser().getCompanyId());
            part.setCreateFlag(false);
            return part;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void savePart(Part part, int type) throws ServiceException {
        try {
            if (part.isCreateFlag()) {
                partDao.create(part, type, currentUser.getUser().getCompanyId());
            } else {
                partDao.update(part, type, currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotSaved", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void updateParts(List<Part> parts, int type) throws ServiceException {
        try {
            partDao.update(parts, type, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotSavedList", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
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
            partDao.delete(partId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotDeleted", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteParts(List<Part> parts, int partType) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного раздела плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            if (partType == Part.Type.FOR_TEMPLATES) {
                templadeDao.removeFromParts(parts, currentUser.getUser().getCompanyId());
            } else if (partType == Part.Type.FOR_DOCUMENTS) {
                documentDao.cancelPublish(parts, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            }
            partDao.delete(parts, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotDeletedList", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }
}
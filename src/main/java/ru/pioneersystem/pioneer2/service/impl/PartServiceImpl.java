package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.DocumentDao;
import ru.pioneersystem.pioneer2.dao.PartDao;
import ru.pioneersystem.pioneer2.dao.TemplateDao;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.PartService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("partService")
public class PartServiceImpl implements PartService {
    private EventService eventService;
    private PartDao partDao;
    private TemplateDao templadeDao;
    private DocumentDao documentDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public PartServiceImpl(EventService eventService, PartDao partDao, TemplateDao templadeDao,
                           DocumentDao documentDao, CurrentUser currentUser, LocaleBean localeBean,
                           MessageSource messageSource) {
        this.eventService = eventService;
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
            List<Part> parts;
            if (currentUser.isSuperRole()) {
                parts = partDao.getSuperList(type);
            } else {
                parts = partDao.getAdminList(type, currentUser.getUser().getCompanyId());
            }
            return parts;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), type);
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
            eventService.logError(mess, e.getMessage(), type);
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
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int getCountPubDocContainingInParts(List<Part> parts) throws ServiceException {
        try {
            return partDao.getPubDocContainingInParts(parts, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.countPubDocContainingInParts", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int getCountPartsWithRestriction(int groupId) throws ServiceException {
        try {
            return partDao.getCountPartsWithRestriction(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.countPartsWithRestriction", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), groupId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Part getNewPart() {
        Part part = new Part();
        part.setLinkGroups(new ArrayList<>());
        part.setCreateFlag(true);
        return part;
    }

    @Override
    public Part getPart(Part selectedPart) throws ServiceException {
        if (selectedPart == null) {
            String mess = messageSource.getMessage("error.part.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            int companyId;
            if (currentUser.isSuperRole()) {
                companyId = selectedPart.getCompanyId();
            } else {
                companyId = currentUser.getUser().getCompanyId();
            }

            Part part = partDao.get(selectedPart.getId(), companyId);
            part.setCreateFlag(false);
            eventService.logEvent(Event.Type.PART_GETED, selectedPart.getId());
            return part;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), selectedPart.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void savePart(Part part, int type) throws ServiceException {
        try {
            if (part.isCreateFlag()) {
                int partId = partDao.create(part, type, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.PART_GETED, partId);
            } else {
                partDao.update(part, type, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.PART_CHANGED, part.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), part.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void updateParts(List<Part> parts, int type) throws ServiceException {
        try {
            partDao.update(parts, type, currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.PART_TREE_REORDERED, 0);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotSavedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deletePart(Part part) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного раздела плюс еще какая-нибудь проверка
        try {
            partDao.delete(part.getId(), currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.PART_DELETED, part.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), part.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteParts(List<Part> parts, int partType) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системного раздела плюс еще какая-нибудь проверка
        try {
            if (partType == Part.Type.FOR_TEMPLATES) {
                templadeDao.removeFromParts(parts, currentUser.getUser().getCompanyId());
            } else if (partType == Part.Type.FOR_DOCUMENTS) {
                documentDao.cancelPublish(parts, currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            }
            partDao.delete(parts, currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.PART_LIST_DELETED, 0);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.NotDeletedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createExamplePart(int partType, int adminGroupId, int companyId) throws ServiceException {
        try {
            Part part = getNewPart();
            part.setParent(0);
            part.setTreeLevel(1);
            part.setOwnerGroup(adminGroupId);

            switch (partType) {
                case Part.Type.FOR_TEMPLATES:
                    part.setName(messageSource.getMessage("part.templates.name", null, localeBean.getLocale()));
                    break;
                case Part.Type.FOR_DOCUMENTS:
                    part.setName(messageSource.getMessage("part.documents.name", null, localeBean.getLocale()));
                    break;
                default:
                    String mess = messageSource.getMessage("error.part.exampleUnknown", null, localeBean.getLocale());
                    eventService.logError(mess, null);
                    throw new ServiceException(mess, null);
            }

            return partDao.create(part, partType, companyId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.part.exampleNotCreated", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }
}
package ru.pioneersystem.pioneer2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

@Service("dictionaryService")
public class DictionaryService {
	private Logger log = LoggerFactory.getLogger(DictionaryService.class);

	private LocaleBean localeBean;
	private MessageSource messageSource;

	@Autowired
	public DictionaryService(LocaleBean localeBean, MessageSource messageSource) {
		this.localeBean = localeBean;
		this.messageSource = messageSource;
	}

	public String getLocalizedRoleName(int roleId) {
		switch (roleId) {
			case Role.Id.SUPER:
				return messageSource.getMessage("role.name.super", null, localeBean.getLocale());
			case Role.Id.ADMIN:
				return messageSource.getMessage("role.name.admin", null, localeBean.getLocale());
			case Role.Id.USER:
				return messageSource.getMessage("role.name.user", null, localeBean.getLocale());
			case Role.Id.PUBLIC:
				return messageSource.getMessage("role.name.public", null, localeBean.getLocale());
			case Role.Id.REZ1:
				return messageSource.getMessage("role.name.rez1", null, localeBean.getLocale());
			case Role.Id.REZ2:
				return messageSource.getMessage("role.name.rez2", null, localeBean.getLocale());
			case Role.Id.REZ3:
				return messageSource.getMessage("role.name.rez3", null, localeBean.getLocale());
			case Role.Id.REZ4:
				return messageSource.getMessage("role.name.rez4", null, localeBean.getLocale());
			case Role.Id.CREATE:
				return messageSource.getMessage("role.name.create", null, localeBean.getLocale());
			case Role.Id.ON_COORDINATION:
				return messageSource.getMessage("role.name.coordinate", null, localeBean.getLocale());
			case Role.Id.ON_EXECUTION:
				return messageSource.getMessage("role.name.execute", null, localeBean.getLocale());
			default:
				return null;
		}
	}

	public String getLocalizedMenuName(int menuId) {
		switch (menuId) {
			case Menu.Id.PUB_DOCS:
				return messageSource.getMessage("menu.name.pubDocs", null, localeBean.getLocale());
			case Menu.Id.CREATE_DOCS:
				return messageSource.getMessage("menu.name.createDocs", null, localeBean.getLocale());
			case Menu.Id.SEARCH_DOCS:
				return messageSource.getMessage("menu.name.searchDocs", null, localeBean.getLocale());
			case Menu.Id.SETTINGS:
				return messageSource.getMessage("menu.name.settings", null, localeBean.getLocale());
			case Menu.Id.JOURNALS:
				return messageSource.getMessage("menu.name.journals", null, localeBean.getLocale());
			case Menu.Id.USERS:
				return messageSource.getMessage("menu.name.users", null, localeBean.getLocale());
			case Menu.Id.ROLES:
				return messageSource.getMessage("menu.name.roles", null, localeBean.getLocale());
			case Menu.Id.MY_DOCS:
				return messageSource.getMessage("menu.name.myDocs", null, localeBean.getLocale());
			case Menu.Id.GROUPS:
				return messageSource.getMessage("menu.name.groups", null, localeBean.getLocale());
			case Menu.Id.ROUTES:
				return messageSource.getMessage("menu.name.routes", null, localeBean.getLocale());
			case Menu.Id.PARTS:
				return messageSource.getMessage("menu.name.parts", null, localeBean.getLocale());
			case Menu.Id.LISTS:
				return messageSource.getMessage("menu.name.lists", null, localeBean.getLocale());
			case Menu.Id.TEMPLATES:
				return messageSource.getMessage("menu.name.templates", null, localeBean.getLocale());
			case Menu.Id.EVENTS:
				return messageSource.getMessage("menu.name.events", null, localeBean.getLocale());
			case Menu.Id.NOTICES:
				return messageSource.getMessage("menu.name.notices", null, localeBean.getLocale());
			case Menu.Id.MENUS:
				return messageSource.getMessage("menu.name.menus", null, localeBean.getLocale());
			case Menu.Id.ON_ROUTE_CONFIRM:
				return messageSource.getMessage("menu.name.onRouteConfirm", null, localeBean.getLocale());
			case Menu.Id.ON_ROUTE_EXEC:
				return messageSource.getMessage("menu.name.onRouteExec", null, localeBean.getLocale());
			case Menu.Id.COMPANY:
				return messageSource.getMessage("menu.name.company", null, localeBean.getLocale());
			default:
				return null;
		}
	}

	public String getLocalizedStatusName(int statusId) {
		switch (statusId) {
			case Status.Id.DELETED:
				return messageSource.getMessage("status.name.deleted", null, localeBean.getLocale());
			case Status.Id.CANCELED:
				return messageSource.getMessage("status.name.canceled", null, localeBean.getLocale());
			case Status.Id.COMPLETED:
				return messageSource.getMessage("status.name.completed", null, localeBean.getLocale());
			case Status.Id.EXECUTED:
				return messageSource.getMessage("status.name.executed", null, localeBean.getLocale());
			case Status.Id.PUBLISHED:
				return messageSource.getMessage("status.name.published", null, localeBean.getLocale());
			case Status.Id.REZ1:
				return messageSource.getMessage("status.name.rez1", null, localeBean.getLocale());
			case Status.Id.REZ2:
				return messageSource.getMessage("status.name.rez2", null, localeBean.getLocale());
			case Status.Id.REZ3:
				return messageSource.getMessage("status.name.rez3", null, localeBean.getLocale());
			case Status.Id.CREATED:
				return messageSource.getMessage("status.name.created", null, localeBean.getLocale());
			case Status.Id.ON_COORDINATION:
				return messageSource.getMessage("status.name.onCoordination", null, localeBean.getLocale());
			case Status.Id.ON_EXECUTION:
				return messageSource.getMessage("status.name.onExecution", null, localeBean.getLocale());
			default:
				return null;
		}
	}

	public String getLocalizedStateName(int userId) {
		switch (userId) {
			case User.State.LOCKED:
				return messageSource.getMessage("status.locked", null, localeBean.getLocale());
			case User.State.ACTIVE:
				return messageSource.getMessage("status.active", null, localeBean.getLocale());
			default:
				return null;
		}
	}

	public String getLocalizedFieldTypeName(int fieldTypeId) {
		switch (fieldTypeId) {
			case FieldType.Id.TEXT_STRING:
				return messageSource.getMessage("fieldType.name.textString", null, localeBean.getLocale());
			case FieldType.Id.LIST:
				return messageSource.getMessage("fieldType.name.list", null, localeBean.getLocale());
			case FieldType.Id.CALENDAR:
				return messageSource.getMessage("fieldType.name.calendar", null, localeBean.getLocale());
			case FieldType.Id.CHECKBOX:
				return messageSource.getMessage("fieldType.name.checkbox", null, localeBean.getLocale());
			case FieldType.Id.TEXT_AREA:
				return messageSource.getMessage("fieldType.name.textArea", null, localeBean.getLocale());
			case FieldType.Id.FILE:
				return messageSource.getMessage("fieldType.name.file", null, localeBean.getLocale());
			case FieldType.Id.LINE:
				return messageSource.getMessage("fieldType.name.line", null, localeBean.getLocale());
			case FieldType.Id.EMPTY_STRING:
				return messageSource.getMessage("fieldType.name.emptyString", null, localeBean.getLocale());
			case FieldType.Id.TITLE:
				return messageSource.getMessage("fieldType.name.title", null, localeBean.getLocale());
			default:
				return null;
		}
	}

	public String getLocalizedFieldTypeTypeName(int fieldTypeId) {
		switch (fieldTypeId) {
			case FieldType.Type.INPUT:
				return messageSource.getMessage("fieldType.type.input", null, localeBean.getLocale());
			case FieldType.Type.DECORATE:
				return messageSource.getMessage("fieldType.type.decorate", null, localeBean.getLocale());
			default:
				return null;
		}
	}
}
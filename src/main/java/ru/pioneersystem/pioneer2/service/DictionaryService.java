package ru.pioneersystem.pioneer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

@Service("dictionaryService")
public class DictionaryService {
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

	public String getLocalizedEventTypeName(int eventTypeId) {
		switch (eventTypeId) {
			case Event.Type.ERROR:
				return messageSource.getMessage("event.name.error", null, localeBean.getLocale());
			case Event.Type.CHANNEL_SN:
				return messageSource.getMessage("event.name.channelSN", null, localeBean.getLocale());
			case Event.Type.CHANNEL_MC:
				return messageSource.getMessage("event.name.channelMC", null, localeBean.getLocale());
			case Event.Type.CHANNEL_CD:
				return messageSource.getMessage("event.name.channelCD", null, localeBean.getLocale());
			case Event.Type.NOTICE_DOC_RECEIVED:
				return messageSource.getMessage("event.name.noticeDocReceived", null, localeBean.getLocale());
			case Event.Type.NOTICE_DOC_STATUS_CHANGED:
				return messageSource.getMessage("event.name.noticeDocStatusChanged", null, localeBean.getLocale());
			case Event.Type.PROC_SYNC_COMPLETED:
				return messageSource.getMessage("event.name.procSyncCompleted", null, localeBean.getLocale());

			case Event.Type.DOC_ERROR:
				return messageSource.getMessage("event.name.docError", null, localeBean.getLocale());
			case Event.Type.DOC_PUBLISHED:
				return messageSource.getMessage("event.name.docPublished", null, localeBean.getLocale());
			case Event.Type.DOC_PUBLISH_CANCELED:
				return messageSource.getMessage("event.name.docPublishCanceled", null, localeBean.getLocale());
			case Event.Type.DOC_GETED:
				return messageSource.getMessage("event.name.docGeted", null, localeBean.getLocale());
			case Event.Type.DOC_CREATED:
				return messageSource.getMessage("event.name.docCreated", null, localeBean.getLocale());
			case Event.Type.DOC_CHANGED:
				return messageSource.getMessage("event.name.docChanged", null, localeBean.getLocale());
			case Event.Type.DOC_SENDED:
				return messageSource.getMessage("event.name.docSended", null, localeBean.getLocale());
			case Event.Type.DOC_SAVED:
				return messageSource.getMessage("event.name.docSaved", null, localeBean.getLocale());
			case Event.Type.DOC_ROUTE_GETED:
				return messageSource.getMessage("event.name.docRouteGeted", null, localeBean.getLocale());
			case Event.Type.DOC_RECALLED:
				return messageSource.getMessage("event.name.docRecalled", null, localeBean.getLocale());
			case Event.Type.DOC_COPIED:
				return messageSource.getMessage("event.name.docCopied", null, localeBean.getLocale());
			case Event.Type.DOC_DELETED:
				return messageSource.getMessage("event.name.docDeleted", null, localeBean.getLocale());
			case Event.Type.DOC_ACCEPTED:
				return messageSource.getMessage("event.name.docAccepted", null, localeBean.getLocale());
			case Event.Type.DOC_REJECTED:
				return messageSource.getMessage("event.name.docRejected", null, localeBean.getLocale());
			case Event.Type.DOC_ROUTE_CHANGED:
				return messageSource.getMessage("event.name.docRouteChanged", null, localeBean.getLocale());
			case Event.Type.DOC_FILE_UPLOADED:
				return messageSource.getMessage("event.name.docFileUploaded", null, localeBean.getLocale());
			case Event.Type.DOC_FILE_DOWNLOADED:
				return messageSource.getMessage("event.name.docFileDownloaded", null, localeBean.getLocale());

			case Event.Type.GROUP_ERROR:
				return messageSource.getMessage("event.name.groupError", null, localeBean.getLocale());
			case Event.Type.GROUP_CREATED:
				return messageSource.getMessage("event.name.groupCreated", null, localeBean.getLocale());
			case Event.Type.GROUP_CHANGED:
				return messageSource.getMessage("event.name.groupChanged", null, localeBean.getLocale());
			case Event.Type.GROUP_GETED:
				return messageSource.getMessage("event.name.groupGeted", null, localeBean.getLocale());
			case Event.Type.GROUP_DELETED:
				return messageSource.getMessage("event.name.groupDeleted", null, localeBean.getLocale());
			case Event.Type.GROUP_GETED_FROM_ROUTE:
				return messageSource.getMessage("event.name.groupGetedFromRoute", null, localeBean.getLocale());

			case Event.Type.LIST_ERROR:
				return messageSource.getMessage("event.name.listError", null, localeBean.getLocale());
			case Event.Type.LIST_CREATED:
				return messageSource.getMessage("event.name.listCreated", null, localeBean.getLocale());
			case Event.Type.LIST_CHANGED:
				return messageSource.getMessage("event.name.listChanged", null, localeBean.getLocale());
			case Event.Type.LIST_GETED:
				return messageSource.getMessage("event.name.listGeted", null, localeBean.getLocale());
			case Event.Type.LIST_DELETED:
				return messageSource.getMessage("event.name.listDeleted", null, localeBean.getLocale());

			case Event.Type.PART_ERROR:
				return messageSource.getMessage("event.name.partError", null, localeBean.getLocale());
			case Event.Type.PART_CREATED:
				return messageSource.getMessage("event.name.partCreated", null, localeBean.getLocale());
			case Event.Type.PART_CHANGED:
				return messageSource.getMessage("event.name.partChanged", null, localeBean.getLocale());
			case Event.Type.PART_GETED:
				return messageSource.getMessage("event.name.partGeted", null, localeBean.getLocale());
			case Event.Type.PART_DELETED:
				return messageSource.getMessage("event.name.partDeleted", null, localeBean.getLocale());
			case Event.Type.PART_TREE_REORDERED:
				return messageSource.getMessage("event.name.partTreeReordered", null, localeBean.getLocale());
			case Event.Type.PART_LIST_DELETED:
				return messageSource.getMessage("event.name.partListDeleted", null, localeBean.getLocale());

			case Event.Type.ROUTE_ERROR:
				return messageSource.getMessage("event.name.routeError", null, localeBean.getLocale());
			case Event.Type.ROUTE_CREATED:
				return messageSource.getMessage("event.name.routeCreated", null, localeBean.getLocale());
			case Event.Type.ROUTE_CHANGED:
				return messageSource.getMessage("event.name.routeChanged", null, localeBean.getLocale());
			case Event.Type.ROUTE_GETED:
				return messageSource.getMessage("event.name.routeGeted", null, localeBean.getLocale());
			case Event.Type.ROUTE_DELETED:
				return messageSource.getMessage("event.name.routeDeleted", null, localeBean.getLocale());

			case Event.Type.TEMPLATE_ERROR:
				return messageSource.getMessage("event.name.templateError", null, localeBean.getLocale());
			case Event.Type.TEMPLATE_CREATED:
				return messageSource.getMessage("event.name.templateCreated", null, localeBean.getLocale());
			case Event.Type.TEMPLATE_CHANGED:
				return messageSource.getMessage("event.name.templateChanged", null, localeBean.getLocale());
			case Event.Type.TEMPLATE_GETED:
				return messageSource.getMessage("event.name.templateGeted", null, localeBean.getLocale());
			case Event.Type.TEMPLATE_DELETED:
				return messageSource.getMessage("event.name.templateDeleted", null, localeBean.getLocale());
			case Event.Type.TEMPLATE_GETED_FOR_DOC_CREATE:
				return messageSource.getMessage("event.name.templateGetedForDocCreate", null, localeBean.getLocale());

			case Event.Type.USER_ERROR:
				return messageSource.getMessage("event.name.userError", null, localeBean.getLocale());
			case Event.Type.USER_CREATED:
				return messageSource.getMessage("event.name.userCreated", null, localeBean.getLocale());
			case Event.Type.USER_CHANGED:
				return messageSource.getMessage("event.name.userChanged", null, localeBean.getLocale());
			case Event.Type.USER_GETED:
				return messageSource.getMessage("event.name.userGeted", null, localeBean.getLocale());
			case Event.Type.USER_LOCKED:
				return messageSource.getMessage("event.name.userLocked", null, localeBean.getLocale());
			case Event.Type.USER_UNLOCKED:
				return messageSource.getMessage("event.name.userUnlocked", null, localeBean.getLocale());
			case Event.Type.USER_FIRST_ADMIN_SETTED:
				return messageSource.getMessage("event.name.userFirstAdminSetted", null, localeBean.getLocale());
			case Event.Type.USER_AUTO_CREATED:
				return messageSource.getMessage("event.name.userAutoCreated", null, localeBean.getLocale());
			case Event.Type.USER_SIGN_IN_SSO_SUCCESS:
				return messageSource.getMessage("event.name.userSignInSsoSuccess", null, localeBean.getLocale());
			case Event.Type.USER_SIGN_IN_SSO_FAIL:
				return messageSource.getMessage("event.name.userSignInSsoFail", null, localeBean.getLocale());
			case Event.Type.USER_SIGNED_IN:
				return messageSource.getMessage("event.name.userSignedIn", null, localeBean.getLocale());
			case Event.Type.USER_TRY_SIGN_IN:
				return messageSource.getMessage("event.name.userTrySignIn", null, localeBean.getLocale());
			case Event.Type.USER_SIGN_IN_EMAIL_SUCCESS:
				return messageSource.getMessage("event.name.userSignInEmailSuccess", null, localeBean.getLocale());
			case Event.Type.USER_SIGN_IN_EMAIL_FAIL:
				return messageSource.getMessage("event.name.userSignInEmailFail", null, localeBean.getLocale());
			case Event.Type.USER_SIGNED_OUT:
				return messageSource.getMessage("event.name.userSignedOut", null, localeBean.getLocale());
			case Event.Type.USER_PASS_SETUP:
				return messageSource.getMessage("event.name.userPassSetup", null, localeBean.getLocale());
			case Event.Type.USER_PASS_CHANGED:
				return messageSource.getMessage("event.name.userPassChanged", null, localeBean.getLocale());

			case Event.Type.ROLE_ERROR:
				return messageSource.getMessage("event.name.roleError", null, localeBean.getLocale());
			case Event.Type.ROLE_CREATED:
				return messageSource.getMessage("event.name.roleCreated", null, localeBean.getLocale());
			case Event.Type.ROLE_CHANGED:
				return messageSource.getMessage("event.name.roleChanged", null, localeBean.getLocale());
			case Event.Type.ROLE_GETED:
				return messageSource.getMessage("event.name.roleGeted", null, localeBean.getLocale());
			case Event.Type.ROLE_DELETED:
				return messageSource.getMessage("event.name.roleDeleted", null, localeBean.getLocale());

			case Event.Type.MENU_ERROR:
				return messageSource.getMessage("event.name.menuError", null, localeBean.getLocale());
			case Event.Type.MENU_CREATED:
				return messageSource.getMessage("event.name.menuCreated", null, localeBean.getLocale());
			case Event.Type.MENU_CHANGED:
				return messageSource.getMessage("event.name.menuChanged", null, localeBean.getLocale());
			case Event.Type.MENU_GETED:
				return messageSource.getMessage("event.name.menuGeted", null, localeBean.getLocale());
			case Event.Type.MENU_DELETED:
				return messageSource.getMessage("event.name.menuDeleted", null, localeBean.getLocale());
			case Event.Type.MENU_REORDERED:
				return messageSource.getMessage("event.name.menuReordered", null, localeBean.getLocale());

			case Event.Type.COMPANY_ERROR:
				return messageSource.getMessage("event.name.companyError", null, localeBean.getLocale());
			case Event.Type.COMPANY_CREATED:
				return messageSource.getMessage("event.name.companyCreated", null, localeBean.getLocale());
			case Event.Type.COMPANY_CHANGED:
				return messageSource.getMessage("event.name.companyChanged", null, localeBean.getLocale());
			case Event.Type.COMPANY_GETED:
				return messageSource.getMessage("event.name.companyGeted", null, localeBean.getLocale());
			case Event.Type.COMPANY_LOCKED:
				return messageSource.getMessage("event.name.companyLocked", null, localeBean.getLocale());
			case Event.Type.COMPANY_UNLOCKED:
				return messageSource.getMessage("event.name.companyUnlocked", null, localeBean.getLocale());

			case Event.Type.ROUTE_PROCESS_ERROR:
				return messageSource.getMessage("event.name.routeProcessError", null, localeBean.getLocale());
			case Event.Type.ROUTE_PROCESS_GETED:
				return messageSource.getMessage("event.name.routeProcessGeted", null, localeBean.getLocale());

			case Event.Type.SEARCH_ERROR:
				return messageSource.getMessage("event.name.searchError", null, localeBean.getLocale());
			case Event.Type.SEARCH_FIND:
				return messageSource.getMessage("event.name.searchFind", null, localeBean.getLocale());

			default:
				return null;
		}
	}
}
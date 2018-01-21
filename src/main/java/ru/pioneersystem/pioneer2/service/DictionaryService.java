package ru.pioneersystem.pioneer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.Locale;

@Service("dictionaryService")
public class DictionaryService {
	private LocaleBean localeBean;
	private MessageSource messageSource;

	@Autowired
	public DictionaryService(LocaleBean localeBean, MessageSource messageSource) {
		this.localeBean = localeBean;
		this.messageSource = messageSource;
	}

	public String getLocalizedRoleName(int roleId, Locale locale) {
		switch (roleId) {
			case Role.Id.SUPER:
				return messageSource.getMessage("role.name.super", null, locale);
			case Role.Id.ADMIN:
				return messageSource.getMessage("role.name.admin", null, locale);
			case Role.Id.USER:
				return messageSource.getMessage("role.name.user", null, locale);
			case Role.Id.PUBLIC:
				return messageSource.getMessage("role.name.public", null, locale);
			case Role.Id.REZ1:
				return messageSource.getMessage("role.name.rez1", null, locale);
			case Role.Id.REZ2:
				return messageSource.getMessage("role.name.rez2", null, locale);
			case Role.Id.REZ3:
				return messageSource.getMessage("role.name.rez3", null, locale);
			case Role.Id.REZ4:
				return messageSource.getMessage("role.name.rez4", null, locale);
			case Role.Id.CREATE:
				return messageSource.getMessage("role.name.create", null, locale);
			default:
				return null;
		}
	}

	public String getLocalizedMenuName(int menuId, Locale locale) {
		switch (menuId) {
			case Menu.Id.PUB_DOCS:
				return messageSource.getMessage("menu.name.pubDocs", null, locale);
			case Menu.Id.CREATE_DOCS:
				return messageSource.getMessage("menu.name.createDocs", null, locale);
			case Menu.Id.SEARCH_DOCS:
				return messageSource.getMessage("menu.name.searchDocs", null, locale);
			case Menu.Id.SETTINGS:
				return messageSource.getMessage("menu.name.settings", null, locale);
			case Menu.Id.JOURNALS:
				return messageSource.getMessage("menu.name.journals", null, locale);
			case Menu.Id.USERS:
				return messageSource.getMessage("menu.name.users", null, locale);
			case Menu.Id.ROLES:
				return messageSource.getMessage("menu.name.roles", null, locale);
			case Menu.Id.MY_DOCS:
				return messageSource.getMessage("menu.name.myDocs", null, locale);
			case Menu.Id.GROUPS:
				return messageSource.getMessage("menu.name.groups", null, locale);
			case Menu.Id.ROUTES:
				return messageSource.getMessage("menu.name.routes", null, locale);
			case Menu.Id.PARTS:
				return messageSource.getMessage("menu.name.parts", null, locale);
			case Menu.Id.LISTS:
				return messageSource.getMessage("menu.name.lists", null, locale);
			case Menu.Id.TEMPLATES:
				return messageSource.getMessage("menu.name.templates", null, locale);
			case Menu.Id.EVENTS:
				return messageSource.getMessage("menu.name.events", null, locale);
			case Menu.Id.NOTICES:
				return messageSource.getMessage("menu.name.notices", null, locale);
			case Menu.Id.MENUS:
				return messageSource.getMessage("menu.name.menus", null, locale);
			case Menu.Id.COMPANY:
				return messageSource.getMessage("menu.name.company", null, locale);
			default:
				return null;
		}
	}

	public String getLocalizedStatusName(int statusId, Locale locale) {
		switch (statusId) {
			case Status.Id.DELETED:
				return messageSource.getMessage("status.name.deleted", null, locale);
			case Status.Id.CANCELED:
				return messageSource.getMessage("status.name.canceled", null, locale);
			case Status.Id.COMPLETED:
				return messageSource.getMessage("status.name.completed", null, locale);
			case Status.Id.PUBLISHED:
				return messageSource.getMessage("status.name.published", null, locale);
			case Status.Id.CREATED:
				return messageSource.getMessage("status.name.created", null, locale);
			default:
				return null;
		}
	}

	public String getLocalizedStateName(int userId, Locale locale) {
		switch (userId) {
			case User.State.LOCKED:
				return messageSource.getMessage("status.locked", null, locale);
			case User.State.ACTIVE:
				return messageSource.getMessage("status.active", null, locale);
			case User.State.SYSTEM:
				return messageSource.getMessage("status.system", null, locale);
			default:
				return null;
		}
	}

	public String getLocalizedFieldTypeName(int fieldTypeId, Locale locale) {
		switch (fieldTypeId) {
			case FieldType.Id.TEXT_STRING:
				return messageSource.getMessage("fieldType.name.textString", null, locale);
			case FieldType.Id.CHOICE_LIST:
				return messageSource.getMessage("fieldType.name.choiceList", null, locale);
			case FieldType.Id.CALENDAR:
				return messageSource.getMessage("fieldType.name.calendar", null, locale);
			case FieldType.Id.CHECKBOX:
				return messageSource.getMessage("fieldType.name.checkbox", null, locale);
			case FieldType.Id.TEXT_AREA:
				return messageSource.getMessage("fieldType.name.textArea", null, locale);
			case FieldType.Id.FILE:
				return messageSource.getMessage("fieldType.name.file", null, locale);
			case FieldType.Id.LINE:
				return messageSource.getMessage("fieldType.name.line", null, locale);
			case FieldType.Id.EMPTY_STRING:
				return messageSource.getMessage("fieldType.name.emptyString", null, locale);
			case FieldType.Id.TITLE:
				return messageSource.getMessage("fieldType.name.title", null, locale);
			default:
				return null;
		}
	}

	public String getLocalizedFieldTypeTypeName(int fieldTypeId, Locale locale) {
		switch (fieldTypeId) {
			case FieldType.Type.INPUT:
				return messageSource.getMessage("fieldType.type.input", null, locale);
			case FieldType.Type.DECORATE:
				return messageSource.getMessage("fieldType.type.decorate", null, locale);
			default:
				return null;
		}
	}

	public String getLocalizedEventTypeName(int eventTypeId, Locale locale) {
		switch (eventTypeId) {
			case Event.Type.ERROR:
				return messageSource.getMessage("event.name.error", null, locale);
			case Event.Type.CHANNEL_SEND_NOTICES:
				return messageSource.getMessage("event.name.channelSendNotices", null, locale);
			case Event.Type.CHANNEL_MAIL_COMMAND:
				return messageSource.getMessage("event.name.channelMailCommand", null, locale);
			case Event.Type.CHANNEL_CREATE_DOCS:
				return messageSource.getMessage("event.name.channelCreateDocs", null, locale);
			case Event.Type.PROC_SYNC_COMPLETED:
				return messageSource.getMessage("event.name.procSyncCompleted", null, locale);

			case Event.Type.DOC_ERROR:
				return messageSource.getMessage("event.name.docError", null, locale);
			case Event.Type.DOC_PUBLISHED:
				return messageSource.getMessage("event.name.docPublished", null, locale);
			case Event.Type.DOC_PUBLISH_CANCELED:
				return messageSource.getMessage("event.name.docPublishCanceled", null, locale);
			case Event.Type.DOC_GETED:
				return messageSource.getMessage("event.name.docGeted", null, locale);
			case Event.Type.DOC_CREATED:
				return messageSource.getMessage("event.name.docCreated", null, locale);
			case Event.Type.DOC_CHANGED:
				return messageSource.getMessage("event.name.docChanged", null, locale);
			case Event.Type.DOC_SENDED:
				return messageSource.getMessage("event.name.docSended", null, locale);
			case Event.Type.DOC_SAVED:
				return messageSource.getMessage("event.name.docSaved", null, locale);
			case Event.Type.DOC_ROUTE_GETED:
				return messageSource.getMessage("event.name.docRouteGeted", null, locale);
			case Event.Type.DOC_RECALLED:
				return messageSource.getMessage("event.name.docRecalled", null, locale);
			case Event.Type.DOC_COPIED:
				return messageSource.getMessage("event.name.docCopied", null, locale);
			case Event.Type.DOC_DELETED:
				return messageSource.getMessage("event.name.docDeleted", null, locale);
			case Event.Type.DOC_ACCEPTED:
				return messageSource.getMessage("event.name.docAccepted", null, locale);
			case Event.Type.DOC_REJECTED:
				return messageSource.getMessage("event.name.docRejected", null, locale);
			case Event.Type.DOC_ROUTE_CHANGED:
				return messageSource.getMessage("event.name.docRouteChanged", null, locale);
			case Event.Type.DOC_FILE_UPLOADED:
				return messageSource.getMessage("event.name.docFileUploaded", null, locale);
			case Event.Type.DOC_FILE_DOWNLOADED:
				return messageSource.getMessage("event.name.docFileDownloaded", null, locale);

			case Event.Type.GROUP_ERROR:
				return messageSource.getMessage("event.name.groupError", null, locale);
			case Event.Type.GROUP_CREATED:
				return messageSource.getMessage("event.name.groupCreated", null, locale);
			case Event.Type.GROUP_CHANGED:
				return messageSource.getMessage("event.name.groupChanged", null, locale);
			case Event.Type.GROUP_GETED:
				return messageSource.getMessage("event.name.groupGeted", null, locale);
			case Event.Type.GROUP_DELETED:
				return messageSource.getMessage("event.name.groupDeleted", null, locale);
			case Event.Type.GROUP_GETED_FROM_ROUTE:
				return messageSource.getMessage("event.name.groupGetedFromRoute", null, locale);

			case Event.Type.LIST_ERROR:
				return messageSource.getMessage("event.name.listError", null, locale);
			case Event.Type.LIST_CREATED:
				return messageSource.getMessage("event.name.listCreated", null, locale);
			case Event.Type.LIST_CHANGED:
				return messageSource.getMessage("event.name.listChanged", null, locale);
			case Event.Type.LIST_GETED:
				return messageSource.getMessage("event.name.listGeted", null, locale);
			case Event.Type.LIST_DELETED:
				return messageSource.getMessage("event.name.listDeleted", null, locale);

			case Event.Type.PART_ERROR:
				return messageSource.getMessage("event.name.partError", null, locale);
			case Event.Type.PART_CREATED:
				return messageSource.getMessage("event.name.partCreated", null, locale);
			case Event.Type.PART_CHANGED:
				return messageSource.getMessage("event.name.partChanged", null, locale);
			case Event.Type.PART_GETED:
				return messageSource.getMessage("event.name.partGeted", null, locale);
			case Event.Type.PART_DELETED:
				return messageSource.getMessage("event.name.partDeleted", null, locale);
			case Event.Type.PART_TREE_REORDERED:
				return messageSource.getMessage("event.name.partTreeReordered", null, locale);
			case Event.Type.PART_LIST_DELETED:
				return messageSource.getMessage("event.name.partListDeleted", null, locale);

			case Event.Type.ROUTE_ERROR:
				return messageSource.getMessage("event.name.routeError", null, locale);
			case Event.Type.ROUTE_CREATED:
				return messageSource.getMessage("event.name.routeCreated", null, locale);
			case Event.Type.ROUTE_CHANGED:
				return messageSource.getMessage("event.name.routeChanged", null, locale);
			case Event.Type.ROUTE_GETED:
				return messageSource.getMessage("event.name.routeGeted", null, locale);
			case Event.Type.ROUTE_DELETED:
				return messageSource.getMessage("event.name.routeDeleted", null, locale);

			case Event.Type.TEMPLATE_ERROR:
				return messageSource.getMessage("event.name.templateError", null, locale);
			case Event.Type.TEMPLATE_CREATED:
				return messageSource.getMessage("event.name.templateCreated", null, locale);
			case Event.Type.TEMPLATE_CHANGED:
				return messageSource.getMessage("event.name.templateChanged", null, locale);
			case Event.Type.TEMPLATE_GETED:
				return messageSource.getMessage("event.name.templateGeted", null, locale);
			case Event.Type.TEMPLATE_DELETED:
				return messageSource.getMessage("event.name.templateDeleted", null, locale);
			case Event.Type.TEMPLATE_GETED_FOR_DOC_CREATE:
				return messageSource.getMessage("event.name.templateGetedForDocCreate", null, locale);

			case Event.Type.USER_ERROR:
				return messageSource.getMessage("event.name.userError", null, locale);
			case Event.Type.USER_CREATED:
				return messageSource.getMessage("event.name.userCreated", null, locale);
			case Event.Type.USER_CHANGED:
				return messageSource.getMessage("event.name.userChanged", null, locale);
			case Event.Type.USER_GETED:
				return messageSource.getMessage("event.name.userGeted", null, locale);
			case Event.Type.USER_LOCKED:
				return messageSource.getMessage("event.name.userLocked", null, locale);
			case Event.Type.USER_UNLOCKED:
				return messageSource.getMessage("event.name.userUnlocked", null, locale);
			case Event.Type.USER_RESTRICTION_ACHIEVED:
				return messageSource.getMessage("event.name.userRestrictionAchieved", null, locale);
			case Event.Type.USER_AUTO_CREATED:
				return messageSource.getMessage("event.name.userAutoCreated", null, locale);
			case Event.Type.USER_SIGN_IN_SSO_SUCCESS:
				return messageSource.getMessage("event.name.userSignInSsoSuccess", null, locale);
			case Event.Type.USER_SIGN_IN_SSO_FAIL:
				return messageSource.getMessage("event.name.userSignInSsoFail", null, locale);
			case Event.Type.USER_SIGNED_IN:
				return messageSource.getMessage("event.name.userSignedIn", null, locale);
			case Event.Type.USER_TRY_SIGN_IN:
				return messageSource.getMessage("event.name.userTrySignIn", null, locale);
			case Event.Type.USER_SIGN_IN_EMAIL_SUCCESS:
				return messageSource.getMessage("event.name.userSignInEmailSuccess", null, locale);
			case Event.Type.USER_SIGN_IN_EMAIL_FAIL:
				return messageSource.getMessage("event.name.userSignInEmailFail", null, locale);
			case Event.Type.USER_SIGNED_OUT:
				return messageSource.getMessage("event.name.userSignedOut", null, locale);
			case Event.Type.USER_PASS_SETUP:
				return messageSource.getMessage("event.name.userPassSetup", null, locale);
			case Event.Type.USER_PASS_CHANGED:
				return messageSource.getMessage("event.name.userPassChanged", null, locale);

			case Event.Type.ROLE_ERROR:
				return messageSource.getMessage("event.name.roleError", null, locale);
			case Event.Type.ROLE_CREATED:
				return messageSource.getMessage("event.name.roleCreated", null, locale);
			case Event.Type.ROLE_CHANGED:
				return messageSource.getMessage("event.name.roleChanged", null, locale);
			case Event.Type.ROLE_GETED:
				return messageSource.getMessage("event.name.roleGeted", null, locale);
			case Event.Type.ROLE_DELETED:
				return messageSource.getMessage("event.name.roleDeleted", null, locale);

			case Event.Type.MENU_ERROR:
				return messageSource.getMessage("event.name.menuError", null, locale);
			case Event.Type.MENU_CREATED:
				return messageSource.getMessage("event.name.menuCreated", null, locale);
			case Event.Type.MENU_CHANGED:
				return messageSource.getMessage("event.name.menuChanged", null, locale);
			case Event.Type.MENU_GETED:
				return messageSource.getMessage("event.name.menuGeted", null, locale);
			case Event.Type.MENU_DELETED:
				return messageSource.getMessage("event.name.menuDeleted", null, locale);
			case Event.Type.MENU_REORDERED:
				return messageSource.getMessage("event.name.menuReordered", null, locale);

			case Event.Type.COMPANY_ERROR:
				return messageSource.getMessage("event.name.companyError", null, locale);
			case Event.Type.COMPANY_CREATED:
				return messageSource.getMessage("event.name.companyCreated", null, locale);
			case Event.Type.COMPANY_CHANGED:
				return messageSource.getMessage("event.name.companyChanged", null, locale);
			case Event.Type.COMPANY_GETED:
				return messageSource.getMessage("event.name.companyGeted", null, locale);
			case Event.Type.COMPANY_LOCKED:
				return messageSource.getMessage("event.name.companyLocked", null, locale);
			case Event.Type.COMPANY_UNLOCKED:
				return messageSource.getMessage("event.name.companyUnlocked", null, locale);

			case Event.Type.ROUTE_PROCESS_ERROR:
				return messageSource.getMessage("event.name.routeProcessError", null, locale);
			case Event.Type.ROUTE_PROCESS_GETED:
				return messageSource.getMessage("event.name.routeProcessGeted", null, locale);

			case Event.Type.SEARCH_ERROR:
				return messageSource.getMessage("event.name.searchError", null, locale);
			case Event.Type.SEARCH_FIND:
				return messageSource.getMessage("event.name.searchFind", null, locale);
			case Event.Type.SEARCH_FIND_RESTRICTION:
				return messageSource.getMessage("event.name.searchFindRestriction", null, locale);

			case Event.Type.EVENT_ERROR:
				return messageSource.getMessage("event.name.eventError", null, locale);
			case Event.Type.EVENT_FIND:
				return messageSource.getMessage("event.name.eventFind", null, locale);
			case Event.Type.EVENT_FIND_RESTRICTION:
				return messageSource.getMessage("event.name.eventFindRestriction", null, locale);

			case Event.Type.NOTICE_ERROR:
				return messageSource.getMessage("event.name.noticeError", null, locale);
			case Event.Type.NOTICE_FIND:
				return messageSource.getMessage("event.name.noticeFind", null, locale);
			case Event.Type.NOTICE_FIND_RESTRICTION:
				return messageSource.getMessage("event.name.noticeFindRestriction", null, locale);
			case Event.Type.NOTICE_DOC_RECEIVED:
				return messageSource.getMessage("event.name.noticeDocReceived", null, locale);
			case Event.Type.NOTICE_DOC_STATUS_CHANGED:
				return messageSource.getMessage("event.name.noticeDocStatusChanged", null, locale);

			default:
				return null;
		}
	}

	public String getLocalizedEmailSendStatus(int sendStatusId, Locale locale) {
		switch (sendStatusId) {
			case Notice.Status.PREPARED_TO_SENDED:
				return messageSource.getMessage("mailSendStatus.name.preparedToSend", null, locale);
			case Notice.Status.SENDED:
				return messageSource.getMessage("mailSendStatus.name.sended", null, locale);
			case Notice.Status.DELAYED:
				return messageSource.getMessage("mailSendStatus.name.delayed", null, locale);
			case Notice.Status.NOT_DELIVERED:
				return messageSource.getMessage("mailSendStatus.name.notDelivered", null, locale);
			case Notice.Status.DELIVERED:
				return messageSource.getMessage("mailSendStatus.name.delivered", null, locale);
			default:
				return null;
		}
	}
}
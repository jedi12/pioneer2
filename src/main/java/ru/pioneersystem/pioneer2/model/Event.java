package ru.pioneersystem.pioneer2.model;

import java.util.Date;

public class Event {
    private String id;
    private Date date;
    private String dateFormatted;
    private int userId;
    private String userName;
    private String userLogin;
    private int typeId;
    private String typeName;
    private int objectId;
    private String objectName;
    private String detail1;
    private String detail2;

    public static class Type {
        public static final int ERROR = 0;
        public static final int CHANNEL_SN = 1;
        public static final int CHANNEL_MC = 2;
        public static final int CHANNEL_CD = 3;
        public static final int NOTICE_DOC_RECEIVED = 8;
        public static final int NOTICE_DOC_STATUS_CHANGED = 9;
        public static final int PROC_SYNC_COMPLETED = 16;

        public static final int DOC_ERROR = 20;
        public static final int DOC_PUBLISHED = 21;
        public static final int DOC_PUBLISH_CANCELED = 22;
        public static final int DOC_GETED = 23;
        public static final int DOC_CREATED = 24;
        public static final int DOC_CHANGED = 36;
        public static final int DOC_SENDED = 25;
        public static final int DOC_SAVED = 26;
        public static final int DOC_ROUTE_GETED = 27;
        public static final int DOC_RECALLED = 28;
        public static final int DOC_COPIED = 29;
        public static final int DOC_DELETED = 30;
        public static final int DOC_ACCEPTED = 31;
        public static final int DOC_REJECTED = 32;
        public static final int DOC_ROUTE_CHANGED = 33;
        public static final int DOC_FILE_UPLOADED = 34;
        public static final int DOC_FILE_DOWNLOADED = 35;

        public static final int GROUP_ERROR = 50;
        public static final int GROUP_CREATED = 51;
        public static final int GROUP_CHANGED = 52;
        public static final int GROUP_GETED = 53;
        public static final int GROUP_DELETED = 54;
        public static final int GROUP_GETED_FROM_ROUTE = 55;

        public static final int LIST_ERROR = 60;
        public static final int LIST_CREATED = 61;
        public static final int LIST_CHANGED = 62;
        public static final int LIST_GETED = 63;
        public static final int LIST_DELETED = 64;

        public static final int PART_ERROR = 70;
        public static final int PART_CREATED = 71;
        public static final int PART_CHANGED = 72;
        public static final int PART_GETED = 73;
        public static final int PART_DELETED = 74;
        public static final int PART_TREE_REORDERED = 75;
        public static final int PART_LIST_DELETED = 76;

        public static final int ROUTE_ERROR = 80;
        public static final int ROUTE_CREATED = 81;
        public static final int ROUTE_CHANGED = 82;
        public static final int ROUTE_GETED = 83;
        public static final int ROUTE_DELETED = 84;

        public static final int TEMPLATE_ERROR = 90;
        public static final int TEMPLATE_CREATED = 91;
        public static final int TEMPLATE_CHANGED = 92;
        public static final int TEMPLATE_GETED = 93;
        public static final int TEMPLATE_DELETED = 94;
        public static final int TEMPLATE_GETED_FOR_DOC_CREATE = 95;

        public static final int USER_ERROR = 100;
        public static final int USER_CREATED = 101;
        public static final int USER_CHANGED = 102;
        public static final int USER_GETED = 103;
        public static final int USER_LOCKED = 104;
        public static final int USER_UNLOCKED = 105;
        public static final int USER_AUTO_CREATED = 106;
        public static final int USER_FIRST_ADMIN_SETTED = 107;
        public static final int USER_SIGN_IN_SSO_SUCCESS = 110;
        public static final int USER_SIGN_IN_SSO_FAIL = 111;
        public static final int USER_SIGNED_IN = 112;
        public static final int USER_TRY_SIGN_IN = 113;
        public static final int USER_SIGN_IN_EMAIL_SUCCESS = 114;
        public static final int USER_SIGN_IN_EMAIL_FAIL = 115;
        public static final int USER_SIGNED_OUT = 116;
        public static final int USER_PASS_SETUP = 118;
        public static final int USER_PASS_CHANGED = 119;

        public static final int ROLE_ERROR = 120;
        public static final int ROLE_CREATED = 121;
        public static final int ROLE_CHANGED = 122;
        public static final int ROLE_GETED = 123;
        public static final int ROLE_DELETED = 124;

        public static final int MENU_ERROR = 130;
        public static final int MENU_CREATED = 131;
        public static final int MENU_CHANGED = 132;
        public static final int MENU_GETED = 133;
        public static final int MENU_DELETED = 134;
        public static final int MENU_REORDERED = 135;

        public static final int COMPANY_ERROR = 140;
        public static final int COMPANY_CREATED = 141;
        public static final int COMPANY_CHANGED = 142;
        public static final int COMPANY_GETED = 143;
        public static final int COMPANY_LOCKED = 144;
        public static final int COMPANY_UNLOCKED = 145;

        public static final int ROUTE_PROCESS_ERROR = 150;
        public static final int ROUTE_PROCESS_GETED = 151;

        public static final int SEARCH_ERROR = 160;
        public static final int SEARCH_FIND = 161;
    }

    public Event() {

    }

    public Event(Date date, int userId, int typeId, int objectId, String detail1, String detail2) {
        this.date = date;
        this.userId = userId;
        this.typeId = typeId;
        this.objectId = objectId;
        this.detail1 = detail1;
        this.detail2 = detail2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateFormatted() {
        return dateFormatted;
    }

    public void setDateFormatted(String dateFormatted) {
        this.dateFormatted = dateFormatted;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getDetail1() {
        return detail1;
    }

    public void setDetail1(String detail1) {
        this.detail1 = detail1;
    }

    public String getDetail2() {
        return detail2;
    }

    public void setDetail2(String detail2) {
        this.detail2 = detail2;
    }
}
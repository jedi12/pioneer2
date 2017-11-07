package ru.pioneersystem.pioneer2.model;

import java.util.Date;

public class Notice {
    private int id;
    private String email;
    private int sendStatusId;
    private String sendStatusName;
    private int docId;
    private String docName;
    private String docGroupName;
    private int attempt;
    private Date changeDate;
    private String changeDateFormatted;
    private int docStatusId;
    private String docStatusName;
    private String info;
    private int eventId;
    private String eventName;
    private int menuId;

    public static class Status {
        public static final int PREPARED_TO_SENDED = 0;
        public static final int SENDED = 1;
        public static final int DELAYED = 2;
        public static final int NOT_DELIVERED = 3;
        public static final int DELIVERED = 4;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getSendStatusId() {
        return sendStatusId;
    }

    public void setSendStatusId(int sendStatusId) {
        this.sendStatusId = sendStatusId;
    }

    public String getSendStatusName() {
        return sendStatusName;
    }

    public void setSendStatusName(String sendStatusName) {
        this.sendStatusName = sendStatusName;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocGroupName() {
        return docGroupName;
    }

    public void setDocGroupName(String docGroupName) {
        this.docGroupName = docGroupName;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public String getChangeDateFormatted() {
        return changeDateFormatted;
    }

    public void setChangeDateFormatted(String changeDateFormatted) {
        this.changeDateFormatted = changeDateFormatted;
    }

    public int getDocStatusId() {
        return docStatusId;
    }

    public void setDocStatusId(int docStatusId) {
        this.docStatusId = docStatusId;
    }

    public String getDocStatusName() {
        return docStatusName;
    }

    public void setDocStatusName(String docStatusName) {
        this.docStatusName = docStatusName;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }
}
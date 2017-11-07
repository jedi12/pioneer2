package ru.pioneersystem.pioneer2.model;

import java.util.Date;

public class RoutePoint {
    private int stage;
    private int groupId;
    private String groupName;
    private int roleId;
    private int signed;
    private Date signDate;
    private String signDateFormatted;
    private int signUserId;
    private String signUserName;
    private String signMessage;
    private boolean active;
    private Date receiptDate;
    private String receiptDateFormatted;

    public static class Signed {
        public static final int NOT_SIGNED = 0;
        public static final int ACCEPTED = 1;
        public static final int REJECTED = 2;
        public static final int CANCELED = 3;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getSigned() {
        return signed;
    }

    public void setSigned(int signed) {
        this.signed = signed;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public String getSignDateFormatted() {
        return signDateFormatted;
    }

    public void setSignDateFormatted(String signDateFormatted) {
        this.signDateFormatted = signDateFormatted;
    }

    public int getSignUserId() {
        return signUserId;
    }

    public void setSignUserId(int signUserId) {
        this.signUserId = signUserId;
    }

    public String getSignUserName() {
        return signUserName;
    }

    public void setSignUserName(String signUserName) {
        this.signUserName = signUserName;
    }

    public String getSignMessage() {
        return signMessage;
    }

    public void setSignMessage(String signMessage) {
        this.signMessage = signMessage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getReceiptDateFormatted() {
        return receiptDateFormatted;
    }

    public void setReceiptDateFormatted(String receiptDateFormatted) {
        this.receiptDateFormatted = receiptDateFormatted;
    }
}
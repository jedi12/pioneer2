package ru.pioneersystem.pioneer2.model;

import java.util.Date;

public class SearchFilter {
    private boolean byId;
    private boolean byName;
    private boolean byContent;
    private boolean byTemplate;
    private boolean byStatus;
    private boolean byOwner;

    private Date fromDate;
    private Date toDate;
    private int id;
    private String name;
    private String content;
    private int templateId;
    private int statusId;
    private int ownerId;

    public boolean isById() {
        return byId;
    }

    public void setById(boolean byId) {
        this.byId = byId;
    }

    public boolean isByName() {
        return byName;
    }

    public void setByName(boolean byName) {
        this.byName = byName;
    }

    public boolean isByContent() {
        return byContent;
    }

    public void setByContent(boolean byContent) {
        this.byContent = byContent;
    }

    public boolean isByTemplate() {
        return byTemplate;
    }

    public void setByTemplate(boolean byTemplate) {
        this.byTemplate = byTemplate;
    }

    public boolean isByStatus() {
        return byStatus;
    }

    public void setByStatus(boolean byStatus) {
        this.byStatus = byStatus;
    }

    public boolean isByOwner() {
        return byOwner;
    }

    public void setByOwner(boolean byOwner) {
        this.byOwner = byOwner;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
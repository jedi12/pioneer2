package ru.pioneersystem.pioneer2.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Document {
    private int id;
    private String name;
    private int statusId;
    private String statusName;
    private Date changeDate;
    private int templateId;
    private int changeUserId;
    private int documentGroupId;
    private String documentGroupName;
    private int partId;
    private int routeId;

    private boolean createFlag;
    private boolean newPart;
    private int newRouteId;
    private boolean newRoute;
    private boolean editMode;
    private String signerComment;

    private List<Field> fields;
    private List<Condition> conditions;
    private List<RoutePoint> routePoints;

    public static class RoutePoint {
        private int stage;
        private int groupId;
        private int roleId;
        private int signed;
        private Date signDate;
        private int signUserId;
        private String signMessage;
        private boolean active;
        private Date receiptDate;

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

        public int getSignUserId() {
            return signUserId;
        }

        public void setSignUserId(int signUserId) {
            this.signUserId = signUserId;
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
    }

    public static class Field {
        private String name;
        private int num;
        private int typeId;
        private String typeName;
        private Integer choiceListId;
        private String choiceListName;
        private List<String> choiceListValues;
        private String valueTextField;
        private String valueChoiceList;
        private Date valueCalendar;
        private Boolean valueCheckBox;
        private String valueTextArea;
        private Integer fileId;
        private String fileName;
        private File file;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
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

        public Integer getChoiceListId() {
            return choiceListId;
        }

        public void setChoiceListId(Integer choiceListId) {
            this.choiceListId = choiceListId;
        }

        public String getChoiceListName() {
            return choiceListName;
        }

        public void setChoiceListName(String choiceListName) {
            this.choiceListName = choiceListName;
        }

        public List<String> getChoiceListValues() {
            return choiceListValues;
        }

        public void setChoiceListValues(List<String> choiceListValues) {
            this.choiceListValues = choiceListValues;
        }

        public String getValueTextField() {
            return valueTextField;
        }

        public void setValueTextField(String valueTextField) {
            this.valueTextField = valueTextField;
        }

        public String getValueChoiceList() {
            return valueChoiceList;
        }

        public void setValueChoiceList(String valueChoiceList) {
            this.valueChoiceList = valueChoiceList;
        }

        public Date getValueCalendar() {
            return valueCalendar;
        }

        public void setValueCalendar(Date valueCalendar) {
            this.valueCalendar = valueCalendar;
        }

        public Boolean getValueCheckBox() {
            return valueCheckBox;
        }

        public void setValueCheckBox(Boolean valueCheckBox) {
            this.valueCheckBox = valueCheckBox;
        }

        public String getValueTextArea() {
            return valueTextArea;
        }

        public void setValueTextArea(String valueTextArea) {
            this.valueTextArea = valueTextArea;
        }

        public Integer getFileId() {
            return fileId;
        }

        public void setFileId(Integer fileId) {
            this.fileId = fileId;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }
    }

    public static class Condition {
        private int condNum;
        private int fieldNum;
        private String fieldName;
        private String cond;
        private String value;
        private int routeId;
        private String routeName;

        public static class Operation {
            public static final String EQ = "Равно";
            public static final String NE = "Не равно";
            public static final String LT = "Меньше";
            public static final String GT = "Больше";
            public static final String LE = "Меньше или равно";
            public static final String GE = "Больше или равно";
            public static final String CNT = "Содержит";
            public static final List<String> LIST = Arrays.asList(EQ, NE, LT, GT, LE, GE, CNT);
        }

        public int getCondNum() {
            return condNum;
        }

        public void setCondNum(int condNum) {
            this.condNum = condNum;
        }

        public int getFieldNum() {
            return fieldNum;
        }

        public void setFieldNum(int fieldNum) {
            this.fieldNum = fieldNum;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getCond() {
            return cond;
        }

        public void setCond(String cond) {
            this.cond = cond;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getRouteId() {
            return routeId;
        }

        public void setRouteId(int routeId) {
            this.routeId = routeId;
        }

        public String getRouteName() {
            return routeName;
        }

        public void setRouteName(String routeName) {
            this.routeName = routeName;
        }
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

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public int getChangeUserId() {
        return changeUserId;
    }

    public void setChangeUserId(int changeUserId) {
        this.changeUserId = changeUserId;
    }

    public int getDocumentGroupId() {
        return documentGroupId;
    }

    public void setDocumentGroupId(int documentGroupId) {
        this.documentGroupId = documentGroupId;
    }

    public String getDocumentGroupName() {
        return documentGroupName;
    }

    public void setDocumentGroupName(String documentGroupName) {
        this.documentGroupName = documentGroupName;
    }

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public void setCreateFlag(boolean createFlag) {
        this.createFlag = createFlag;
    }

    public boolean isNewPart() {
        return newPart;
    }

    public void setNewPart(boolean newPart) {
        this.newPart = newPart;
    }

    public int getNewRouteId() {
        return newRouteId;
    }

    public void setNewRouteId(int newRouteId) {
        this.newRouteId = newRouteId;
    }

    public boolean isNewRoute() {
        return newRoute;
    }

    public void setNewRoute(boolean newRoute) {
        this.newRoute = newRoute;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getSignerComment() {
        return signerComment;
    }

    public void setSignerComment(String signerComment) {
        this.signerComment = signerComment;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(List<RoutePoint> routePoints) {
        this.routePoints = routePoints;
    }
}
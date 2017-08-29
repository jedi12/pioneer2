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
    private String changeDateFormatted;
    private int templateId;
    private int changeUserId;
    private int documentGroupId;
    private String documentGroupName;
    private int partId;
    private int routeId;
    private Date createDate;
    private String createDateFormatted;
    private int createUserId;

    private boolean createFlag;
    private boolean newPart;
    private int newRouteId;
    private boolean newRoute;
    private boolean editMode;
    private String signerComment;

    private ViewElements elems;

    private List<Field> fields;
    private List<Condition> conditions;

    public static class ViewElements {
        private boolean elEditHeader;
        private boolean elEditDoc;
        private boolean elPublish;
        private boolean elChangeRoute;
        private boolean elDocOwner;
        private boolean elSignerComment;
        private boolean disableBtn;
        private boolean btnSave;
        private boolean btnSaveAndSend;
        private boolean btnRecall;
        private boolean btnCopy;
        private boolean btnPublish;
        private boolean btnPublishCancel;
        private boolean btnDelete;
        private boolean btnAccept;
        private boolean btnReject;

        public boolean isElEditHeader() {
            return elEditHeader;
        }

        public void setElEditHeader(boolean elEditHeader) {
            this.elEditHeader = elEditHeader;
        }

        public boolean isElEditDoc() {
            return elEditDoc;
        }

        public void setElEditDoc(boolean elEditDoc) {
            this.elEditDoc = elEditDoc;
        }

        public boolean isElPublish() {
            return elPublish;
        }

        public void setElPublish(boolean elPublish) {
            this.elPublish = elPublish;
        }

        public boolean isElChangeRoute() {
            return elChangeRoute;
        }

        public void setElChangeRoute(boolean elChangeRoute) {
            this.elChangeRoute = elChangeRoute;
        }

        public boolean isElDocOwner() {
            return elDocOwner;
        }

        public void setElDocOwner(boolean elDocOwner) {
            this.elDocOwner = elDocOwner;
        }

        public boolean isElSignerComment() {
            return elSignerComment;
        }

        public void setElSignerComment(boolean elSignerComment) {
            this.elSignerComment = elSignerComment;
        }

        public boolean isDisableBtn() {
            return disableBtn;
        }

        public void setDisableBtn(boolean disableBtn) {
            this.disableBtn = disableBtn;
        }

        public boolean isBtnSave() {
            return btnSave;
        }

        public void setBtnSave(boolean btnSave) {
            this.btnSave = btnSave;
        }

        public boolean isBtnSaveAndSend() {
            return btnSaveAndSend;
        }

        public void setBtnSaveAndSend(boolean btnSaveAndSend) {
            this.btnSaveAndSend = btnSaveAndSend;
        }

        public boolean isBtnRecall() {
            return btnRecall;
        }

        public void setBtnRecall(boolean btnRecall) {
            this.btnRecall = btnRecall;
        }

        public boolean isBtnCopy() {
            return btnCopy;
        }

        public void setBtnCopy(boolean btnCopy) {
            this.btnCopy = btnCopy;
        }

        public boolean isBtnPublish() {
            return btnPublish;
        }

        public void setBtnPublish(boolean btnPublish) {
            this.btnPublish = btnPublish;
        }

        public boolean isBtnPublishCancel() {
            return btnPublishCancel;
        }

        public void setBtnPublishCancel(boolean btnPublishCancel) {
            this.btnPublishCancel = btnPublishCancel;
        }

        public boolean isBtnDelete() {
            return btnDelete;
        }

        public void setBtnDelete(boolean btnDelete) {
            this.btnDelete = btnDelete;
        }

        public boolean isBtnAccept() {
            return btnAccept;
        }

        public void setBtnAccept(boolean btnAccept) {
            this.btnAccept = btnAccept;
        }

        public boolean isBtnReject() {
            return btnReject;
        }

        public void setBtnReject(boolean btnReject) {
            this.btnReject = btnReject;
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

    public String getChangeDateFormatted() {
        return changeDateFormatted;
    }

    public void setChangeDateFormatted(String changeDateFormatted) {
        this.changeDateFormatted = changeDateFormatted;
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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreateDateFormatted() {
        return createDateFormatted;
    }

    public void setCreateDateFormatted(String createDateFormatted) {
        this.createDateFormatted = createDateFormatted;
    }

    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
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

    public ViewElements getElems() {
        return elems;
    }

    public void setElems(ViewElements elems) {
        this.elems = elems;
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
}
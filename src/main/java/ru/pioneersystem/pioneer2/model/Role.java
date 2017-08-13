package ru.pioneersystem.pioneer2.model;

public class Role {
    private int id;
    private String name;
    private int state;
    private int type;
    private String acceptButton;
    private String rejectButton;
    private boolean canRouteChange;
    private boolean canEdit;
    private boolean canComment;
    private String statusName;
    private String menuName;


    private boolean createFlag;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
    }

    public static class Type {
        public static final int SUPER = 1;
        public static final int ADMIN = 2;
        public static final int USER = 3;
        public static final int REZ1 = 4;
        public static final int PUBLIC = 5;
        public static final int ROUTE_CHANGE = 6;
        public static final int EDIT = 7;
        public static final int REZ2 = 8;
        public static final int CREATE = 9;
        public static final int ON_ROUTE = 10;
    }

    public static class Id {
        public static final int SUPER = 1;
        public static final int ADMIN = 2;
        public static final int USER = 3;
        public static final int REZ1 = 4;
        public static final int PUBLIC = 5;
        public static final int ROUTE_CHANGE = 6;
        public static final int EDIT = 7;
        public static final int REZ2 = 8;
        public static final int CREATE = 9;
        public static final int ON_COORDINATION = 10;
        public static final int ON_EXECUTION = 11;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAcceptButton() {
        return acceptButton;
    }

    public void setAcceptButton(String acceptButton) {
        this.acceptButton = acceptButton;
    }

    public String getRejectButton() {
        return rejectButton;
    }

    public void setRejectButton(String rejectButton) {
        this.rejectButton = rejectButton;
    }

    public boolean isCanRouteChange() {
        return canRouteChange;
    }

    public void setCanRouteChange(boolean canRouteChange) {
        this.canRouteChange = canRouteChange;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanComment() {
        return canComment;
    }

    public void setCanComment(boolean canComment) {
        this.canComment = canComment;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public void setCreateFlag(boolean createFlag) {
        this.createFlag = createFlag;
    }
}
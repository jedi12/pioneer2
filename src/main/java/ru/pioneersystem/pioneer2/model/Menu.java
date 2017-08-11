package ru.pioneersystem.pioneer2.model;

import java.util.List;

public class Menu {
    private int id;
    private String name;
    private String page;
    private int num;
    private int parent;
    private int roleId;
    private int state;
    private List<Menu> subMenu;

    private boolean createFlag;

    public static class Page {
        public static final String PUBLIC_DOC = "docPublic.xhtml";
        public static final String CREATE_DOC = "docCreate.xhtml";
        public static final String SEARCH_DOC = "docSearch.xhtml";
        public static final String MY_DOC = "docMy.xhtml";
        public static final String ON_ROUTE_DOC = "docOnRoute.xhtml";
    }

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
        public static final int CREATED_BY_ROLE = 3;
    }

    public static class Id {
        public static final int PUB_DOCS = 1;
        public static final int CREATE_DOCS = 2;
        public static final int SEARCH_DOCS = 3;
        public static final int SETTINGS = 4;
        public static final int JOURNALS = 5;
        public static final int USERS = 6;
        public static final int ROLES = 7;
        public static final int MY_DOCS = 8;
        public static final int GROUPS = 9;
        public static final int ROUTES = 10;
        public static final int PARTS = 11;
        public static final int LISTS = 12;
        public static final int TEMPLATES = 13;
        public static final int EVENTS = 14;
        public static final int NOTICES = 15;
        public static final int MENUS = 16;
        public static final int ON_ROUTE_CONFIRM = 17;
        public static final int ON_ROUTE_EXEC = 18;
        public static final int COMPANY = 19;
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

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<Menu> getSubMenu() {
        return subMenu;
    }

    public void setSubMenu(List<Menu> subMenu) {
        this.subMenu = subMenu;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public void setCreateFlag(boolean createFlag) {
        this.createFlag = createFlag;
    }
}
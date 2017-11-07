package ru.pioneersystem.pioneer2.model;

import java.util.List;

public class Part {
    private int id;
    private String name;
    private int state;
    private int parent;
    private int treeLevel;
    private int ownerGroup;
    private int type;
    private int companyId;
    private String companyName;
    private List<LinkGroup> linkGroups;

    private boolean createFlag;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
    }

    public static class Type {
        public static final int FOR_TEMPLATES = 1;
        public static final int FOR_DOCUMENTS = 2;
    }

    public static class LinkGroup {
        private int groupId;
        private String groupName;

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

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getTreeLevel() {
        return treeLevel;
    }

    public void setTreeLevel(int treeLevel) {
        this.treeLevel = treeLevel;
    }

    public int getOwnerGroup() {
        return ownerGroup;
    }

    public void setOwnerGroup(int ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<LinkGroup> getLinkGroups() {
        return linkGroups;
    }

    public void setLinkGroups(List<LinkGroup> linkGroups) {
        this.linkGroups = linkGroups;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public void setCreateFlag(boolean createFlag) {
        this.createFlag = createFlag;
    }
}
package ru.pioneersystem.pioneer2.model;

import java.util.List;

public class Part {
    private int id;
    private String name;
    private int state;
    private int parent;
    private int treeLevel;
    private int ownerGroup;
    private List<Group> groups;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
    }

    public static class Type {
        public static final int FOR_TEMPLATES = 1;
        public static final int FOR_DOCUMENTS = 2;
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

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
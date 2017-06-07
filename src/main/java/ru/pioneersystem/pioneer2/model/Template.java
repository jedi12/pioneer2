package ru.pioneersystem.pioneer2.model;

import java.util.LinkedList;
import java.util.List;

public class Template {
    private int id;
    private String name;
    private int routeId;
    private String routeName;
    private int partId;
    private String partName;
    private int state;
    private LinkedList<Document.Field> fields;
    private List<Document.Condition> conditions;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
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

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public LinkedList<Document.Field> getFields() {
        return fields;
    }

    public void setFields(LinkedList<Document.Field> fields) {
        this.fields = fields;
    }

    public List<Document.Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Document.Condition> conditions) {
        this.conditions = conditions;
    }
}
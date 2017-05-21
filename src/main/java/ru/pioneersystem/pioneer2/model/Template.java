package ru.pioneersystem.pioneer2.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Template {
    private int id;
    private String name;
    private int routeId;
    private Route route;
    private int partId;
    private Part part;
    private int state;
    private LinkedList<Field> fields;
    private List<Condition> conditions;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
    }

    public static class Field {
        private String name;
        private int num;
        private int typeId;
        private String typeName;
        private int choiceListId;
        private String choiceListName;

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

        public int getChoiceListId() {
            return choiceListId;
        }

        public void setChoiceListId(int choiceListId) {
            this.choiceListId = choiceListId;
        }

        public String getChoiceListName() {
            return choiceListName;
        }

        public void setChoiceListName(String choiceListName) {
            this.choiceListName = choiceListName;
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

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public LinkedList<Field> getFields() {
        return fields;
    }

    public void setFields(LinkedList<Field> fields) {
        this.fields = fields;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
}
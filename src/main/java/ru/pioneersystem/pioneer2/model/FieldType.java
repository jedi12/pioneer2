package ru.pioneersystem.pioneer2.model;

public class FieldType {
    private int id;
    private String name;
    private int typeId;
    private String typeName;

    public static class Type {
        public static final int INPUT = 1;
        public static final int DECORATE = 2;
    }

    public static class Id {
        public static final int TEXT_STRING = 5;
        public static final int CHOICE_LIST = 6;
        public static final int CALENDAR = 7;
        public static final int CHECKBOX = 8;
        public static final int TEXT_AREA = 9;
        public static final int FILE = 10;
        public static final int LINE = 20;
        public static final int EMPTY_STRING = 21;
        public static final int TITLE = 22;
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
}
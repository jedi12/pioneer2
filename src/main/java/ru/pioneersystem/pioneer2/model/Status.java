package ru.pioneersystem.pioneer2.model;

public class Status {
    private int id;
    private String name;
    private int state;
    private int type;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
    }

    public static class Type {
        public static final int FINAL = 0;
        public static final int INITIAL = 1;
        public static final int MEDIUM = 2;
    }

    public static class Id {
        public static final int DELETED = 0;
        public static final int CANCELED = 6;
        public static final int COMPLETED = 7;
        public static final int PUBLISHED = 8;
        public static final int CREATED = 9;
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
}
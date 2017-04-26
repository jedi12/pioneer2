package ru.pioneersystem.pioneer2.model;

import java.util.List;

public class ChoiceList {
    private int id;
    private String name;
    private List<String> values;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
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

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
package ru.pioneersystem.pioneer2.model;

public class Role {
    private int id;
    private String name;
    private int state;
    private int type;
    private boolean create;
    private boolean publish;
    private boolean comment;
    private boolean edit;
    private boolean changeRoute;

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

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isComment() {
        return comment;
    }

    public void setComment(boolean comment) {
        this.comment = comment;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isChangeRoute() {
        return changeRoute;
    }

    public void setChangeRoute(boolean changeRoute) {
        this.changeRoute = changeRoute;
    }
}
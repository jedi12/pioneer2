package ru.pioneersystem.pioneer2.model;

public class User {
    private int id;
    private String name;
    private String login;
    private String position;
    private String email;
    private String phone;
    private String comment;
    private int state;
    private String stateName;
    private int companyId;
    private Company company;
    private boolean noticeDocIncoming;
    private boolean noticeStatusChanged;

    private boolean createFlag;

    public static class State {
        public static final int LOCKED = 0;
        public static final int ACTIVE = 1;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public void setCreateFlag(boolean createFlag) {
        this.createFlag = createFlag;
    }

    public boolean isNoticeDocIncoming() {
        return noticeDocIncoming;
    }

    public void setNoticeDocIncoming(boolean noticeDocIncoming) {
        this.noticeDocIncoming = noticeDocIncoming;
    }

    public boolean isNoticeStatusChanged() {
        return noticeStatusChanged;
    }

    public void setNoticeStatusChanged(boolean noticeStatusChanged) {
        this.noticeStatusChanged = noticeStatusChanged;
    }
}
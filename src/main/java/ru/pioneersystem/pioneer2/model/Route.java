package ru.pioneersystem.pioneer2.model;

import java.util.List;

public class Route {
    private int id;
    private String name;
    private int state;
    private int companyId;
    private String companyName;
    private List<LinkGroup> groups;
    private List<Point> points;

    private boolean createFlag;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
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

    public static class Point {
        private int stage;
        private int groupId;
        private String groupName;
        private int roleId;
        private String roleName;

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

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

        public int getRoleId() {
            return roleId;
        }

        public void setRoleId(int roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
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

    public List<LinkGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<LinkGroup> groups) {
        this.groups = groups;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public void setCreateFlag(boolean createFlag) {
        this.createFlag = createFlag;
    }
}
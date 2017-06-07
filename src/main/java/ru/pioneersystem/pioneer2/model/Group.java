package ru.pioneersystem.pioneer2.model;

import java.util.List;

public class Group {
    private int id;
    private String name;
    private int state;
    private int roleId;
    private String roleName;
    private List<LinkUser> linkUsers;

    public static class State {
        public static final int DELETED = 0;
        public static final int EXISTS = 1;
        public static final int SYSTEM = 2;
    }

    public static class ActorType {
        public static final int SPECTATOR = 0;
        public static final int PARTICIPANT = 1;
    }

    public static class LinkUser {
        private int userId;
        private String userName;
        private boolean isParticipant;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public boolean isParticipant() {
            return isParticipant;
        }

        public void setParticipant(boolean participant) {
            isParticipant = participant;
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

    public List<LinkUser> getLinkUsers() {
        return linkUsers;
    }

    public void setLinkUsers(List<LinkUser> linkUsers) {
        this.linkUsers = linkUsers;
    }
}
package ru.pioneersystem.pioneer2.model;

import java.util.List;

public class Group {
    private int id;
    private String name;
    private int state;
    private int roleId;
    private Role role;
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
        private User user;
        private int groupId;
        private int userId;
        private boolean isParticipant;

        public User getUser() {
            return user;
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<LinkUser> getLinkUsers() {
        return linkUsers;
    }

    public void setLinkUsers(List<LinkUser> linkUsers) {
        this.linkUsers = linkUsers;
    }
}
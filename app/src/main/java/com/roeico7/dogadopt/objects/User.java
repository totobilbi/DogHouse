package com.roeico7.dogadopt.objects;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private String userID;
    private String username;
    private String phone;
    private String email;
    private String password;
    private String avatar;
    private String onlineStatus;
    private String typingTo;
    private HashMap<String, String> profileInfo = new HashMap<>();
    private ArrayList<Dog> dogList = new ArrayList<>();

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public HashMap<String, String> getProfileInfo() {
        return profileInfo;
    }

    public void setProfileInfo(HashMap<String, String> profileInfo) {
        this.profileInfo = profileInfo;
    }

    public User(String userID, String username, String phone, String email, String password, String avatar, String onlineStatus, String typingTo, HashMap<String, String> profileInfo, ArrayList<Dog> dogList) {
        this.userID = userID;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.profileInfo = profileInfo;
        this.dogList = dogList;
    }



    public User() {

    }

    public User(String userID, String username, String phone, String email, String password, ArrayList<Dog> dogList) {
        this.userID = userID;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.dogList = dogList;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Dog> getDogList() {
        return dogList;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDogList(ArrayList<Dog> dogList) {
        this.dogList = dogList;
    }



    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", dogList=" + dogList +
                '}';
    }
}

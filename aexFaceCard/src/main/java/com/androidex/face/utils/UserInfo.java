package com.androidex.face.utils;

/**
 * Created by cts on 17/4/7.
 */

public class UserInfo {
    public String username;
    public String facepath;

    public UserInfo() {
    }

    public UserInfo (String username, String facepath){
        this.username = username;
        this.facepath = facepath;

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getFacepath() {
        return facepath;
    }

    public void setFacepath(String facepath) {
        this.facepath = facepath;
    }





}

package com.hcu.beaconandroid.model;

public class Response {

    private Beacon[] beacons;
    private String message;
    private String username;
    private String fullname;
    private String token;
    private String detail;
    private String email;

    public String getFullname() {
        return fullname;
    }

    public String getToken() {
        return token;
    }

    public String getDetail() {
        return detail;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public Beacon[] getBeacons() {
        return beacons;
    }

    public void setBeacons(Beacon[] beacons) {
        this.beacons = beacons;
    }
}

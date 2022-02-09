package com.example.ialerto.ui.alerts;

public class AlertsInfo {
    String id,name,contact_number,address,type,date;
    double longitude,latitude;


    public AlertsInfo(String id, String name, String address, String type, String date, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.type = type;
        this.date = date;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public AlertsInfo(String id, String name, String contact_number, String address, String type, String date, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.contact_number = contact_number;
        this.address = address;
        this.type = type;
        this.date = date;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}

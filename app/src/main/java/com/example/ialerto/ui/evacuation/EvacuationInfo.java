package com.example.ialerto.ui.evacuation;

import org.json.JSONArray;

public class EvacuationInfo {
    String id,name,address,capacity,status,created_at,updated_at,date,is_avail;
    JSONArray barangays;

    public EvacuationInfo(String id, String name, String address, String capacity, String status, String date, String is_avail, JSONArray barangays) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.status = status;
        this.date = date;
        this.is_avail = is_avail;
        this.barangays = barangays;
    }

    public EvacuationInfo(String name, String address, String capacity) {
        this.name = name;
        this.address = address;
        this.capacity = capacity;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIs_avail() {
        return is_avail;
    }

    public void setIs_avail(String is_avail) {
        this.is_avail = is_avail;
    }

    public JSONArray getBarangays() {
        return barangays;
    }

    public void setBarangays(JSONArray barangays) {
        this.barangays = barangays;
    }
}

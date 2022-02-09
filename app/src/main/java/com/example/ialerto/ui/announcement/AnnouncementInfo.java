package com.example.ialerto.ui.announcement;

import org.json.JSONArray;
import org.json.JSONObject;

public class AnnouncementInfo {
    String title,detailes,created_at;
    JSONArray evacuations;

    public AnnouncementInfo(String title, String detailes, String created_at, JSONArray evacuations) {
        this.title = title;
        this.detailes = detailes;
        this.created_at = created_at;
        this.evacuations = evacuations;
    }

    public AnnouncementInfo(String title, String detailes, String created_at) {
        this.title = title;
        this.detailes = detailes;
        this.created_at = created_at;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailes() {
        return detailes;
    }

    public void setDetailes(String detailes) {
        this.detailes = detailes;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public JSONArray getEvacuations() {
        return evacuations;
    }

    public void setEvacuations(JSONArray evacuations) {
        this.evacuations = evacuations;
    }
}

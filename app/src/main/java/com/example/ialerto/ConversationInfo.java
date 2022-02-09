package com.example.ialerto;

public class ConversationInfo {
    String id,user_id,alert_id,name,role,message,image,date;
    int has_image;

    public ConversationInfo(String id, String user_id, String alert_id, String name, String role, String message, String date) {
        this.id = id;
        this.user_id = user_id;
        this.alert_id = alert_id;
        this.name = name;
        this.role = role;
        this.message = message;
        this.date = date;
    }

    public ConversationInfo(String id, String user_id, String alert_id, String name, String role, String message, String image, String date, int has_image) {
        this.id = id;
        this.user_id = user_id;
        this.alert_id = alert_id;
        this.name = name;
        this.role = role;
        this.message = message;
        this.image = image;
        this.date = date;
        this.has_image = has_image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAlert_id() {
        return alert_id;
    }

    public void setAlert_id(String alert_id) {
        this.alert_id = alert_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getHas_image() {
        return has_image;
    }

    public void setHas_image(int has_image) {
        this.has_image = has_image;
    }
}

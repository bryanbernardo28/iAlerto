package com.example.ialerto.ui.reporthistory;

import org.json.JSONObject;

public class ReportHistoryInfos {
    String id;
    String alert_user;
    String report_type,report_address,time_report,time_deploy;
    int status;
    double longitude,latitude;


    public ReportHistoryInfos(String id, String alert_user, String report_type, String report_address, String time_report, String time_deploy, int status, double longitude, double latitude) {
        this.id = id;
        this.alert_user = alert_user;
        this.report_type = report_type;
        this.report_address = report_address;
        this.time_report = time_report;
        this.time_deploy = time_deploy;
        this.status = status;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlert_user() {
        return alert_user;
    }

    public void setAlert_user(String alert_user) {
        this.alert_user = alert_user;
    }

    public String getReport_type() {
        return report_type;
    }

    public void setReport_type(String report_type) {
        this.report_type = report_type;
    }

    public String getReport_address() {
        return report_address;
    }

    public void setReport_address(String report_address) {
        this.report_address = report_address;
    }

    public String getTime_report() {
        return time_report;
    }

    public void setTime_report(String time_report) {
        this.time_report = time_report;
    }

    public String getTime_deploy() {
        return time_deploy;
    }

    public void setTime_deploy(String time_deploy) {
        this.time_deploy = time_deploy;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

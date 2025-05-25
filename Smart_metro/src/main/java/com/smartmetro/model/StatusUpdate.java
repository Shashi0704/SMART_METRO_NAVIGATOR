package com.smartmetro.model;

public class StatusUpdate {
    private int metroId;
    private int stationId;
    private int delayMinutes;
    private String crowdLevel;
    private String station;
    private String message;

    public StatusUpdate() {}

    // Getters and Setters
    public int getMetroId() {
        return metroId;
    }

    public void setMetroId(int metroId) {
        this.metroId = metroId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(int delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public String getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(String crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 
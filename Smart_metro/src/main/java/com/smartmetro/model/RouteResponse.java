package com.smartmetro.model;

import java.util.List;

public class RouteResponse {
    private List<Station> stations;
    private float distance;
    private int estimatedTime;
    private List<MetroInfo> metros;
    private List<StatusUpdate> statusUpdates;

    public static class MetroInfo {
        private int id;
        private int time;
        private int delay;
        private boolean crowded;
        private boolean recommended;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public boolean isCrowded() {
            return crowded;
        }

        public void setCrowded(boolean crowded) {
            this.crowded = crowded;
        }

        public boolean isRecommended() {
            return recommended;
        }

        public void setRecommended(boolean recommended) {
            this.recommended = recommended;
        }
    }

    // Getters and Setters
    public List<Station> getStations() { return stations; }
    public void setStations(List<Station> stations) { this.stations = stations; }

    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }

    public int getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(int estimatedTime) { this.estimatedTime = estimatedTime; }

    public List<MetroInfo> getMetros() { return metros; }
    public void setMetros(List<MetroInfo> metros) { this.metros = metros; }

    public List<StatusUpdate> getStatusUpdates() { return statusUpdates; }
    public void setStatusUpdates(List<StatusUpdate> statusUpdates) { this.statusUpdates = statusUpdates; }
} 
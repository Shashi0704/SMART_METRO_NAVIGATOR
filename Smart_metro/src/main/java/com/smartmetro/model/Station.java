package com.smartmetro.model;

import java.util.ArrayList;
import java.util.List;

public class Station {
    private String name;
    private int code;
    private String color;
    private List<String> arrivalTime;

    public Station() {
        arrivalTime = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(List<String> arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
} 
package com.example.rpmsim.entity;

import java.io.Serializable;

public class Shield implements Serializable {
    private String nameShield;
    private double thickness;
    private int id;

    public Shield(String nameShield, double thickness, int id) {
        this.nameShield = nameShield;
        this.thickness = thickness;
        this.id = id;
    }

    public String getNameShield() {
        return nameShield;
    }

    public int getId() {
        return id;
    }

    public double getThickness() {
        return thickness;
    }
}

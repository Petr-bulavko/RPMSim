package com.example.rpmsim.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Detector implements Serializable {
    private String nameDetector;
    private ArrayList<Double> sensitivity;
    private double x;
    private double y;
    private double z;
    private double geometricalSizes;
    private double background;
    private int positionInSpinner;

    public Detector() {
    }

    public Detector(String nameDetector, ArrayList<Double> sensitivity, double x, double y, double z, double geometricalSizes, double background, int positionInSpinner) {
        this.nameDetector = nameDetector;
        this.sensitivity = sensitivity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.geometricalSizes = geometricalSizes;
        this.background = background;
        this.positionInSpinner = positionInSpinner;
    }

    public String getNameDetector() {
        return nameDetector;
    }

    public void setNameDetector(String nameDetector) {
        this.nameDetector = nameDetector;
    }

    public ArrayList<Double> getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(ArrayList<Double> sensitivity) {
        this.sensitivity = sensitivity;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getGeometricalSizes() {
        return geometricalSizes;
    }

    public void setGeometricalSizes(double geometricalSizes) {
        this.geometricalSizes = geometricalSizes;
    }

    public double getBackground() {
        return background;
    }

    public void setBackground(double background) {
        this.background = background;
    }

    public int getPositionInSpinner() {
        return positionInSpinner;
    }

    public void setPositionInSpinner(int positionInSpinner) {
        this.positionInSpinner = positionInSpinner;
    }
}

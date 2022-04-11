package com.example.rpmsim.entity;

import java.io.Serializable;

public class Source implements Serializable {
    private String nameSource;
    private String dimension;
    private double coefficient;
    private double activitySource;
    private double coordinateSourceX;
    private double coordinateSourceY;
    private double coordinateSourceZ;
    private int positionInSpinner;
    private String dimension_factor;

    public Source() {
    }

    public Source(String nameSource, String dimension, double coefficient,
                  double activitySource, double coordinateSourceX, double coordinateSourceY,
                  double coordinateSourceZ, int positionInSpinner, String dimension_factor) {
        this.nameSource = nameSource;
        this.dimension = dimension;
        this.coefficient = coefficient;
        this.activitySource = activitySource;
        this.coordinateSourceX = coordinateSourceX;
        this.coordinateSourceY = coordinateSourceY;
        this.coordinateSourceZ = coordinateSourceZ;
        this.positionInSpinner = positionInSpinner;
        this.dimension_factor = dimension_factor;
    }

    public String getNameSource() {
        return nameSource;
    }

    public void setNameSource(String nameSource) {
        this.nameSource = nameSource;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public double getActivitySource() {
        return activitySource;
    }

    public void setActivitySource(double activitySource) {
        this.activitySource = activitySource;
    }

    public double getCoordinateSourceX() {
        return coordinateSourceX;
    }

    public void setCoordinateSourceX(double coordinateSourceX) {
        this.coordinateSourceX = coordinateSourceX;
    }

    public double getCoordinateSourceY() {
        return coordinateSourceY;
    }

    public void setCoordinateSourceY(double coordinateSourceY) {
        this.coordinateSourceY = coordinateSourceY;
    }

    public double getCoordinateSourceZ() {
        return coordinateSourceZ;
    }

    public void setCoordinateSourceZ(double coordinateSourceZ) {
        this.coordinateSourceZ = coordinateSourceZ;
    }

    public int getPositionInSpinner() {
        return positionInSpinner;
    }

    public void setPositionInSpinner(int positionInSpinner) {
        this.positionInSpinner = positionInSpinner;
    }

    public String getDimension_factor() {
        return dimension_factor;
    }

    public void setDimension_factor(String dimension_factor) {
        this.dimension_factor = dimension_factor;
    }
}

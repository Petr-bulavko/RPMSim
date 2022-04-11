package com.example.rpmsim.entity;

public class CountRate {
    private double countRate;
    private boolean bckg;

    public CountRate() {
    }

    public CountRate(double countRate, boolean bckg) {
        this.countRate = countRate;
        this.bckg = bckg;
    }

    public double getCountRate() {
        return countRate;
    }

    public void setCountRate(double countRate) {
        this.countRate = countRate;
    }

    public boolean isBckg() {
        return bckg;
    }

    public void setBckg(boolean bckg) {
        this.bckg = bckg;
    }
}

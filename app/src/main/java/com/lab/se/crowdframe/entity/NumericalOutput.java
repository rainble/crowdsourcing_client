package com.lab.se.crowdframe.entity;

/**
 * Created by lwh on 2017/4/7.
 */

public class NumericalOutput extends OutputParent {
    private int interval;
    private double upBound;
    private double lowBound;
    private int numberAggregate;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public double getUpBound() {
        return upBound;
    }

    public void setUpBound(double upBound) {
        this.upBound = upBound;
    }

    public double getLowBound() {
        return lowBound;
    }

    public void setLowBound(double lowBound) {
        this.lowBound = lowBound;
    }

    public int getNumberAggregate() {
        return numberAggregate;
    }

    public void setNumberAggregate(int numberAggregate) {
        this.numberAggregate = numberAggregate;
    }
}

package com.lab.se.crowdframe.entity;

import java.io.Serializable;

/**
 * Created by lwh on 2017/4/7.
 */

public class OutputParent implements Serializable{
    private int outputType;
    private String outputDesc;
    private String outputValue;

    public int getOutputType() {
        return outputType;
    }

    public void setOutputType(int outputType) {
        this.outputType = outputType;
    }

    public String getOutputDesc() {
        return outputDesc;
    }

    public void setOutputDesc(String outputDesc) {
        this.outputDesc = outputDesc;
    }

    public String getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(String outputValue) {
        this.outputValue = outputValue;
    }
}

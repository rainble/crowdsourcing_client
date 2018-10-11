package com.lab.se.crowdframe.entity;

import java.io.Serializable;
import java.util.List;


public class ExtraValue implements Serializable{

    public void setExtraValue(List<String> extraValue) {
        this.extraValue = extraValue;
    }

    public List<String> getExtraValue() {

        return extraValue;
    }

    private List<String> extraValue;


}

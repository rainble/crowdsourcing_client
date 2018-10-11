package com.lab.se.crowdframe.entity;

import java.util.List;

/**
 * Created by lwh on 2017/4/7.
 */

public class EnumOutput extends OutputParent {
    List<String> entries;
    int enumAggregate;

    public List<String> getEntries() {
        return entries;
    }

    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

    public int getEnumAggregate() {
        return enumAggregate;
    }

    public void setEnumAggregate(int enumAggregate) {
        this.enumAggregate = enumAggregate;
    }
}

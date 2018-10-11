package com.lab.se.crowdframe.entity;

import com.baidu.mapapi.search.core.PoiInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ExtraMap implements Serializable {
    private HashMap<String,String> extraMap;

    public HashMap<String, String> getExtraMap() {
        return extraMap;
    }

    public void setExtraMap(HashMap<String, String> extraMap) {
        this.extraMap = extraMap;
    }
}
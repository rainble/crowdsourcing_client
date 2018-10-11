package com.lab.se.crowdframe.entity;

import com.baidu.mapapi.search.core.PoiInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


public class ExtraName implements Serializable{

   private List<String> extraName;

    public void setExtraName(List<String> extraName) {
        this.extraName = extraName;
    }

    public List<String> getExtraName() {

        return extraName;
    }
}

package com.lab.se.crowdframe.entity;

import java.util.List;

/**
 * Created by lwh on 2017/3/29.
 */

public class TemplateInfo {
    private String name;
    private String description;
    List<StageInfo> stages;

    public TemplateInfo(){

    }

    public TemplateInfo(String name, String description, List<StageInfo> stages) {
        this.name = name;
        this.description = description;
        this.stages = stages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StageInfo> getStages() {
        return stages;
    }

    public void setStages(List<StageInfo> stages) {
        this.stages = stages;
    }
}

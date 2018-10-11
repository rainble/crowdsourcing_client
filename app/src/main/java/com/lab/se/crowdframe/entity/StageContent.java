package com.lab.se.crowdframe.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;



public class StageContent implements Serializable {
    private String name;
    private String desc;
    private double reward;
    private int stageIndex;
    private Timestamp ddl;
    private int workerNum;
    private int aggregateMethod;
    private long restrictions;
    private boolean isDeadlineActive;
    private double endDuration;
    private double startDuration;
    private double stageDuration;

    public StageContent(){
        isDeadlineActive = false;
        startDuration = 0;
        reward=0;
    }

    public double getStageDuration() {
        return stageDuration;
    }

    public void setStageDuration(double stageDuration) {
        this.stageDuration = stageDuration;
    }

    public double getStartDuration() {
        return startDuration;
    }

    public void setStartDuration(double startDuration) {
        this.startDuration = startDuration;
    }




    public boolean isDeadlineActive() {
        return isDeadlineActive;
    }

    public void setDeadlineActive(boolean deadlineActive) {
        isDeadlineActive = deadlineActive;
    }

    public double getEndDuration() {
        return endDuration;
    }

    public void setEndDuration(double duration) {
        this.endDuration = duration;
    }

    private List<LocationContent> locations;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public int getStageIndex() {
        return stageIndex;
    }

    public void setStageIndex(int stageIndex) {
        this.stageIndex = stageIndex;
    }

    public Timestamp getDdl() {
        return ddl;
    }

    public void setDdl(Timestamp ddl) {
        this.ddl = ddl;
    }

    public int getWorkerNum() {
        return workerNum;
    }

    public void setWorkerNum(int workerNum) {
        this.workerNum = workerNum;
    }

    public int getAggregateMethod() {
        return aggregateMethod;
    }

    public void setAggregateMethod(int aggregateMethod) {
        this.aggregateMethod = aggregateMethod;
    }

    public long getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(long restrictions) {
        this.restrictions = restrictions;
    }

    public List<LocationContent> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationContent> locations) {
        this.locations = locations;
    }

    public static class LocationContent implements Serializable{
        private double duration;

        public double getDuration() {
            return duration;
        }

        public void setDuration(double duration) {
            this.duration = duration;
        }

        private int type;
        private String address;
        private double longitude;
        private double latitude;
        private List<InputContent> inputs;
        private List<OutputContent> outputs;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public List<InputContent> getInputs() {
            return inputs;
        }

        public void setInputs(List<InputContent> inputs) {
            this.inputs = inputs;
        }

        public List<OutputContent> getOutputs() {
            return outputs;
        }

        public void setOutputs(List<OutputContent> outputs) {
            this.outputs = outputs;
        }
    }
    public static class InputContent implements Serializable{
        private int id;
        private int actionId;
        private int type;
        private String desc;
        private String value;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getActionId() {
            return actionId;
        }

        public void setActionId(int actionId) {
            this.actionId = actionId;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
    public static class OutputContent implements Serializable{
        private int id;
        private int actionId;
        private int type;
        private String desc;
        private String value;
        boolean isActive;
        private int intervalValue;
        private double upBound;
        private double lowBound;
        private String entries;
        private int aggregaMethod;

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public int getIntervalValue() {
            return intervalValue;
        }

        public void setIntervalValue(int intervalValue) {
            this.intervalValue = intervalValue;
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

        public String getEntries() {
            return entries;
        }

        public void setEntries(String entries) {
            this.entries = entries;
        }

        public int getAggregaMethod() {
            return aggregaMethod;
        }

        public void setAggregaMethod(int aggregaMethod) {
            this.aggregaMethod = aggregaMethod;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getActionId() {
            return actionId;
        }

        public void setActionId(int actionId) {
            this.actionId = actionId;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

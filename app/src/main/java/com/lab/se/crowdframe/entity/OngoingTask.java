package com.lab.se.crowdframe.entity;


import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by lenovo2013 on 2017/3/14.
 */

public class OngoingTask implements Serializable {

    private int taskId;
    private String taskTitle;
    private String taskDesc;
    private String taskProgress;
    private int currentStage;
    private double bonusReward;
    private Timestamp taskDeadline;
    private int stageId;
    private String stageName;
    private String stageDesc;
    private double reward;
    private Timestamp ddl;
    private Timestamp contract;
    List<Location> locations;

    public OngoingTask(int taskId,
                       String taskTitle,
                       String taskDesc,
                       String taskProgress,
                       int currentStage,
                       double bonusReward,
                       Timestamp taskDeadline,
                       int stageId,
                       String stageName,
                       String stageDesc,
                       double reward,
                       Timestamp ddl,
                       Timestamp contract,
                       List<Location> locations){
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskDesc = taskDesc;
        this.taskProgress = taskProgress;
        this.currentStage = currentStage;
        this.bonusReward = bonusReward;
        this.taskDeadline = taskDeadline;
        this.stageId = stageId;
        this.stageName = stageName;
        this.stageDesc = stageDesc;
        this.reward = reward;
        this.ddl = ddl;
        this.contract = contract;
        this.locations = locations;

    }

    public Timestamp getContract() {
        return contract;
    }

    public void setContract(Timestamp contract) {
        this.contract = contract;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getTaskProgress() {
        return taskProgress;
    }

    public void setTaskProgress(String taskProgress) {
        this.taskProgress = taskProgress;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;
    }

    public double getBonusReward() {
        return bonusReward;
    }

    public void setBonusReward(double bonusReward) {
        this.bonusReward = bonusReward;
    }

    public Timestamp getTaskDeadline() {
        return taskDeadline;
    }

    public void setTaskDeadline(Timestamp taskDeadline) {
        this.taskDeadline = taskDeadline;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStageDesc() {
        return stageDesc;
    }

    public void setStageDesc(String stageDesc) {
        this.stageDesc = stageDesc;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public Timestamp getDdl() {
        return ddl;
    }

    public void setDdl(Timestamp ddl) {
        this.ddl = ddl;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }


    public static class Location implements Serializable{
        private int id;
        private int stageId;
        private String address;
        private double longitude;
        private double latitude;
        private int type;
        private List<Input> inputs;
        private List<Output> outputs;

        public Location(int id, int stageId, String address,
                          double longitude, double latitude,int type,
                          List<Input> inputs,
                          List<Output> outputs){
            this.id = id;
            this.stageId = stageId;
            this.address = address;
            this.longitude = longitude;
            this.type = type;
            this.latitude = latitude;
            this.inputs = inputs;
            this.outputs = outputs;

        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getStageId() {
            return stageId;
        }

        public void setStageId(int stageId) {
            this.stageId = stageId;
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

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public List<Input> getInputs() {
            return inputs;
        }

        public void setInputs(List<Input> inputs) {
            this.inputs = inputs;
        }

        public List<Output> getOutputs() {
            return outputs;
        }

        public void setOutputs(List<Output> outputs) {
            this.outputs = outputs;
        }
    }

    public static class Input implements Serializable{
        private int id;
        private int actionId;
        private int type;
        private String desc;
        private String value;

        public Input(int id, int actionId, int type, String desc, String value){
            this.id = id;
            this.actionId = actionId;
            this.type = type;
            this.desc = desc;
            this.value = value;
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

    public static class Output implements Serializable{
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

        public Output(int id, int actionId, int type, String desc, String value, boolean isActive,
                      int intervalValue, double upBound, double lowBound, String entries, int aggregaMethod) {
            this.id = id;
            this.actionId = actionId;
            this.type = type;
            this.desc = desc;
            this.value = value;
            this.isActive = isActive;
            this.intervalValue = intervalValue;
            this.upBound = upBound;
            this.lowBound = lowBound;
            this.entries = entries;
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

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean isActive) {
            this.isActive = isActive;
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
    }

}


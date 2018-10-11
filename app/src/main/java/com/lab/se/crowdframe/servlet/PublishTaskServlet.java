package com.lab.se.crowdframe.servlet;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by fzj05 on 2017/4/1.
 */

public class PublishTaskServlet {
    public static class RequestBO {
        private int templateId;
        private int requesterId;
        private String title;
        private String description;
        private double bonusReward;
        private Timestamp publishTime;
        private Timestamp deadline;
        private int userScope;//0所有人可接，1只有软工实验室人可接
        private List<StageBO> stages;

        public int getTemplateId() {
            return templateId;
        }

        public void setTemplateId(int templateId) {
            this.templateId = templateId;
        }

        public int getRequesterId() {
            return requesterId;
        }

        public void setRequesterId(int requesterId) {
            this.requesterId = requesterId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getBonusReward() {
            return bonusReward;
        }

        public void setBonusReward(double bonusReward) {
            this.bonusReward = bonusReward;
        }

        public Timestamp getPublishTime() {
            return publishTime;
        }

        public void setPublishTime(Timestamp publishTime) {
            this.publishTime = publishTime;
        }

        public Timestamp getDeadline() {
            return deadline;
        }

        public void setDeadline(Timestamp deadline) {
            this.deadline = deadline;
        }

        public int getUserScope() {
            return userScope;
        }

        public void setUserScope(int userScope) {
            this.userScope = userScope;
        }

        public List<StageBO> getStages() {
            return stages;
        }

        public void setStages(List<StageBO> stages) {
            this.stages = stages;
        }

        public static class StageBO {
            private String name;
            private String desc;
            private double reward;
            private double stageDuration;
            private int stageIndex;
            private Timestamp ddl;
            private int workerNum;
            private int aggregateMethod;
            private long restrictions;
            private List<LocationBO> locations;

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

            public double getStageDuration() {
                return stageDuration;
            }

            public void setStageDuration(double stageDuration) {
                this.stageDuration = stageDuration;
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

            public List<LocationBO> getLocations() {
                return locations;
            }

            public void setLocations(List<LocationBO> locations) {
                this.locations = locations;
            }

        }

        public static class LocationBO {
            private int type;
            private String address;
            private double longitude;
            private double latitude;
            private double duration;
            private List<InputBO> inputs;
            private List<OutputBO> outputs;

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

            public double getDuration() {
                return duration;
            }

            public void setDuration(double duration) {
                this.duration = duration;
            }

            public List<InputBO> getInputs() {
                return inputs;
            }

            public void setInputs(List<InputBO> inputs) {
                this.inputs = inputs;
            }

            public List<OutputBO> getOutputs() {
                return outputs;
            }

            public void setOutputs(List<OutputBO> outputs) {
                this.outputs = outputs;
            }
        }

        public static class InputBO {
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

        public static class OutputBO {
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

    public static class ResponseBO {
        private int taskId;

        public int getTaskId() {
            return taskId;
        }

        public void setTaskId(int taskId) {
            this.taskId = taskId;
        }
    }
}

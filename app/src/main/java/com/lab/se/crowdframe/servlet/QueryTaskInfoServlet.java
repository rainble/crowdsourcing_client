package com.lab.se.crowdframe.servlet;

import java.util.List;
import java.sql.Timestamp;

/**
 * Created by lwh on 2017/4/19.
 */

public class QueryTaskInfoServlet {
    public static class RequestBO {
        private int taskId;
        private int userId;

        public int getTaskId() {
            return taskId;
        }

        public void setTaskId(int taskId) {
            this.taskId = taskId;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }
    }

    public static class ResponseBO {
        private int id;
        private int templateId;
        private int requesterId;
        private String title;
        private String description;
        private int status;
        private String progress;
        private int currentStage;
        private double bonusReward;
        private Timestamp publishTime;
        private Timestamp deadline;
        private List<StageBO> stages;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

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

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String progress) {
            this.progress = progress;
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

        public List<StageBO> getStages() {
            return stages;
        }

        public void setStages(List<StageBO> stages) {
            this.stages = stages;
        }

        public static class StageBO {
            private int stageId;
            private String name;
            private String desc;
            private double reward;
            private Timestamp ddl;
            private int workerNum;
            private List<String> workerNames;
            private int stageStatus;
            List<LocationBO> locations;

            public int getStageId() {
                return stageId;
            }

            public void setStageId(int stageId) {
                this.stageId = stageId;
            }

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

            public List<String> getWorkerNames() {
                return workerNames;
            }

            public void setWorkerNames(List<String> workerNames) {
                this.workerNames = workerNames;
            }

            public int getStageStatus() {
                return stageStatus;
            }

            public void setStageStatus(int stageStatus) {
                this.stageStatus = stageStatus;
            }

            public List<LocationBO> getLocations() {
                return locations;
            }

            public void setLocations(List<LocationBO> locations) {
                this.locations = locations;
            }

        }

        public static class LocationBO {
            private String address;
            private double longitude;
            private double latitude;
            private int type;
            private List<InputBO> inputs;
            private List<OutputBO> outputs;

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
}

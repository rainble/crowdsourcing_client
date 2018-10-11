package com.lab.se.crowdframe.servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class GetAcceptedTaskServlet{

	public static class RequestBO {
		private int userId;

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}
	}

	public static class ResponseBO {
		private List<TaskBO> tasks;

		public List<TaskBO> getTasks() {
			return tasks;
		}

		public void setTasks(List<TaskBO> tasks) {
			this.tasks = tasks;
		}

		public static class TaskBO {
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
			List<LocationBO> locations;

			public Timestamp getContract() {
				return contract;
			}

			public void setContract(Timestamp contract) {
				this.contract = contract;
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

			public int getStageId() {
				return stageId;
			}

			public void setStageId(int stageId) {
				this.stageId = stageId;
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

			public List<LocationBO> getLocations() {
				return locations;
			}

			public void setLocations(List<LocationBO> locations) {
				this.locations = locations;
			}
		}

		public static class LocationBO {
			private int id;
			private int stageId;
			private String address;
			private double longitude;
			private double latitude;
			private int type;
			private List<InputBO> inputs;
			private List<OutputBO> outputs;

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

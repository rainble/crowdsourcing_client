package com.lab.se.crowdframe.servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;


public class CompleteTaskServlet {

	public static class RequestBO {
		private int userId;
		private int stageId;
		private Timestamp finishTime;
		double longitude;
		double latitude;
		List<OutputBO> outputs;

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}

		public int getStageId() {
			return stageId;
		}

		public void setStageId(int stageId) {
			this.stageId = stageId;
		}

		public Timestamp getFinishTime() {
			return finishTime;
		}

		public void setFinishTime(Timestamp finishTime) {
			this.finishTime = finishTime;
		}

		public List<OutputBO> getOutputs() {
			return outputs;
		}

		public void setOutputs(List<OutputBO> outputs) {
			this.outputs = outputs;
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

	public static class ResponseBO {
		private int undertakeId;

		public int getUndertakeId() {
			return undertakeId;
		}

		public void setUndertakeId(int undertakeId) {
			this.undertakeId = undertakeId;
		}
	}
}

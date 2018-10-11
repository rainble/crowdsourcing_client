package com.lab.se.crowdframe.servlet;

import java.io.IOException;
import java.sql.Timestamp;


public class AcceptTaskServlet{

	public static class RequestBO {
		private int userId;
		private int taskId;
		private int currentStage;
		private Timestamp startTime;
		private Timestamp contractTime;

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}

		public int getTaskId() {
			return taskId;
		}

		public void setTaskId(int taskId) {
			this.taskId = taskId;
		}

		public int getCurrentStage() {
			return currentStage;
		}

		public void setCurrentStage(int currentStage) {
			this.currentStage = currentStage;
		}

		public Timestamp getStartTime() {
			return startTime;
		}

		public void setStartTime(Timestamp startTime) {
			this.startTime = startTime;
		}

		public Timestamp getContractTime() {
			return contractTime;
		}

		public void setContractTime(Timestamp contractTime) {
			this.contractTime = contractTime;
		}

	}
	public static class ResponseBO {
	}
}

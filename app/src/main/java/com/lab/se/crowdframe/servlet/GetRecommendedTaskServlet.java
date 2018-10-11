package com.lab.se.crowdframe.servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;



/**
 * Servlet implementation class GetRecommendedTaskServlet
 */

public class GetRecommendedTaskServlet{

	public static class RequestBO {
		private int userId;
		private double longitude;
		private double latitude;

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
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

	public static class ResponseBO {
		private List<TaskBO> tasks;

		public List<TaskBO> getTasks() {
			return tasks;
		}

		public void setTasks(List<TaskBO> tasks) {
			this.tasks = tasks;
		}

		public static class TaskBO {
			private int id;
			private int templateId;
			private int userId;
			private String requester;
			private String taskTitle;
			private String stageDesc;
			private double stageReward;
			private String progress;
			private int currentStage;
			private double bonusReward;
			private Timestamp publishTime;
			private Timestamp deadline;
			private double longitude;
			private double latitude;
			private Timestamp stageContract;

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

			public int getUserId() {
				return userId;
			}

			public void setUserId(int userId) {
				this.userId = userId;
			}

			public String getRequester() {
				return requester;
			}

			public void setRequester(String requester) {
				this.requester = requester;
			}

			public String getTaskTitle() {
				return taskTitle;
			}

			public void setTaskTitle(String taskTitle) {
				this.taskTitle = taskTitle;
			}

			public String getStageDesc() {
				return stageDesc;
			}

			public void setStageDesc(String stageDesc) {
				this.stageDesc = stageDesc;
			}

			public double getStageReward() {
				return stageReward;
			}

			public void setStageReward(double stageReward) {
				this.stageReward = stageReward;
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

			public Timestamp getStageContract() {
				return stageContract;
			}

			public void setStageContract(Timestamp stageContract) {
				this.stageContract = stageContract;
			}

		}
	}
}

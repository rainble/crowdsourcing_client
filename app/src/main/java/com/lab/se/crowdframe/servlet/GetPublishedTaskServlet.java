package com.lab.se.crowdframe.servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class GetPublishedTaskServlet {

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
			private int id;
			private int templateId;
			private String title;
			private String description;
			private int status;
			private String progress;
			private int currentStage;
			private double bonusReward;
			private Timestamp publishTime;
			private Timestamp deadline;

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
		}
	}
}

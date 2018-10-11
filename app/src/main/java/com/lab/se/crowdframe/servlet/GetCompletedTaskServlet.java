package com.lab.se.crowdframe.servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GetCompletedTaskServlet {

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
			private String stageName;
			private String stageDesc;
			private double reward;
			private int stageStatus;
			private Timestamp finishTime;

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

			public int getStageStatus() {
				return stageStatus;
			}

			public void setStageStatus(int stageStatus) {
				this.stageStatus = stageStatus;
			}

			public Timestamp getFinishTime() {
				return finishTime;
			}

			public void setFinishTime(Timestamp finishTime) {
				this.finishTime = finishTime;
			}
		}
	}
}

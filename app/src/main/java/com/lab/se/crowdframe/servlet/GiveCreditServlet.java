package com.lab.se.crowdframe.servlet;

/**
 * Created by lenovo2013 on 2017/6/28.
 */

public class GiveCreditServlet {

    public static class RequestBO{
        private int userId;
        private int taskId;

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

    }

    public static class ResponseBO{

    }
}

package com.lab.se.crowdframe.servlet;

/**
 * Created by lenovo2013 on 2017/6/21.
 */

public class GetPunishCreditServlet {

    public static class RequestBO{
        private int userId;
        private int stageId;

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

    }

    public static class ResponseBO{
        private double creditPunish;

        public double getCreditPunish() {
            return creditPunish;
        }

        public void setCreditPunish(double creditPunish) {
            this.creditPunish = creditPunish;
        }
    }

}

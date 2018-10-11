package com.lab.se.crowdframe.servlet;

/**
 * Created by fzj05 on 2017/4/24.
 */

public class GetUserInfoServlet {
    public static class RequestBO{
        private int userId;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

    }

    public static class ResponseBO{
        private double creditPublish;
        private double creditWithdraw;
        private int userTag;
        private int loginFlag;

        public double getCreditPublish() {
            return creditPublish;
        }

        public void setCreditPublish(double creditPublish) {
            this.creditPublish = creditPublish;
        }

        public double getCreditWithdraw() {
            return creditWithdraw;
        }

        public void setCreditWithdraw(double creditWithdraw) {
            this.creditWithdraw = creditWithdraw;
        }

        public int getUserTag() {
            return userTag;
        }

        public void setUserTag(int userTag) {
            this.userTag = userTag;
        }

        public int getLoginFlag() {
            return loginFlag;
        }

        public void setLoginFlag(int loginFlag) {
            this.loginFlag = loginFlag;
        }

    }
}

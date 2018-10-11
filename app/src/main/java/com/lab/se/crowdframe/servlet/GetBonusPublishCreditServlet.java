package com.lab.se.crowdframe.servlet;

/**
 * Created by lenovo2013 on 2017/5/31.
 */

public class GetBonusPublishCreditServlet {

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
    }
}

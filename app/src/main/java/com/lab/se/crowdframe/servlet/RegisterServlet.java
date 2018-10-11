package com.lab.se.crowdframe.servlet;

/**
 * Created by lwh on 2017/3/24.
 */

public class RegisterServlet {
    public static class RequestBO {
        private String account;
        private String password;
        private int userTag;//0非软工，1软工实验室
        private String phone;

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getUserTag() {
            return userTag;
        }

        public void setUserTag(int userTag) {
            this.userTag = userTag;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static class ResponseBO {
        private int userId;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

    }
}

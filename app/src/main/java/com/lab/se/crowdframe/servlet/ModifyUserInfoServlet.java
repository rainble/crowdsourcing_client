package com.lab.se.crowdframe.servlet;

/**
 * Created by lenovo2013 on 2017/5/31.
 */

public class ModifyUserInfoServlet {

    public static class RequestBO{
        private int id;
        private String password;
        private String avatar;
        private int tag;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public int getTag() {
            return tag;
        }

        public void setTag(int tag) {
            this.tag = tag;
        }

    }

    public static class ResponseBO{
        private int id;
        private String account;
        private double publishCredit;
        private double withdrawCredit;
        private String avatar;
        private int tag;
        private int loginFlag;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public double getPublishCredit() {
            return publishCredit;
        }

        public void setPublishCredit(double publishCredit) {
            this.publishCredit = publishCredit;
        }

        public double getWithdrawCredit() {
            return withdrawCredit;
        }

        public void setWithdrawCredit(double withdrawCredit) {
            this.withdrawCredit = withdrawCredit;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public int getTag() {
            return tag;
        }

        public void setTag(int tag) {
            this.tag = tag;
        }

        public int getLoginFlag() {
            return loginFlag;
        }

        public void setLoginFlag(int loginFlag) {
            this.loginFlag = loginFlag;
        }
    }
}

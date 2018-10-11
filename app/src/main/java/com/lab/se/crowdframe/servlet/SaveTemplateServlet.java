package com.lab.se.crowdframe.servlet;

import java.sql.Timestamp;
/**
 * Created by lwh on 2017/4/1.
 */

public class SaveTemplateServlet {
    public static class RequestBO {
        private int templateId;
        private int userId;
        private String name;
        private String description;
        private int heat;
        private String uri;
        private Timestamp createTime;
        private int totalStageNum;

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getHeat() {
            return heat;
        }

        public void setHeat(int heat) {
            this.heat = heat;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Timestamp getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Timestamp createTime) {
            this.createTime = createTime;
        }

        public int getTotalStageNum() {
            return totalStageNum;
        }

        public void setTotalStageNum(int totalStageNum) {
            this.totalStageNum = totalStageNum;
        }
    }

    public static class ResponseBO {
        private int templateID;

        public int getTemplateID() {
            return templateID;
        }

        public void setTemplateID(int templateID) {
            this.templateID = templateID;
        }

    }
}

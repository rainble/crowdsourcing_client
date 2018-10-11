package com.lab.se.crowdframe.servlet;

/**
 * Created by lwh on 2017/4/2.
 */

public class CollectTemplateServlet {
    public static class RequestBO {
        private int templateId;
        private int userId;
        private int indicator;

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

        public int getIndicator() {
            return indicator;
        }

        public void setIndicator(int indicator) {
            this.indicator = indicator;
        }
    }

    public static class ResponseBO {
        private int collectionId;

        public int getCollectionId() {
            return collectionId;
        }

        public void setCollectionId(int collectionId) {
            this.collectionId = collectionId;
        }
    }
}

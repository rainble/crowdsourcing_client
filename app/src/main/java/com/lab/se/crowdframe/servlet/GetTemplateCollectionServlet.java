package com.lab.se.crowdframe.servlet;


import java.sql.Timestamp;
import java.util.List;

public class GetTemplateCollectionServlet {
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
        private List<TemplateBO> templates;

        public List<TemplateBO> getTemplates() {
            return templates;
        }

        public void setTemplates(List<TemplateBO> templates) {
            this.templates = templates;
        }

        public static class TemplateBO {
            private int templateId;
            private String name;
            private String uri;
            private Timestamp createTime;
            private String creater;
            private int totalStageNum;

            public int getTemplateId() {
                return templateId;
            }

            public void setTemplateId(int templateId) {
                this.templateId = templateId;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
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

            public String getCreater() {
                return creater;
            }

            public void setCreater(String creater) {
                this.creater = creater;
            }

            public int getTotalStageNum() {
                return totalStageNum;
            }

            public void setTotalStageNum(int totalStageNum) {
                this.totalStageNum = totalStageNum;
            }
        }
    }


}

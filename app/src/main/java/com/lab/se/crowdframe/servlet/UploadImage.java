package com.lab.se.crowdframe.servlet;

public class UploadImage {


	public static class ResponseBO {
		private String startImageUri;
		private String endImageUri;
		
		public String getStartImageUri() {
			return startImageUri;
		}
		
		public void setStartImageUri(String startImageUri) {
			this.startImageUri = startImageUri;
		}
		
		public String getEndImageUri() {
			return endImageUri;
		}
		
		public void setEndImageUri(String endImageUri) {
			this.endImageUri = endImageUri;
		}
		
	}

}

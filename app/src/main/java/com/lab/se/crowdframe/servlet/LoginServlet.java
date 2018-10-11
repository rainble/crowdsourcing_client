package com.lab.se.crowdframe.servlet;


public class LoginServlet {

	public static class RequestBO {
		private String account;
		private String password;

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
	}

	public static class ResponseBO {
		private int userId;
		private double creditPublish;
		private double creditWithdraw;

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}

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

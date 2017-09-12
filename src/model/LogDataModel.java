package model;

public class LogDataModel {
	public static final int MISSING_GITHUB_NAME = 1;
	public static final int MISSING_GRAD_YEAR = 2;
	public static final int MISSING_FIRST_VISIT_DATE = 3;
	public static final int MISSING_HOME_LOCATION = 4;
	public static final int ADD_NEW_STUDENT = 5;

	private int logType, clientID;
	private StudentNameModel studentName;
	private String logString;
	
	public LogDataModel(int logType, StudentNameModel name, int clientID, String logString) {
		this.clientID = clientID;
		this.logType = logType;
		this.studentName = name;
		this.logString = logString;
	}

	public int getClientID() {
		return clientID;
	}

	public StudentNameModel getStudentName() {
		return studentName;
	}

	public int getLogType() {
		return logType;
	}

	public String getLogString() {
		return logString;
	}
}

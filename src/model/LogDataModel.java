package model;

public class LogDataModel {
	public static final int MISSING_GITHUB_NAME = 0;
	public static final int MISSING_GRAD_YEAR = 1;
	public static final int MISSING_FIRST_VISIT_DATE = 2;
	public static final int MISSING_HOME_LOCATION = 3;
	public static final int ADD_NEW_STUDENT = 4;
	public static final int ADD_NEW_STUDENT_NO_GITHUB = 5;

	private static final String[] logTypeName = { "Missing Github user name", "Missing Graduation year",
			"Missing First Visit date", "Missing Home Location", "Adding new Student",
			"Adding new Student with no Github user name" };

	private int logType, clientID;
	private StudentNameModel studentName;

	public LogDataModel(int logType, StudentNameModel name, int clientID) {
		this.clientID = clientID;
		this.logType = logType;
		this.studentName = name;
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
		return logTypeName[logType];
	}
}

package model;

public class LogDataModel {
	public static final int MISSING_GITHUB_NAME = 0;
	public static final int MISSING_GRAD_YEAR = 1;
	public static final int MISSING_FIRST_VISIT_DATE = 2;
	public static final int MISSING_HOME_LOCATION = 3;
	public static final int MISSING_GENDER = 4;
	public static final int ADD_NEW_STUDENT = 5;
	public static final int ADD_NEW_STUDENT_NO_GITHUB = 6;
	public static final int UPDATE_STUDENT_INFO = 7;
	public static final int UPDATE_STUDENT_ATTENDANCE = 8;
	public static final int INVALID_CLASS_NAME = 9;
	public static final int REMOVE_INACTIVE_STUDENT = 10;
	public static final int STUDENT_NOT_FOUND = 11;
	public static final int INVALID_GRAD_YEAR = 12;
	public static final int GITHUB_IMPORT_FAILURE = 13;
	public static final int GITHUB_PARSING_ERROR = 14;

	private static final String[] logTypeName = { "Missing Github user name", "Missing Graduation year",
			"Missing First Visit date", "Missing Home Location", "Missing Gender", "Added new Student",
			"Added new Student with no Github user name", "Updated Student Info", "Updated Student Attendance",
			"Invalid Class Name", "Removed inactive student", "Attendance data with no matching student",
			"Invalid Grad Year field", "Failure importing Github data", "Failure parsing Github data" };

	private int logType, clientID;
	private StudentNameModel studentName;
	private String appendedString;

	public LogDataModel(int logType, StudentNameModel name, int clientID, String appendedString) {
		this.clientID = clientID;
		this.logType = logType;
		this.studentName = name;
		this.appendedString = appendedString;
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
		return logTypeName[logType] + appendedString;
	}
}

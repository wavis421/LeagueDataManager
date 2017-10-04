package model;

public class LogDataModel {
	public static final int MISSING_GITHUB_NAME = 0;
	public static final int MISSING_GRAD_YEAR = 1;
	public static final int MISSING_FIRST_VISIT_DATE = 2;
	public static final int MISSING_HOME_LOCATION = 3;
	public static final int ADD_NEW_STUDENT = 4;
	public static final int ADD_NEW_STUDENT_NO_GITHUB = 5;
	public static final int UPDATE_STUDENT_INFO = 6;
	public static final int UPDATE_STUDENT_ATTENDANCE = 7;
	public static final int INVALID_CLASS_NAME = 8;
	public static final int REMOVE_INACTIVE_STUDENT = 9;
	public static final int STUDENT_NOT_FOUND = 10;
	public static final int MISSING_COMMIT_DATA = 11;
	public static final int INVALID_GRAD_YEAR = 12;

	private static final String[] logTypeName = { "Missing Github user name", "Missing Graduation year",
			"Missing First Visit date", "Missing Home Location", "Added new Student",
			"Added new Student with no Github user name", "Updated Student Information", 
			"Updated Student Attendance Data", "Invalid Class Name",
			"Removed inactive student", "Attendance data with no matching student",
			"Missing commit data for Github user", "Invalid Grad Year field"};

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

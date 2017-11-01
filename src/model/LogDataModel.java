package model;

import org.joda.time.DateTime;

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
	public static final int UPDATE_GITHUB_COMMENTS = 9;
	public static final int INVALID_CLASS_NAME = 10;
	public static final int REMOVE_INACTIVE_STUDENT = 11;
	public static final int STUDENT_NOT_FOUND = 12;
	public static final int INVALID_GRAD_YEAR = 13;
	public static final int GITHUB_IMPORT_FAILURE = 14;
	public static final int GITHUB_PARSING_ERROR = 15;
	public static final int STUDENT_DB_ERROR = 16;
	public static final int ATTENDANCE_DB_ERROR = 17;
	public static final int PIKE13_IMPORT_ERROR = 18;
	public static final int GITHUB_IMPORT_ERROR = 19;
	public static final int GITHUB_MODULE_REPO_ERROR = 20;

	private static final String[] logTypeName = { "Missing Github user name", "Missing Graduation year",
			"Missing First Visit date", "Missing Home Location", "Missing Gender", "Added new Student",
			"Added new Student with no Github user name", "Updated Student Info", "Updated Student Attendance",
			"Updated Github Comments", "Invalid Class Name", "Removed inactive student",
			"Attendance data with no matching student", "Invalid Grad Year field", "Failure importing Github data",
			"Failure parsing Github data", "Student Database error", "Attendance Database error", "Pike13 Import error",
			"Github Import error", "Failure getting Module Repo"};

	private int logType, clientID;
	private StudentNameModel studentName;
	private String appendedString;
	private DateTime date;

	public LogDataModel(int logType, StudentNameModel name, int clientID, String appendedString) {
		this.clientID = clientID;
		this.logType = logType;
		this.studentName = name;
		this.appendedString = appendedString;
		date = new DateTime();
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
	
	public String getDate() {
		return date.toString("yyyy-MM-dd HH:mm");
	}
}

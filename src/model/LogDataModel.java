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
	public static final int UPDATE_GITHUB_COMMENTS = 9;
	public static final int INVALID_CLASS_NAME = 10;
	public static final int REMOVE_INACTIVE_STUDENT = 11;
	public static final int STUDENT_NOT_FOUND = 12;
	public static final int INVALID_GRAD_YEAR = 13;
	public static final int GITHUB_IMPORT_FAILURE = 14;
	public static final int GITHUB_PARSING_ERROR = 15;
	public static final int STUDENT_DB_ERROR = 16;
	public static final int ATTENDANCE_DB_ERROR = 17;
	public static final int LOG_DATA_DB_ERROR = 18;
	public static final int PIKE13_CONNECTION_ERROR = 19;
	public static final int PIKE13_IMPORT_ERROR = 20;
	public static final int GITHUB_IMPORT_ERROR = 21;
	public static final int GITHUB_MODULE_REPO_ERROR = 22;
	public static final int FILE_IMPORT_ERROR = 23;
	public static final int UNKNOWN_HOME_LOCATION = 24;
	public static final int STARTING_STUDENT_IMPORT = 25;
	public static final int STUDENT_IMPORT_COMPLETE = 26;
	public static final int STARTING_ATTENDANCE_IMPORT = 27;
	public static final int ATTENDANCE_IMPORT_COMPLETE = 28;
	public static final int STARTING_GITHUB_IMPORT = 29;
	public static final int GITHUB_IMPORT_COMPLETE = 30;
	public static final int GITHUB_IMPORT_ABORTED = 31;

	private static final String[] logTypeName = { "Missing Github user name", "Missing Graduation year",
			"Missing First Visit date", "Missing Home Location", "Missing Gender", "Added new Student",
			"Added new Student with no Github user name", "Updated Student Info", "Updated Student Attendance",
			"Updated Github Comments", "Invalid Class Name", "Removed inactive student",
			"Attendance data with no matching student", "Invalid Grad Year field", "Failure importing Github data",
			"Failure parsing Github data", "Student Database error", "Attendance Database error",
			"Logging Database error", "Pike13 Connection error", "Pike13 Import error", "Github Import error",
			"Failure getting Module Repo", "Error importing from file", "Unrecognized Home Location",
			"*** STARTING STUDENT IMPORT ***", "*** STUDENT IMPORT COMPLETE ***", "*** STARTING ATTENDANCE IMPORT",
			"*** ATTENDANCE IMPORT COMPLETE ***", "*** STARTING GITHUB IMPORT", "*** GITHUB IMPORT COMPLETE ***",
			"*** GITHUB IMPORT ABORTED" };

	private int logType, clientID;
	private StudentNameModel studentName;
	private String appendedString;
	private String logDate;

	public LogDataModel(int logType, String date, StudentNameModel name, int clientID, String appendedString) {
		this.clientID = clientID;
		this.logType = logType;
		this.studentName = name;
		this.appendedString = appendedString;
		this.logDate = date;
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
		return logDate;
	}

	public static String getLogType(int logType) {
		return logTypeName[logType];
	}
}

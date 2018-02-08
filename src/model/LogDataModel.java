package model;

public class LogDataModel {
	public static final int MISSING_GITHUB_NAME = 0;
	public static final int MISSING_GRAD_YEAR = 1;
	public static final int MISSING_FIRST_VISIT_DATE = 2;
	public static final int MISSING_HOME_LOCATION = 3;
	public static final int MISSING_GENDER = 4;

	public static final int ADD_NEW_STUDENT = 5;
	public static final int ADD_NEW_STUDENT_NO_GITHUB = 6;
	public static final int ADD_CLASS_TO_SCHEDULE = 7;
	public static final int UPDATE_STUDENT_INFO = 8;
	public static final int UPDATE_STUDENT_ATTENDANCE = 9;
	public static final int UPDATE_GITHUB_COMMENTS = 10;
	public static final int REMOVE_INACTIVE_STUDENT = 11;
	public static final int REMOVE_CLASS_FROM_SCHEDULE = 12;

	public static final int STUDENT_NOT_FOUND = 13;
	public static final int INVALID_CLASS_NAME = 14;
	public static final int INVALID_GRAD_YEAR = 15;
	public static final int UNKNOWN_HOME_LOCATION = 16;

	public static final int GITHUB_IMPORT_FAILURE = 17;
	public static final int GITHUB_PARSING_ERROR = 18;
	public static final int STUDENT_DB_ERROR = 19;
	public static final int ATTENDANCE_DB_ERROR = 20;
	public static final int LOG_DATA_DB_ERROR = 21;
	public static final int SCHEDULE_DB_ERROR = 22;
	public static final int PIKE13_CONNECTION_ERROR = 23;
	public static final int PIKE13_IMPORT_ERROR = 24;
	public static final int GITHUB_MODULE_REPO_ERROR = 25;

	public static final int STARTING_STUDENT_IMPORT = 26;
	public static final int STUDENT_IMPORT_COMPLETE = 27;
	public static final int STARTING_ATTENDANCE_IMPORT = 28;
	public static final int ATTENDANCE_IMPORT_COMPLETE = 29;
	public static final int STARTING_GITHUB_IMPORT = 30;
	public static final int GITHUB_IMPORT_COMPLETE = 31;
	public static final int GITHUB_IMPORT_ABORTED = 32;
	public static final int STARTING_SCHEDULE_IMPORT = 33;
	public static final int SCHEDULE_IMPORT_COMPLETE = 34;
	public static final int STARTING_SALES_FORCE_IMPORT = 35;
	public static final int SALES_FORCE_IMPORT_COMPLETE = 36;

	public static final int SALES_FORCE_CONNECTION_ERROR = 37;
	public static final int SALES_FORCE_CONTACTS_IMPORT_ERROR = 38;
	public static final int SALES_FORCE_UPSERT_ATTENDANCE_ERROR = 39;
	public static final int SALES_FORCE_DELETE_ATTENDANCE_ERROR = 40;
	public static final int MISSING_SF_CONTACT_FOR_ATTENDANCE = 41;
	public static final int SALES_FORCE_ATTENDANCE_UPDATED = 42;
	public static final int SALES_FORCE_CANCELED_ATTEND_CLEANUP = 43;
	public static final int SALES_FORCE_DELETE_ATTENDANCE_RECORD = 44;
	public static final int SALES_FORCE_IMPORT_ABORTED = 45;
	public static final int SALES_FORCE_STAFF_HOURS_UPDATED = 46;
	public static final int SALES_FORCE_UPSERT_STAFF_HOURS_ERROR = 47;
	public static final int MISSING_SALES_FORCE_STAFF_MEMBER = 48;
	public static final int MISSING_PIKE13_STAFF_MEMBER = 49;
	public static final int SF_ATTENDANCE_IMPORT_ERROR = 50;
	public static final int SF_STAFF_HOURS_IMPORT_ERROR = 51;

	// This should always be last
	private static final int LOG_TYPE_OUT_OF_BOUNDS = 52;

	private static final String[] logTypeName = { "Missing Github user name", "Missing Graduation year",
			"Missing First Visit date", "Missing Home Location", "Missing Gender", "Added new Student",
			"Added new Student with no Github user name", "Added class to schedule", "Updated Student Info",
			"Updated Student Attendance", "Updated Github Comments", "Removed inactive student",
			"Removed class from schedule",

			"Attendance data with no matching student", "Invalid Class Name", "Invalid Grad Year field",
			"Unrecognized Home Location", "Failure importing Github data", "Failure parsing Github data",
			"Student Database error", "Attendance Database error", "Logging Database error", "Schedule Database error",
			"Pike13 Connection error", "Pike13 Import error", "Failure getting Module Repo",

			"*** BEGIN STUDENT IMPORT", "*** STUDENT IMPORT COMPLETE", "*** BEGIN ATTENDANCE IMPORT",
			"*** ATTENDANCE IMPORT COMPLETE", "*** BEGIN GITHUB IMPORT", "*** GITHUB IMPORT COMPLETE",
			"*** GITHUB IMPORT ABORTED", "*** BEGIN SCHEDULE IMPORT", "*** SCHEDULE IMPORT COMPLETE",
			"*** BEGIN SALESFORCE IMPORT", "*** SALESFORCE IMPORT COMPLETE",

			"SalesForce Connection error", "SalesForce Contacts Import error", "SalesForce Upsert Attendance error",
			"SalesForce Delete Attendance error", "Missing SF contact for Attendance",
			"SalesForce attendance updated", "SalesForce canceled visits removed", "Deleted canceled Visit ID",
			"*** SALESFORCE IMPORT ABORTED", "SalesForce Staff Hours updated", "SalesForce Upsert Staff Hours error",
			"Missing SF Staff Member", "Missing Pike13 Staff Member", "SalesForce Attendance import error",
			"SalesForce Staff Hours Import error" };

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
		if (studentName == null)
			return new StudentNameModel("", "", false);
		else
			return studentName;
	}

	public String getLogString() {
		if (logType >= LOG_TYPE_OUT_OF_BOUNDS)
			return "Unexpected Log type " + logType + ": software update required";
		else
			return logTypeName[logType] + appendedString;
	}

	public String getDate() {
		return logDate;
	}
}

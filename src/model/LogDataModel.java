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
	public static final int MISSING_GITHUB_COMMENTS_BY_EVENT = 52;
	public static final int CREATE_SALES_FORCE_ACCOUNT = 53;
	public static final int DUPLICATE_SF_ACCOUNT_NAME = 54;
	public static final int MISSING_PIKE13_ACCT_MGR_FOR_CLIENT = 55;
	public static final int SALES_FORCE_DELETE_CLIENT_RECORD = 56;
	public static final int SF_CLIENT_IMPORT_ERROR = 57;
	public static final int SF_ACCOUNT_IMPORT_ERROR = 58;
	public static final int SALES_FORCE_UPSERT_CLIENTS_ERROR = 59;

	public static final int SALES_FORCE_UPSERT_ACCOUNT_ERROR = 60;
	public static final int SF_CLIENTS_UPDATED = 61;
	public static final int INVOICE_REPORT_ENROLL_RECORD_NOT_FOUND = 62;
	public static final int MISSING_SF_CLIENT_ID_FOR_TA = 63;
	public static final int BLANK_EVENT_NAME_FOR_ATTENDANCE = 64;
	public static final int ATTENDANCE_LOC_CODE_INVALID = 65;
	public static final int ATTENDANCE_LOC_CODE_MISMATCH = 66;
	public static final int MISSING_ACCOUNT_FOR_TA_OR_PARENT = 67;
	public static final int UPSERTED_ACCOUNT_RETRIEVAL_ERROR = 68;
	public static final int STARTING_COURSES_IMPORT = 69;

	public static final int COURSES_IMPORT_COMPLETE = 70;
	public static final int COURSES_DB_ERROR = 71;
	public static final int ADD_COURSES_TO_SCHEDULE = 72;
	public static final int REMOVE_COURSES_FROM_SCHEDULE = 73;
	public static final int UPDATE_COURSES_INFO = 74;
	public static final int UPDATE_ATTENDANCE_STATE = 75;
	public static final int STARTING_COURSE_ATTENDANCE_IMPORT = 76;
	public static final int COURSE_ATTENDANCE_IMPORT_COMPLETE = 77;
	public static final int MISSING_SF_CONTACT_FOR_GRADUATION = 78;
	public static final int SALES_FORCE_UPSERT_DIARY_ERROR = 79;

	public static final int STUDENT_GRADUATION = 80;
	public static final int SF_DIARY_UPDATED = 81;
	public static final int SF_DIARY_IMPORT_ERROR = 82;
	public static final int MISSING_VISIT_ID_FOR_SF_IMPORT = 83;
	public static final int CLASS_LEVEL_MISMATCH = 84;
	public static final int SF_ENROLLMENT_STATS_UPDATED = 85;
	public static final int MISSING_SF_CONTACT_FOR_ENROLL_STATS = 86;
	public static final int SF_ENROLL_STATS_IMPORT_ERROR = 87;
	public static final int UPDATE_CLASS_INFO = 88;

	// This should always be last
	private static final int LOG_TYPE_OUT_OF_BOUNDS = 89;

	private static final String[] logTypeName = { "Missing Github user name", "Missing Graduation year",
			"Missing First Visit date", "Missing Home Location", "Missing Gender", "Added new Student",
			"Added new Student with no Github user name", "Added class to schedule", "Updated Student Info",
			"Updated Student Attendance",

			// 10
			"Updated Github Comments", "Removed inactive student", "Removed class from schedule",
			"Attendance data with no matching student", "Invalid Class Name", "Invalid Grad Year field",
			"Unrecognized Home Location", "Failure importing Github data", "Failure parsing Github data",
			"Student Database error",

			// 20
			"Attendance Database error", "Logging Database error", "Schedule Database error", "Pike13 Connection error",
			"Pike13 Import error", "Failure getting Module Repo", "*** BEGIN STUDENT IMPORT",
			"*** STUDENT IMPORT COMPLETE", "*** BEGIN CLASS ATTENDANCE IMPORT", "*** CLASS ATTENDANCE IMPORT COMPLETE",

			// 30
			"*** BEGIN GITHUB IMPORT", "*** GITHUB IMPORT COMPLETE", "*** GITHUB IMPORT ABORTED",
			"*** BEGIN SCHEDULE IMPORT", "*** SCHEDULE IMPORT COMPLETE", "*** BEGIN SALESFORCE IMPORT",
			"*** SALESFORCE IMPORT COMPLETE", "SalesForce Connection error", "SalesForce Contacts Import error",
			"SalesForce Upsert Attendance error",

			// 40
			"SalesForce Delete Attendance error", "Missing SF contact for Attendance", "SalesForce attendance updated",
			"SalesForce canceled visits removed", "Deleted canceled Visit ID", "*** SALESFORCE IMPORT ABORTED",
			"SalesForce Staff Hours updated", "SalesForce Upsert Staff Hours error", "Missing SF Staff Member",
			"Missing Pike13 Staff Member",

			// 50
			"SalesForce Attendance import error", "SalesForce Staff Hours Import error", "Missing github comments",
			"Create SalesForce account", "Warning: Duplicate SalesForce account name",
			"Missing Pike13 Acct Manager for client", "Deleted SalesForce Contact record",
			"SalesForce Contact import error", "SalesForce Account import error", "SalesForce Upsert Clients error",

			// 60
			"SalesForce Upsert Account error", "SalesForce Contacts updated",
			"Invoice Report: Enrollment record not found", "Missing SalesForce ClientID",
			"Blank Event Name for Attendance record", "Attendance Location Code invalid",
			"Attendance event Location Code mismatch", "Missing Account for TA/Parent",
			"Failure retrieving upserted Account", "*** BEGIN COURSES IMPORT",

			// 70
			"*** COURSES IMPORT COMPLETE", "Courses Database error", "Add courses to schedule",
			"Remove Courses from schedule", "Update Courses info", "Updated Attendance state/teacher(s)",
			"*** BEGIN COURSE ATTENDANCE IMPORT", "*** COURSE ATTENDANCE IMPORT COMPLETE",
			"Missing SF contact for Graduation", "SalesForce Upsert Diary error",

			// 80
			"Graduated", "SalesForce Diary updated", "SalesForce Diary Import error", "Missing Visit ID for SF Import",
			"Class Level Mismatch", "SalesForce Enrollment Stats updated", "Missing SF contact for Enrollment Stats", 
			"SalesForce Enrollment Stats import error", "Update Class Info" };

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

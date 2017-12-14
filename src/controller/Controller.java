package controller;

import java.awt.Cursor;
import java.util.ArrayList;

import javax.swing.JFrame;

import model.AttendanceEventModel;
import model.AttendanceModel;
import model.InvoiceModel;
import model.LogDataModel;
import model.MySqlDatabase;
import model.ScheduleModel;
import model.StudentImportModel;
import model.StudentModel;
import model.StudentNameModel;

public class Controller {
	// Different port than League Student Tracker to allow simultaneous connects
	private static final int LOCAL_SSH_PORT = 5000;

	private MySqlDatabase sqlDb;
	private GithubApi githubApi;
	private Pike13Api pike13Api;
	private JFrame parent;

	public Controller(JFrame parent, String awsPassword, String githubToken, String pike13Token) {
		this.parent = parent;
		sqlDb = new MySqlDatabase(parent, awsPassword, LOCAL_SSH_PORT);
		githubApi = new GithubApi(sqlDb, githubToken);
		pike13Api = new Pike13Api(sqlDb, pike13Token);
	}

	/*
	 * ------- Database Connections -------
	 */
	public boolean connectDatabase() {
		return sqlDb.connectDatabase();
	}

	public void disconnectDatabase() {
		sqlDb.disconnectDatabase();
	}

	/*
	 * ------- Logging Activity -------
	 */
	public ArrayList<LogDataModel> getDbLogData() {
		return sqlDb.getLogData();
	}

	public void clearDbLogData() {
		sqlDb.clearLogData();
	}

	/*
	 * ------- Database Queries -------
	 */
	public ArrayList<StudentModel> getAllStudents() {
		return sqlDb.getAllStudents();
	}

	public ArrayList<StudentModel> getStudentByClientID(int clientID) {
		return sqlDb.getStudentByClientID(clientID);
	}

	public ArrayList<StudentModel> getStudentsNotInMasterDB() {
		return sqlDb.getStudentsNotInMasterDB();
	}

	public void removeInactiveStudents() {
		sqlDb.removeInactiveStudents();
	}

	public void removeStudentByClientID(int clientID) {
		sqlDb.removeStudentByClientID(clientID);
	}

	public ArrayList<AttendanceModel> getAllAttendance() {
		return sqlDb.getAllAttendance();
	}

	public ArrayList<AttendanceModel> getAttendanceByClassName(String className) {
		return sqlDb.getAttendanceByClassName(className);
	}

	public ArrayList<AttendanceModel> getAttendanceByClientID(String clientID) {
		return sqlDb.getAttendanceByClientID(clientID);
	}

	public ArrayList<StudentNameModel> getAllStudentNames() {
		return sqlDb.getAllStudentNames();
	}

	public ArrayList<String> getClassNamesByLevel(int level) {
		return sqlDb.getClassNamesByLevel(level);
	}

	public ArrayList<ScheduleModel> getClassSchedule() {
		return sqlDb.getClassSchedule();
	}

	/*
	 * ------- File & data import/export -------
	 */

	public ArrayList<InvoiceModel> getInvoices() {
		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get invoice data from Pike13
		ArrayList<InvoiceModel> invoiceList = pike13Api.getInvoices();

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		return invoiceList;
	}

	public void importStudentsFromPike13() {
		sqlDb.insertLogData(LogDataModel.STARTING_STUDENT_IMPORT, new StudentNameModel("", "", false), 0, " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get data from Pike13
		ArrayList<StudentImportModel> studentList = pike13Api.getClients();

		// Update changes in database
		if (studentList.size() > 0)
			sqlDb.importStudents(studentList);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		sqlDb.insertLogData(LogDataModel.STUDENT_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0, " ***");
	}

	public void importAttendanceFromPike13(String startDate) {
		sqlDb.insertLogData(LogDataModel.STARTING_ATTENDANCE_IMPORT, new StudentNameModel("", "", false), 0,
				" starting from " + startDate.substring(0, 10) + " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get data from Pike13
		ArrayList<AttendanceEventModel> eventList = pike13Api.getEnrollment(startDate);

		// Update changes in database
		if (eventList.size() > 0)
			sqlDb.importAttendance(eventList);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		sqlDb.insertLogData(LogDataModel.ATTENDANCE_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0, " ***");
	}

	public void importScheduleFromPike13() {
		sqlDb.insertLogData(LogDataModel.STARTING_SCHEDULE_IMPORT, new StudentNameModel("", "", false), 0, " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get data from Pike13
		ArrayList<ScheduleModel> eventList = pike13Api.getSchedule();

		// Update changes in database
		if (eventList.size() > 0)
			sqlDb.importSchedule(eventList);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		sqlDb.insertLogData(LogDataModel.SCHEDULE_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0, " ***");
	}

	public void importGithubComments(String startDate) {
		boolean result;

		// Import github from start date, update missing github data for new users
		sqlDb.insertLogData(LogDataModel.STARTING_GITHUB_IMPORT, new StudentNameModel("", "", false), 0,
				" starting from " + startDate.substring(0, 10) + " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		result = githubApi.importGithubComments(startDate, 0);
		if (result) {
			githubApi.importGithubCommentsByLevel(0, startDate, 0);
			githubApi.updateMissingGithubComments();
		}

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		if (result)
			sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0, " ***");
		else
			sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_ABORTED, new StudentNameModel("", "", false), 0,
					": Github API rate limit exceeded ***");
	}

	public void importAllDatabases(String startDate) {
		// Import students, attendance, schedule and github data
		importStudentsFromPike13();
		importAttendanceFromPike13(startDate);
		importScheduleFromPike13();
		importGithubComments(startDate);
	}
}

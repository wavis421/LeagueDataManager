package controller;

import java.awt.Cursor;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.joda.time.DateTime;

import model.AttendanceEventModel;
import model.AttendanceModel;
import model.DateRangeEvent;
import model.InvoiceModel;
import model.LogDataModel;
import model.MySqlDatabase;
import model.ScheduleModel;
import model.StudentImportModel;
import model.StudentModel;
import model.StudentNameModel;

public class Controller {
	private MySqlDatabase sqlDb;
	private GithubApi githubApi;
	private Pike13Api pike13Api;
	private JFrame parent;

	public Controller(JFrame parent, String awsPassword, String githubToken, String pike13Token) {
		this.parent = parent;
		sqlDb = new MySqlDatabase(parent, awsPassword, MySqlDatabase.TRACKER_APP_SSH_PORT);
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
	public ArrayList<StudentModel> getActiveStudents() {
		return sqlDb.getActiveStudents();
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

	public ArrayList<InvoiceModel> getInvoices(DateRangeEvent dateRange) {
		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get invoice data from Pike13
		ArrayList<InvoiceModel> invoiceList = pike13Api.getInvoices(dateRange);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		return invoiceList;
	}

	public void importStudentsFromPike13() {
		String today = new DateTime().toString("yyyy-MM-dd").substring(0, 10);
		sqlDb.insertLogData(LogDataModel.STARTING_STUDENT_IMPORT, new StudentNameModel("", "", false), 0,
				" for " + today + " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get data from Pike13
		ArrayList<StudentImportModel> studentList = pike13Api.getClients();

		// Update changes in database
		if (studentList.size() > 0)
			sqlDb.importStudents(studentList);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		sqlDb.insertLogData(LogDataModel.STUDENT_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0,
				" for " + today + " ***");
	}

	public void importAttendanceFromPike13(String startDate) {
		sqlDb.insertLogData(LogDataModel.STARTING_ATTENDANCE_IMPORT, new StudentNameModel("", "", false), 0,
				" starting from " + startDate.substring(0, 10) + " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get attendance data from Pike13 for all students
		ArrayList<AttendanceEventModel> eventList = pike13Api.getAttendance(startDate);

		// Update changes in database
		if (eventList.size() > 0)
			sqlDb.importAttendance(eventList);

		// Get 'missing' attendance for new and returned students
		ArrayList<StudentModel> newStudents = sqlDb.getStudentsUsingFlag("NewStudent");
		if (newStudents.size() > 0) {
			eventList = pike13Api.getMissingAttendance(startDate, newStudents);
			if (eventList.size() > 0)
				sqlDb.importAttendance(eventList);
		}

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		sqlDb.insertLogData(LogDataModel.ATTENDANCE_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0,
				" starting from " + startDate.substring(0, 10) + " ***");
	}

	public void importScheduleFromPike13() {
		// Get last 2 weeks of data since if there's a holiday there won't be any data!
		String startDate = new DateTime().minusDays(14).toString("yyyy-MM-dd");
		sqlDb.insertLogData(LogDataModel.STARTING_SCHEDULE_IMPORT, new StudentNameModel("", "", false), 0,
				" as of " + startDate.substring(0, 10) + " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get data from Pike13
		ArrayList<ScheduleModel> eventList = pike13Api.getSchedule(startDate);

		// Update changes in database
		if (eventList.size() > 0)
			sqlDb.importSchedule(eventList);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		sqlDb.insertLogData(LogDataModel.SCHEDULE_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0,
				" as of " + startDate.substring(0, 10) + " ***");
	}

	public void importGithubComments(String startDate) {
		boolean result = true;

		// Import github from start date, update missing github data for new users
		sqlDb.insertLogData(LogDataModel.STARTING_GITHUB_IMPORT, new StudentNameModel("", "", false), 0,
				" starting from " + startDate.substring(0, 10) + " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get list of events with missing comments
		ArrayList<AttendanceEventModel> eventList = sqlDb.getEventsWithNoComments(startDate, 0, false);
		if (eventList.size() > 0) {
			// Import Github comments
			result = githubApi.importGithubComments(startDate, eventList);

			if (result) {
				// Remove updated events from eventList before processing further
				removeUpdatedGithubEvents(eventList);

				if (eventList.size() > 0) {
					// Import github comments for level 0 & 1
					githubApi.importGithubCommentsByLevel(0, startDate, null, eventList);
					removeUpdatedGithubEvents(eventList);

					if (eventList.size() > 0) {
						githubApi.importGithubCommentsByLevel(1, startDate, null, eventList);

						// Update any remaining null comments to show event was processed
						githubApi.updateEmptyGithubComments(eventList);
					}
				}
			}
		}

		// Updated github comments for users with new user name
		githubApi.updateMissingGithubComments();

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		if (result)
			sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0,
					" starting from " + startDate.substring(0, 10) + " ***");
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

	private void removeUpdatedGithubEvents(ArrayList<AttendanceEventModel> eventList) {
		for (int i = eventList.size() - 1; i >= 0; i--) {
			AttendanceEventModel model = eventList.get(i);
			if (!model.getGithubComments().equals("")) {
				eventList.remove(model);
			}
		}
	}
}

package controller;

import java.awt.Cursor;
import java.util.ArrayList;

import javax.swing.JFrame;

import model.AttendanceEventModel;
import model.AttendanceModel;
import model.CoursesModel;
import model.DateRangeEvent;
import model.GithubModel;
import model.InvoiceModel;
import model.LocationModel;
import model.LogDataModel;
import model.MySqlConnection;
import model.MySqlDatabase;
import model.MySqlDbLogging;
import model.ScheduleModel;
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
		new MySqlDbLogging(sqlDb);
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
	
	public String getKeyFilePath() {
		return MySqlConnection.getKeyFilePath();
	}

	/*
	 * ------- Logging Activity -------
	 */
	public ArrayList<LogDataModel> getDbLogData() {
		return MySqlDbLogging.getLogData();
	}

	public void clearDbLogData() {
		MySqlDbLogging.clearLogData();
	}

	/*
	 * ------- Location Data -------
	 */
	public ArrayList<LocationModel> getLocationList() {
		return sqlDb.getLocationList();
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

	public ArrayList<GithubModel> getStudentsWithNoRecentGithub(String sinceDate, int minClassesWithoutGithub) {
		return sqlDb.getStudentsWithNoRecentGithub(sinceDate, minClassesWithoutGithub);
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

	public ArrayList<AttendanceModel> getAttendanceByCourseName(String courseName) {
		return sqlDb.getAttendanceByCourseName(courseName);
	}

	public ArrayList<AttendanceModel> getAttendanceByClientID(String clientID) {
		return sqlDb.getAttendanceByClientID(clientID);
	}

	public ArrayList<AttendanceModel> getAttendanceByClassByDate(String className, String day) {
		return sqlDb.getAttendanceByClassByDate(className, day);
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

	public ArrayList<CoursesModel> getCourseSchedule() {
		return sqlDb.getCourseSchedule(null);
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

	private void removeUpdatedGithubEvents(ArrayList<AttendanceEventModel> eventList) {
		for (int i = eventList.size() - 1; i >= 0; i--) {
			AttendanceEventModel model = eventList.get(i);
			if (!model.getGithubComments().equals("")) {
				eventList.remove(model);
			}
		}
	}
}

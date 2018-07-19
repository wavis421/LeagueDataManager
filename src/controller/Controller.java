package controller;

import java.awt.Cursor;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import model.AttendanceModel;
import model.CoursesModel;
import model.DateRangeEvent;
import model.GithubModel;
import model.GraduationModel;
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
	private Pike13Api pike13Api;
	private JFrame parent;
	private ImageIcon icon;

	public Controller(JFrame parent, String awsPassword, String githubToken, String pike13Token, ImageIcon icon) {
		this.parent = parent;
		this.icon = icon;
		sqlDb = new MySqlDatabase(parent, awsPassword, MySqlDatabase.TRACKER_APP_SSH_PORT);
		new MySqlDbLogging(sqlDb);
		new GithubApi(sqlDb, githubToken);
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
		ArrayList<LogDataModel> result = MySqlDbLogging.getLogData();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public void clearDbLogData() {
		MySqlDbLogging.clearLogData();
		if (sqlDb.getConnectError())
			reportConnectError();
	}

	/*
	 * ------- Location Data -------
	 */
	public ArrayList<LocationModel> getLocationList() {
		ArrayList<LocationModel> result = sqlDb.getLocationList();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	/*
	 * ------- Database Queries -------
	 */
	public ArrayList<StudentModel> getActiveStudents() {
		ArrayList<StudentModel> result = sqlDb.getActiveStudents();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<StudentModel> getStudentByClientID(int clientID) {
		ArrayList<StudentModel> result = sqlDb.getStudentByClientID(clientID);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<StudentModel> getStudentsNotInMasterDB() {
		ArrayList<StudentModel> result = sqlDb.getStudentsNotInMasterDB();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<GithubModel> getStudentsWithNoRecentGithub(String sinceDate, int minClassesWithoutGithub) {
		ArrayList<GithubModel> result = sqlDb.getStudentsWithNoRecentGithub(sinceDate, minClassesWithoutGithub);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public void removeInactiveStudents() {
		sqlDb.removeInactiveStudents();
		if (sqlDb.getConnectError())
			reportConnectError();
	}

	public void removeStudentByClientID(int clientID) {
		sqlDb.removeStudentByClientID(clientID);
		if (sqlDb.getConnectError())
			reportConnectError();
	}

	public ArrayList<AttendanceModel> getAllAttendance() {
		ArrayList<AttendanceModel> result = sqlDb.getAllAttendance();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByClassName(String className) {
		ArrayList<AttendanceModel> result = sqlDb.getAttendanceByClassName(className);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByCourseName(String courseName) {
		ArrayList<AttendanceModel> result = sqlDb.getAttendanceByCourseName(courseName);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByClientID(String clientID) {
		ArrayList<AttendanceModel> result = sqlDb.getAttendanceByClientID(clientID);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByClassByDate(String className, String day) {
		ArrayList<AttendanceModel> result = sqlDb.getAttendanceByClassByDate(className, day);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<StudentNameModel> getAllStudentNames() {
		ArrayList<StudentNameModel> result = sqlDb.getAllStudentNames();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<String> getClassNamesByLevel(int level) {
		ArrayList<String> result = sqlDb.getClassNamesByLevel(level);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<ScheduleModel> getClassSchedule() {
		ArrayList<ScheduleModel> result = sqlDb.getClassSchedule();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<CoursesModel> getCourseSchedule() {
		ArrayList<CoursesModel> result = sqlDb.getCourseSchedule(null);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public String getStartDateByClientIdAndLevel(int clientID, String level) {
		return sqlDb.getStartDateByClientIdAndLevel(clientID, level);
	}
	
	public void addGraduationRecord(GraduationModel gradModel) {
		sqlDb.addGraduationRecord(gradModel);
	}

	public ArrayList<InvoiceModel> getInvoices(DateRangeEvent dateRange) {
		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get invoice data from Pike13
		ArrayList<InvoiceModel> invoiceList = pike13Api.getInvoices(dateRange);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());

		if (sqlDb.getConnectError())
			reportConnectError();
		return invoiceList;
	}

	private void reportConnectError() {
		JOptionPane.showMessageDialog(parent,
				"Check your internet connection, and whether  \nanother Student Tracker is already connected. \n",
				"Failure re-connecting to database", JOptionPane.ERROR_MESSAGE, icon);
		sqlDb.clearConnectError();
	}
}

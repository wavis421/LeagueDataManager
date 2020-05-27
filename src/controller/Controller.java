package controller;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import model.CoursesModel;
import model.GraduationModel;
import model.LocationModel;
import model.LogDataModel;
import model.MySqlConnection;
import model.MySqlDatabase;
import model.MySqlDbLogging;
import model.ScheduleModel;
import model.StudentModel;
import model_for_gui.AttendanceModel;
import model_for_gui.GithubModel;
import model_for_gui.MySqlDbForGui;

public class Controller {
	public static final String GRAD_MODEL_IN_SF_FIELD = MySqlDatabase.GRAD_MODEL_IN_SF_FIELD;

	private MySqlDatabase sqlDb;
	private MySqlDbForGui sqlForGui;
	private JFrame parent;
	private ImageIcon icon;

	public Controller(JFrame parent, String awsPassword, ImageIcon icon) {
		this.parent = parent;
		this.icon = icon;
		sqlDb = new MySqlDatabase(parent, awsPassword, MySqlDatabase.TRACKER_APP_SSH_PORT);
		new MySqlDbLogging(sqlDb);
		sqlForGui = new MySqlDbForGui(sqlDb);
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
	
	public void deleteDbLogEntry(int logID) {
		MySqlDbLogging.deleteLogEntry(logID);
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
		ArrayList<StudentModel> result = sqlForGui.getActiveStudents();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<StudentModel> getActiveTAs(String minNumClasses, int minAge, int minLevel) {
		ArrayList<StudentModel> result = sqlForGui.getActiveTAs(minNumClasses, minAge, minLevel);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<StudentModel> getStudentByClientID(int clientID) {
		ArrayList<StudentModel> result = sqlForGui.getStudentByClientID(clientID);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<StudentModel> getStudentsNotInMasterDB() {
		ArrayList<StudentModel> result = sqlForGui.getStudentsNotInMasterDB();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<GithubModel> getStudentsWithNoRecentGithub(String sinceDate, int minClassesWithoutGithub) {
		ArrayList<GithubModel> result = sqlForGui.getStudentsWithNoRecentGithub(sinceDate, minClassesWithoutGithub);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAllAttendance() {
		ArrayList<AttendanceModel> result = sqlForGui.getAllAttendance();
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByClassName(String className, boolean withDate) {
		ArrayList<AttendanceModel> result = sqlForGui.getAttendanceByClassName(className, withDate);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByCourseName(String courseName) {
		ArrayList<AttendanceModel> result = sqlForGui.getAttendanceByCourseName(courseName);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByClientID(String clientID) {
		ArrayList<AttendanceModel> result = sqlForGui.getAttendanceByClientID(clientID);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByClassByDate(String className, String day) {
		ArrayList<AttendanceModel> result = sqlForGui.getAttendanceByClassByDate(className, day);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<AttendanceModel> getAttendanceByCourseByDate(String className, String day) {
		ArrayList<AttendanceModel> result = sqlForGui.getAttendanceByCourseByDate(className, day);
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

	public ArrayList<ScheduleModel> getWeeklyClassDetails(boolean[] dowSelectList) {
		ArrayList<ScheduleModel> result = sqlForGui.getClassDetails(dowSelectList);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}
	
	public String getClassDowAndTime(String className) {
		return sqlForGui.getClassDowAndTime(className);
	}

	public ArrayList<CoursesModel> getCourseSchedule() {
		ArrayList<CoursesModel> result = sqlDb.getCourseSchedule(null);
		if (sqlDb.getConnectError())
			reportConnectError();
		return result;
	}

	public ArrayList<GraduationModel> getAllGradRecords() {
		return sqlDb.getAllGradRecords();
	}

	private void reportConnectError() {
		JOptionPane.showMessageDialog(parent,
				"Check your internet connection, and whether  \nanother Student Tracker is already connected. \n",
				"Failure re-connecting to database", JOptionPane.ERROR_MESSAGE, icon);
		sqlDb.setConnectError(false);
	}
}

package controller;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFrame;

import model.ActivityEventModel;
import model.ActivityModel;
import model.LogDataModel;
import model.MySqlDatabase;
import model.ScheduleModel;
import model.StudentImportModel;
import model.StudentModel;
import model.StudentNameModel;

public class Controller {
	// CSV Student Table indices
	private static final int CSV_STUDENT_LOCATION_IDX = 0;
	private static final int CSV_STUDENT_STARTDATE_IDX = 1;
	private static final int CSV_STUDENT_FIRSTNAME_IDX = 2;
	private static final int CSV_STUDENT_LASTNAME_IDX = 3;
	private static final int CSV_STUDENT_CLIENTID_IDX = 4;
	private static final int CSV_STUDENT_GENDER_IDX = 5;
	private static final int CSV_STUDENT_GRAD_YEAR_IDX = 6;
	private static final int CSV_STUDENT_GITHUB_IDX = 7;

	// CSV Enrollment table indices
	private static final int CSV_ACTIVITY_STUDENT_NAME_IDX = 0;
	private static final int CSV_ACTIVITY_SERVICE_DATE_IDX = 1;
	private static final int CSV_ACTIVITY_EVENT_NAME_IDX = 2;
	private static final int CSV_ACTIVITY_CLIENTID_IDX = 3;

	// Different port than League Student Tracker to allow simultaneous connects
	private static final int LOCAL_SSH_PORT = 5000;

	private MySqlDatabase sqlDb;
	private GitApiController gitController;
	private Pike13ApiController pike13Controller;
	private JFrame parent;

	public Controller(JFrame parent, String awsPassword, String githubToken, String pike13Token) {
		this.parent = parent;
		sqlDb = new MySqlDatabase(parent, awsPassword, LOCAL_SSH_PORT);
		gitController = new GitApiController(sqlDb, githubToken);
		pike13Controller = new Pike13ApiController(sqlDb, pike13Token);
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

	public ArrayList<ActivityModel> getAllActivities() {
		return sqlDb.getAllActivities();
	}

	public ArrayList<ActivityModel> getActivitiesByClassName(String className) {
		return sqlDb.getActivitiesByClassName(className);
	}

	public ArrayList<ActivityModel> getActivitiesByStudentName(StudentNameModel studentName) {
		return sqlDb.getActivitiesByStudentName(studentName);
	}

	public ArrayList<ActivityModel> getActivitiesByClientID(String clientID) {
		return sqlDb.getActivitiesByClientID(clientID);
	}

	public ArrayList<StudentNameModel> getAllStudentNames() {
		return sqlDb.getAllStudentNames();
	}

	public ArrayList<String> getAllClassNames() {
		return sqlDb.getAllClassNames();
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
	public void importStudentsFromFile(File file) {
		Path pathToFile = Paths.get(file.getAbsolutePath());
		String line = "";
		ArrayList<StudentImportModel> studentList = new ArrayList<StudentImportModel>();

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// CSV file has the following columns:
		// Home Location, First Visit Date, First Name, Last Name,
		// Client ID, gender, high school grad year, Github user name
		try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
			// Toss header line
			line = br.readLine();
			if (line != null)
				line = br.readLine();

			while (line != null) {
				// Add student to list
				String[] fields = line.split(",");

				studentList.add(new StudentImportModel(Integer.parseInt(fields[CSV_STUDENT_CLIENTID_IDX]),
						fields[CSV_STUDENT_LASTNAME_IDX], fields[CSV_STUDENT_FIRSTNAME_IDX],
						fields[CSV_STUDENT_GITHUB_IDX], fields[CSV_STUDENT_GENDER_IDX],
						fields[CSV_STUDENT_STARTDATE_IDX], fields[CSV_STUDENT_LOCATION_IDX],
						fields[CSV_STUDENT_GRAD_YEAR_IDX]));

				line = br.readLine();
			}
			br.close();

			// Update changes in database
			if (studentList.size() > 0)
				sqlDb.importStudents(studentList);

		} catch (IOException e) {
			sqlDb.insertLogData(LogDataModel.FILE_IMPORT_ERROR, null, 0,
					" for file '" + pathToFile + "': " + e.getMessage());
		}

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
	}

	public void importStudentsFromPike13() {
		sqlDb.insertLogData(LogDataModel.STARTING_STUDENT_IMPORT, new StudentNameModel("", "", false), 0, " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get data from Pike13
		ArrayList<StudentImportModel> studentList = pike13Controller.getClients();

		// Update changes in database
		if (studentList.size() > 0)
			sqlDb.importStudents(studentList);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		sqlDb.insertLogData(LogDataModel.STUDENT_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0, " ***");
	}

	public void importActivitiesFromFile(File file) {
		Path pathToFile = Paths.get(file.getAbsolutePath());
		String line = "";
		ArrayList<ActivityEventModel> eventList = new ArrayList<ActivityEventModel>();

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// CSV file has the following columns:
		// Student name, service date, event name, clientID, Schedule ID
		try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
			// Toss header line
			line = br.readLine();
			if (line != null)
				line = br.readLine();

			while (line != null) {
				// Read next line of CSV file and split into columns
				String[] fields = line.split(",");

				// Pre-process some columns
				String serviceDate = fields[CSV_ACTIVITY_SERVICE_DATE_IDX];
				String eventName = fields[CSV_ACTIVITY_EVENT_NAME_IDX];
				int paren = eventName.indexOf('(');
				if (paren > 0)
					eventName = eventName.substring(0, paren);

				// Create new student
				if (!eventName.equals("") && !eventName.equals("\"\"") && !serviceDate.equals("")) {
					eventList.add(new ActivityEventModel(Integer.parseInt(fields[CSV_ACTIVITY_CLIENTID_IDX]),
							fields[CSV_ACTIVITY_STUDENT_NAME_IDX], fields[CSV_ACTIVITY_SERVICE_DATE_IDX], eventName));
				}

				line = br.readLine();
			}
			br.close();

			// Update changes in database
			if (eventList.size() > 0)
				sqlDb.importActivities(eventList);

		} catch (IOException e) {
			sqlDb.insertLogData(LogDataModel.FILE_IMPORT_ERROR, null, 0,
					" for file '" + pathToFile + "': " + e.getMessage());
		}

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
	}

	public void importActivitiesFromPike13(String startDate) {
		sqlDb.insertLogData(LogDataModel.STARTING_ATTENDANCE_IMPORT, new StudentNameModel("", "", false), 0,
				" starting from " + startDate.substring(0, 10) + " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get data from Pike13
		ArrayList<ActivityEventModel> eventList = pike13Controller.getEnrollment(startDate);

		// Update changes in database
		if (eventList.size() > 0)
			sqlDb.importActivities(eventList);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		sqlDb.insertLogData(LogDataModel.ATTENDANCE_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0, " ***");
	}

	public void importScheduleFromPike13() {
		// TODO: Add a log entry once this is fully integrated

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get data from Pike13
		ArrayList<ScheduleModel> eventList = pike13Controller.getSchedule();

		// Update changes in database
		if (eventList.size() > 0)
			sqlDb.importSchedule(eventList);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());

		// TODO: Add a log entry once this is fully integrated
	}

	public void importGithubComments(String startDate) {
		boolean result;

		sqlDb.insertLogData(LogDataModel.STARTING_GITHUB_IMPORT, new StudentNameModel("", "", false), 0,
				" starting from " + startDate.substring(0, 10) + " ***");

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		result = gitController.importGithubComments(startDate);
		if (result)
			gitController.importGithubCommentsByLevel(0, startDate);

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
		if (result)
			sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_COMPLETE, new StudentNameModel("", "", false), 0, " ***");
		else
			sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_ABORTED, new StudentNameModel("", "", false), 0,
					": Github API rate limit exceeded ***");
	}

	public void importAllDatabases(String startDate) {
		importStudentsFromPike13();
		importActivitiesFromPike13(startDate);
		importGithubComments(startDate);
	}
}

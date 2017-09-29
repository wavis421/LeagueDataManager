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
import javax.swing.JOptionPane;

import model.ActivityModel;
import model.LogDataModel;
import model.MySqlDatabase;
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
	private static final int CSV_ACTIVITY_SERVICE_DATE_IDX = 1;
	private static final int CSV_ACTIVITY_EVENT_NAME_IDX = 2;
	private static final int CSV_ACTIVITY_CLIENTID_IDX = 3;

	private MySqlDatabase sqlDb;
	private JFrame parent;

	public Controller(JFrame parent) {
		this.parent = parent;
		sqlDb = new MySqlDatabase(parent);
	}

	/*
	 * ------- Database Connections -------
	 */
	public void disconnectDatabase() {
		sqlDb.disconnectDatabase();
	}

	/*
	 * ------- Logging Activity -------
	 */
	public ArrayList<LogDataModel> getDbLogData() {
		return sqlDb.getDbLogData();
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
		sqlDb.clearDbLogData();
		sqlDb.removeInactiveStudents();
		
		if (sqlDb.getDbLogData().size() > 0)
			JOptionPane.showMessageDialog(parent, "Please view Log Data for list of students removed");
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

	/*
	 * ------- File import/export -------
	 */
	public void importStudentsFromFile(File file) {
		Path pathToFile = Paths.get(file.getAbsolutePath());
		String line = "";
		ArrayList<StudentImportModel> studentList = new ArrayList<StudentImportModel>();

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Clear log data
		sqlDb.clearDbLogData();

		// CSV file has the following columns:
		// Birth date, Completed Visits, Home Location, First Visit Date, First Name,
		// Last Name, Client ID, gender, year graduating high school, Github account
		// name
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
			System.out.println("Error line: " + line);
			// e.printStackTrace();
		}

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());

		// Report if log data collected during import
		if (sqlDb.getDbLogData().size() > 0)
			JOptionPane.showMessageDialog(parent, "Please view Log Data -- some errors/warnings have occurred");
	}

	public void importActivitiesFromFile(File file) {
		Path pathToFile = Paths.get(file.getAbsolutePath());
		String line = "";

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
					sqlDb.addActivity(Integer.parseInt(fields[CSV_ACTIVITY_CLIENTID_IDX]),
							fields[CSV_ACTIVITY_SERVICE_DATE_IDX], eventName, "");
				}

				line = br.readLine();
			}
			br.close();

		} catch (IOException e) {
			System.out.println("Error line: " + line);
			// e.printStackTrace();
		}

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
	}
}

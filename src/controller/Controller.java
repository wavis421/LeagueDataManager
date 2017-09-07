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

import model.ActivityModel;
import model.MySqlDatabase;
import model.StudentModel;

public class Controller {
	// TODO: Get rid of NumVisits when exporting from FrontDesk
	// CSV Student Table indices
	private static final int CSV_STUDENT_LOCATION_IDX = 2;
	private static final int CSV_STUDENT_STARTDATE_IDX = 3;
	private static final int CSV_STUDENT_FIRSTNAME_IDX = 4;
	private static final int CSV_STUDENT_LASTNAME_IDX = 5;
	private static final int CSV_STUDENT_CLIENTID_IDX = 6;
	private static final int CSV_STUDENT_GENDER_IDX = 7;
	private static final int CSV_STUDENT_GRAD_YEAR_IDX = 8;
	private static final int CSV_STUDENT_GITHUB_IDX = 9;

	// CSV Enrollment table indices
	private static final int CSV_ACTIVITY_SERVICE_DATE_IDX = 1;
	private static final int CSV_ACTIVITY_EVENT_NAME_IDX = 4;
	private static final int CSV_ACTIVITY_CLIENTID_IDX = 6;

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
	 * ------- Database Queries -------
	 */
	public ArrayList<StudentModel> getAllStudents() {
		return sqlDb.getAllStudents();
	}

	public ArrayList<ActivityModel> getAllActivities() {
		return sqlDb.getAllActivities();
	}

	/*
	 * ------- File save/restore items -------
	 */
	public void importStudentsFromFile(File file) {
		// TODO: Fix this to get path from user
		Path pathToFile = Paths.get("C:\\Users\\Wendy\\workspace\\LeagueDataManager\\" + file.getName());
		String line = "";

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// CSV file has the following columns:
		// Birth date, Completed Visits, Home Location, First Visit Date, First Name,
		// Last Name, Client ID, gender, year graduating high school, Github account
		// name
		try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
			line = br.readLine();

			while (line != null) {
				// Create new student
				String[] fields = line.split(",");

				sqlDb.addStudent(Integer.parseInt(fields[CSV_STUDENT_CLIENTID_IDX]), fields[CSV_STUDENT_LASTNAME_IDX],
						fields[CSV_STUDENT_FIRSTNAME_IDX], fields[CSV_STUDENT_GITHUB_IDX], fields[CSV_STUDENT_GENDER_IDX],
						fields[CSV_STUDENT_STARTDATE_IDX], fields[CSV_STUDENT_LOCATION_IDX], fields[CSV_STUDENT_GRAD_YEAR_IDX]);

				line = br.readLine();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error line: " + line);
			// e.printStackTrace();
		}

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
	}

	public void importActivitiesFromFile(File file) {
		// TODO: Fix this to get path from user
		Path pathToFile = Paths.get("C:\\Users\\Wendy\\workspace\\LeagueDataManager\\" + file.getName());
		String line = "";

		// Set cursor to "wait" cursor
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// CSV file has the following columns:
		// Student name, service date, dow, time, event name, visitID, clientID
		try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
			line = br.readLine();

			while (line != null) {
				// Create new student
				String[] fields = line.split(",");

				String serviceDate = fields[CSV_ACTIVITY_SERVICE_DATE_IDX];
				int paren = fields[CSV_ACTIVITY_EVENT_NAME_IDX].indexOf('(');
				if (paren > 0)
					fields[CSV_ACTIVITY_EVENT_NAME_IDX] = fields[CSV_ACTIVITY_EVENT_NAME_IDX].substring(0, paren);
				
				if (!fields[CSV_ACTIVITY_EVENT_NAME_IDX].equals("") && !fields[CSV_ACTIVITY_EVENT_NAME_IDX].equals("\"\"") && 
						!fields[CSV_ACTIVITY_EVENT_NAME_IDX].contains("iAROC") && !fields[CSV_ACTIVITY_EVENT_NAME_IDX].contains("iARoC") &&
						!fields[CSV_ACTIVITY_EVENT_NAME_IDX].contains("Intro to Java Workshop") && 
						!serviceDate.equals("") && serviceDate.compareTo("2017-07-01") > 0) {
					
					sqlDb.addActivity(Integer.parseInt(fields[CSV_ACTIVITY_CLIENTID_IDX]), fields[CSV_ACTIVITY_SERVICE_DATE_IDX],
							fields[CSV_ACTIVITY_EVENT_NAME_IDX], "");
				}

				line = br.readLine();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error line: " + line);
			// e.printStackTrace();
		}

		// Set cursor back to default
		parent.setCursor(Cursor.getDefaultCursor());
	}
}

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

import model.MySqlDatabase;
import model.StudentModel;

public class Controller {
	// TODO: Get rid of NumVisits when exporting from FrontDesk
	private static final int CSV_BIRTHDATE_IDX = 0;
	private static final int CSV_NUMVISITS_IDX = 1;
	private static final int CSV_HOMELOCATION_IDX = 2;
	private static final int CSV_STARTDATE_IDX = 3;
	private static final int CSV_FIRSTNAME_IDX = 4;
	private static final int CSV_LASTNAME_IDX = 5;
	private static final int CSV_CLIENTID_IDX = 6;
	private static final int CSV_GENDER_IDX = 7;
	private static final int CSV_GRAD_YEAR_IDX = 8;
	private static final int CSV_GITHUB_IDX = 9;

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

				sqlDb.addStudent(Integer.parseInt(fields[CSV_CLIENTID_IDX]), fields[CSV_LASTNAME_IDX],
						fields[CSV_FIRSTNAME_IDX], fields[CSV_GITHUB_IDX], fields[CSV_GENDER_IDX],
						fields[CSV_STARTDATE_IDX], fields[CSV_HOMELOCATION_IDX], fields[CSV_GRAD_YEAR_IDX]);

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

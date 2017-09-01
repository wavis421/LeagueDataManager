package controller;

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
	 * ------- Database  Queries -------
	 */
	public ArrayList<StudentModel> getAllStudents() {
		return sqlDb.getAllStudents();
	}
	
	public void addStudent(String lastName, String firstName, String githubName) {
		sqlDb.addStudent(lastName, firstName, githubName);
	}
	
	/*
	 * ------- File save/restore items -------
	 */
	public void importStudentsFromFile(File file) {
		Path pathToFile = Paths.get(file.getName());
		
		try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
			String line = br.readLine();
			while (line != null) {
				// Create new student
				String[] fields = line.split(",");
				sqlDb.addStudent(fields[0], fields[1], fields[2]);
				
				line = br.readLine();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import sun.net.www.content.image.jpeg;

public class MySqlDatabase {
	private static Connection dbConnection = null;
	private JFrame parent;

	public MySqlDatabase(JFrame parent) {
		this.parent = parent;

		// Make initial connection to database
		connectDatabase();
	}

	/*
	 * ------- Database Connections -------
	 */
	private static final int MAX_CONNECTION_ATTEMPTS = 3;

	private void connectDatabase() {
		int connectAttempts = 0;
		while (true) {
			connectAttempts++;
			try {
				dbConnection = MySqlConnection.connectToServer(parent, "LeagueData", "tester421", "Rwarwe310");
			} catch (SQLException e) {
				// Error handling performed in connectToServer
			}

			if (dbConnection == null) {
				int answer = JOptionPane.showConfirmDialog(null, "Do you want to retry?",
						"Failure connecting to database", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (answer == JOptionPane.NO_OPTION) {
					// Exit program
					// TODO: Figure out how to dispose of MainFrame
					System.exit(0);
				} else if (connectAttempts >= MAX_CONNECTION_ATTEMPTS) {
					JOptionPane.showMessageDialog(null,
							"Exceeded maximum connection attempts.\nPlease try again later.");
					// Exit program
					// TODO: Figure out how to dispose of MainFrame
					System.exit(0);
				}
			} else
				break;
		}
	}

	public void disconnectDatabase() {
		if (dbConnection != null) {
			MySqlConnection.closeConnections();
			dbConnection = null;
		}
	}

	/*
	 * ------- Database Queries -------
	 */
	public ArrayList<StudentModel> getAllStudents() {
		ArrayList<StudentModel> nameList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students ORDER BY LastName, FirstName;");
				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					nameList.add(new StudentModel(0, result.getString("LastName"), result.getString("FirstName"),
							result.getString("GithubName")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (SQLException e) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}
			}
		}
		return nameList;
	}
	
	public StudentModel getStudentByGithubName(String githubName) {
		StudentModel student = null;
		
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students WHERE GithubName=?;");
				selectStmt.setString(1, githubName);
				
				ResultSet result = selectStmt.executeQuery();
				if (result.next()) {
					student = new StudentModel(result.getInt("StudentId"), 
							result.getString("LastName"), result.getString("FirstName"),
							result.getString("GithubName"));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (SQLException e) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}
			}
		}
		return student;
	}
	
	public void addStudent(String lastName, String firstName, String githubName) {
		for (int i = 0; i < 2; i++) {
			if (getStudentByGithubName(githubName) != null) {
				// TODO: Add this to log file
				System.out.println("Student with github name " + githubName + " already exists");
				break;
			}
			try {
				PreparedStatement addStudentStmt = dbConnection.prepareStatement(
						"INSERT INTO Students (lastName, firstName, githubName) "
								+ "VALUES (?, ?, ?);");

				int col = 1;
				addStudentStmt.setString(col++, lastName);
				addStudentStmt.setString(col++, firstName);
				addStudentStmt.setString(col++, githubName);

				addStudentStmt.executeUpdate();
				addStudentStmt.close();
				break;

			} catch (SQLException e) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}
			}
		}
	}
}

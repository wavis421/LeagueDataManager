package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

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
					// TODO: Should have 1 place where program exits
					// TODO: Add cleanup of MainFrame
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
					nameList.add(new StudentModel(result.getInt("StudentID"), result.getInt("ClientID"),
							result.getString("LastName"), result.getString("FirstName"), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database (" + i + "): " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				System.out.println("Get Student database error: " + e2.getMessage());
				e2.printStackTrace();
				break;
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
					student = new StudentModel(result.getInt("StudentID"), result.getInt("ClientID"),
							result.getString("LastName"), result.getString("FirstName"), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("HomeLocation"),
							result.getInt("GradYear"));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database: " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				System.out.println("Get Student database error: " + e2.getMessage());
				break;
			}
		}
		return student;
	}

	public int getStudentIDFromClientID(int clientID) {
		int studentID = -1;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT StudentID FROM Students WHERE ClientID=?;");
				selectStmt.setInt(1, clientID);

				ResultSet result = selectStmt.executeQuery();
				if (result.next())
					studentID = result.getInt("StudentID");

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database: " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				System.out.println("Get Student ID database error: " + e2.getMessage());
				break;
			}
		}
		return studentID;
	}

	public void addStudent(int clientID, String lastName, String firstName, String githubName, String gender,
			String firstVisitDate, String homeLocation, String gradYear) {

		int gradYearAsInt = 0;

		if (githubName.equals("") || githubName.equals("\"\"")) {
			System.out.println(firstName + " " + lastName + "(" + clientID + ") does not have a github user name");
			githubName = null;
		}
		if (gradYear != null && !gradYear.equals("") && !gradYear.equals("\"\""))
			gradYearAsInt = Integer.parseInt(gradYear);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addStudentStmt = dbConnection.prepareStatement(
						"INSERT INTO Students (ClientID, LastName, FirstName, GithubName, Gender, StartDate, Location, GradYear) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

				int col = 1;
				addStudentStmt.setInt(col++, clientID);
				addStudentStmt.setString(col++, lastName);
				addStudentStmt.setString(col++, firstName);
				addStudentStmt.setString(col++, githubName);
				addStudentStmt.setInt(col++, GenderModel.convertStringToGender(gender));
				if (!firstVisitDate.equals(""))
					addStudentStmt.setDate(col++, java.sql.Date.valueOf(firstVisitDate));
				else
					addStudentStmt.setDate(col++, null);
				addStudentStmt.setInt(col++, LocationModel.convertStringToLocation(homeLocation));
				addStudentStmt.setInt(col++, gradYearAsInt);

				addStudentStmt.executeUpdate();
				addStudentStmt.close();
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database: " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Student already exists, so update instead
				updateStudent(clientID, lastName, firstName, githubName, firstVisitDate, homeLocation, gradYearAsInt);
				break;

			} catch (SQLException e3) {
				System.out.println("Add student database failure: " + e3.getMessage());
				break;
			}
		}
	}

	private void updateStudent(int clientID, String lastName, String firstName, String githubName,
			String firstVisitDate, String homeLocation, int gradYear) {

		PreparedStatement updateStudentStmt;
		try {
			updateStudentStmt = dbConnection.prepareStatement(
					"UPDATE Students SET LastName=?, FirstName=?, GithubName=?, StartDate=?, Location=?, GradYear=? "
							+ "WHERE ClientID=?;");

			int col = 1;
			updateStudentStmt.setString(col++, lastName);
			updateStudentStmt.setString(col++, firstName);
			updateStudentStmt.setString(col++, githubName);
			if (!firstVisitDate.equals(""))
				updateStudentStmt.setDate(col++, java.sql.Date.valueOf(firstVisitDate));
			else {
				updateStudentStmt.setDate(col++, null);
				System.out.println(firstName + " " + lastName + " has no first Visit Date");
			}
			updateStudentStmt.setInt(col++, LocationModel.convertStringToLocation(homeLocation));
			updateStudentStmt.setInt(col++, gradYear);
			updateStudentStmt.setInt(col++, clientID);

			updateStudentStmt.executeUpdate();
			updateStudentStmt.close();
			return;

		} catch (SQLException e) {
			System.out.println("Update student database failure: " + e.getMessage());
		}
	}

	public ArrayList<ActivityModel> getAllActivities() {
		ArrayList<ActivityModel> activityList = new ArrayList<ActivityModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.StudentID = Students.StudentID "
								+ "ORDER BY EventName, ServiceDate, Students.LastName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					activityList
							.add(new ActivityModel(result.getInt("Students.ClientID"),
									result.getString("Students.FirstName") + " "
											+ result.getString("Students.LastName"),
									result.getDate("ServiceDate"), result.getString("EventName"),
									result.getString("Comments")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database (" + i + "): " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				System.out.println("Get Activity database error: " + e2.getMessage());
				e2.printStackTrace();
				break;
			}
		}
		return activityList;
	}

	public void addActivity(int clientID, String serviceDate, String eventName, String comments) {
		for (int i = 0; i < 2; i++) {
			int studentID = getStudentIDFromClientID(clientID);
			if (studentID < 0)
				break;

			try {
				PreparedStatement addActivityStmt = dbConnection.prepareStatement("INSERT INTO Activities ("
						+ "StudentID, ServiceDate, EventName, Comments) VALUES (" + "?, ?, ?, ?);");

				int col = 1;
				addActivityStmt.setInt(col++, studentID);
				addActivityStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));
				addActivityStmt.setString(col++, eventName);
				addActivityStmt.setString(col++, comments);

				addActivityStmt.executeUpdate();
				addActivityStmt.close();
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database: " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Activity already exists
				break;

			} catch (SQLException e3) {
				System.out.println(
						"Add activity failure for ID=" + clientID + ", event=" + eventName + ": " + e3.getMessage());
				break;
			}
		}
	}
}

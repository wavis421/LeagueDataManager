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
	private ArrayList<LogDataModel> logData = new ArrayList<LogDataModel>();

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
	 * ------- Logging Activity -------
	 */
	public void clearDbLogData() {
		logData.clear();
	}

	public ArrayList<LogDataModel> getDbLogData() {
		return (ArrayList<LogDataModel>) logData.clone();
	}

	/*
	 * ------- Student Database Queries -------
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
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							result.getString("GithubName"), result.getInt("Gender"), result.getDate("StartDate"),
							result.getInt("Location"), result.getInt("GradYear")));
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
	
	public void removeStudentByClientID(int clientID) {
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement deleteStudentStmt = dbConnection
						.prepareStatement("DELETE FROM Students WHERE ClientID=?;");

				// Delete student
				deleteStudentStmt.setInt(1, clientID);
				deleteStudentStmt.executeUpdate();
				deleteStudentStmt.close();
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database (" + i + "): " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				System.out.println("Remove Student database error: " + e2.getMessage());
				e2.printStackTrace();
				break;
			}
		}
	}

	public ArrayList<StudentModel> getStudentsNotInMasterDB() {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Students WHERE NOT isInMasterDb " + "ORDER BY LastName, FirstName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					studentList.add(new StudentModel(result.getInt("StudentID"), result.getInt("ClientID"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							result.getString("GithubName"), result.getInt("Gender"), result.getDate("StartDate"),
							result.getInt("Location"), result.getInt("GradYear")));
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
		return studentList;
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
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							result.getString("GithubName"), result.getInt("Gender"), result.getDate("StartDate"),
							result.getInt("Location"), result.getInt("GradYear"));
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

	public ArrayList<StudentModel> getStudentByClientID(int clientID) {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students WHERE ClientID=?;");
				selectStmt.setInt(1, clientID);

				ResultSet result = selectStmt.executeQuery();
				if (result.next()) {
					studentList.add(new StudentModel(result.getInt("StudentID"), result.getInt("ClientID"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							result.getString("GithubName"), result.getInt("Gender"), result.getDate("StartDate"),
							result.getInt("Location"), result.getInt("GradYear")));
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
		return studentList;
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

		int gradYearAsInt = 0, homeLocNum = 0;

		if (githubName.equals("") || githubName.equals("\"\"")) {
			logData.add(new LogDataModel(LogDataModel.MISSING_GITHUB_NAME,
					new StudentNameModel(firstName, lastName, true), clientID));
			githubName = null;
		} else
			githubName = parseGithubName(githubName);

		if (gradYear == null || gradYear.equals("") || gradYear.equals("\"\""))
			logData.add(new LogDataModel(LogDataModel.MISSING_GRAD_YEAR,
					new StudentNameModel(firstName, lastName, true), clientID));
		else
			gradYearAsInt = Integer.parseInt(gradYear);

		if (firstVisitDate.equals(""))
			logData.add(new LogDataModel(LogDataModel.MISSING_FIRST_VISIT_DATE,
					new StudentNameModel(firstName, lastName, true), clientID));

		homeLocNum = LocationModel.convertStringToLocation(homeLocation);
		if (homeLocNum == 0)
			logData.add(new LogDataModel(LogDataModel.MISSING_HOME_LOCATION,
					new StudentNameModel(firstName, lastName, true), clientID));

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addStudentStmt = dbConnection.prepareStatement(
						"INSERT INTO Students (ClientID, LastName, FirstName, GithubName, Gender, StartDate, Location, GradYear, isInMasterDb) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1);");

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
				addStudentStmt.setInt(col++, homeLocNum);
				addStudentStmt.setInt(col++, gradYearAsInt);

				addStudentStmt.executeUpdate();
				addStudentStmt.close();

				if (githubName == null)
					logData.add(new LogDataModel(LogDataModel.ADD_NEW_STUDENT_NO_GITHUB,
							new StudentNameModel(firstName, lastName, true), clientID));
				else
					logData.add(new LogDataModel(LogDataModel.ADD_NEW_STUDENT,
							new StudentNameModel(firstName, lastName, true), clientID));
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database: " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Student already exists, so update instead
				updateStudent(clientID, lastName, firstName, githubName, firstVisitDate, homeLocNum, gradYearAsInt);
				break;

			} catch (SQLException e3) {
				System.out.println("Add student database failure: " + e3.getMessage());
				break;
			}
		}
	}

	public void markAllStudentsAsNotInDb() {
		// This is used prior to a Student DB import. The Student DB field
		// 'isInMasterDb' is cleared to detect obsolete entries after import.
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateStudentStmt = dbConnection
						.prepareStatement("UPDATE Students SET isInMasterDb=0;");
				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();
				return;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database: " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e) {
				System.out.println("Update student database failure: " + e.getMessage());
			}
		}
	}

	private void updateStudent(int clientID, String lastName, String firstName, String githubName,
			String firstVisitDate, int homeLocNum, int gradYear) {

		PreparedStatement updateStudentStmt;
		try {
			updateStudentStmt = dbConnection.prepareStatement(
					"UPDATE Students SET LastName=?, FirstName=?, GithubName=?, StartDate=?, Location=?, GradYear=?, isInMasterDb=? "
							+ "WHERE ClientID=?;");

			int col = 1;
			updateStudentStmt.setString(col++, lastName);
			updateStudentStmt.setString(col++, firstName);
			updateStudentStmt.setString(col++, githubName);
			if (!firstVisitDate.equals(""))
				updateStudentStmt.setDate(col++, java.sql.Date.valueOf(firstVisitDate));
			else {
				updateStudentStmt.setDate(col++, null);
			}
			updateStudentStmt.setInt(col++, homeLocNum);
			updateStudentStmt.setInt(col++, gradYear);
			updateStudentStmt.setInt(col++, 1);
			updateStudentStmt.setInt(col, clientID);

			updateStudentStmt.executeUpdate();
			updateStudentStmt.close();
			return;

		} catch (SQLException e) {
			System.out.println("Update student database failure: " + e.getMessage());
		}
	}

	private String parseGithubName(String githubName) {
		int index = githubName.indexOf('(');
		if (index != -1)
			githubName = githubName.substring(0, index);
		githubName.trim();

		return githubName;
	}

	/*
	 * ------- Activity Database Queries -------
	 */
	public ArrayList<ActivityModel> getAllActivities() {
		ArrayList<ActivityModel> activityList = new ArrayList<ActivityModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.StudentID = Students.StudentID "
								+ "ORDER BY ServiceDate DESC, EventName, Students.LastName, Students.FirstName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					activityList.add(new ActivityModel(result.getInt("Students.ClientID"),
							new StudentNameModel(result.getString("Students.FirstName"),
									result.getString("Students.LastName"), result.getBoolean("isInMasterDb")),
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

	public ArrayList<ActivityModel> getActivitiesByClassName(String className) {
		ArrayList<ActivityModel> activityList = new ArrayList<ActivityModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.StudentID = Students.StudentID AND "
								+ "EventName='" + className
								+ "' ORDER BY ServiceDate DESC, EventName, Students.LastName, Students.FirstName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					activityList.add(new ActivityModel(result.getInt("Students.ClientID"),
							new StudentNameModel(result.getString("Students.FirstName"),
									result.getString("Students.LastName"), result.getBoolean("isInMasterDb")),
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

	public ArrayList<ActivityModel> getActivitiesByStudentName(StudentNameModel studentName) {
		ArrayList<ActivityModel> activityList = new ArrayList<ActivityModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.StudentID = Students.StudentID AND "
								+ "FirstName='" + studentName.getFirstName() + "' AND " + "LastName='"
								+ studentName.getLastName()
								+ "' ORDER BY ServiceDate DESC, EventName, Students.LastName, Students.FirstName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					activityList.add(new ActivityModel(result.getInt("Students.ClientID"),
							new StudentNameModel(result.getString("Students.FirstName"),
									result.getString("Students.LastName"), result.getBoolean("isInMasterDb")),
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

	public ArrayList<String> getAllClassNames() {
		ArrayList<String> classList = new ArrayList<String>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT EventName "
						+ "FROM Activities WHERE EventName != '' GROUP BY EventName ORDER BY EventName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					classList.add(result.getString("EventName"));
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
				System.out.println("Get Class Name database error: " + e2.getMessage());
				e2.printStackTrace();
				break;
			}
		}
		return classList;
	}

	public ArrayList<StudentNameModel> getAllStudentNames() {
		ArrayList<StudentNameModel> studentList = new ArrayList<StudentNameModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT LastName, FirstName, isInMasterDb FROM Students ORDER BY LastName, FirstName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					studentList.add(new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
							result.getBoolean("isInMasterDb")));
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
				System.out.println("Get Student Name database error: " + e2.getMessage());
				e2.printStackTrace();
				break;
			}
		}
		return studentList;
	}

	public void addActivity(int clientID, String serviceDate, String eventName, String comments) {
		for (int i = 0; i < 2; i++) {
			int studentID = getStudentIDFromClientID(clientID);
			if (studentID < 0) {
				System.out.println("No student for Client ID " + clientID);
				break;
			}

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
				// Activity already exists, so update
				if (!comments.equals(""))
					updateActivity(studentID, serviceDate, eventName, comments);
				break;

			} catch (SQLException e3) {
				System.out.println(
						"Add activity failure for ID=" + clientID + ", event=" + eventName + ": " + e3.getMessage());
				break;
			}
		}
	}

	private void updateActivity(int studentID, String serviceDate, String eventName, String comments) {
		PreparedStatement updateActivityStmt;
		try {
			// The only field that should be updated is the comments
			updateActivityStmt = dbConnection.prepareStatement(
					"UPDATE Activities SET Comments=? WHERE StudentID=? AND ServiceDate=? AND EventName=?;");

			int col = 1;
			updateActivityStmt.setString(col++, comments);
			updateActivityStmt.setInt(col++, studentID);
			updateActivityStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));
			updateActivityStmt.setString(col++, eventName);

			updateActivityStmt.executeUpdate();
			updateActivityStmt.close();
			System.out.println("Updated student ID = " + studentID + ", comments: " + comments);
			return;

		} catch (SQLException e) {
			System.out.println("Update activities database failure: " + e.getMessage());
		}
	}
}

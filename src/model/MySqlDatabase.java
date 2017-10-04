package model;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import gui.MainFrame;

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
					MainFrame.shutdown();

				} else if (connectAttempts >= MAX_CONNECTION_ATTEMPTS) {
					JOptionPane.showMessageDialog(null,
							"Exceeded maximum connection attempts.\nPlease try again later.");
					// Exit program
					MainFrame.shutdown();
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
					nameList.add(new StudentModel(result.getInt("ClientID"),
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
						"SELECT * FROM Students WHERE NOT isInMasterDb ORDER BY LastName, FirstName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					studentList.add(new StudentModel(result.getInt("ClientID"),
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

	public void removeInactiveStudents() {
		// Remove any student not in master DB who have no activity data
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students WHERE NOT isInMasterDb AND "
								+ "(SELECT COUNT(*) FROM Activities WHERE Activities.ClientID = Students.ClientID) = 0;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					logData.add(new LogDataModel(LogDataModel.REMOVE_INACTIVE_STUDENT,
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), false),
							result.getInt("ClientID"), ""));

					removeStudentByClientID(result.getInt("ClientID"));
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
					student = new StudentModel(result.getInt("ClientID"),
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
					studentList.add(new StudentModel(result.getInt("ClientID"),
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

	public void importStudents(ArrayList<StudentImportModel> importList) {
		ArrayList<StudentImportModel> dbList = getAllStudentsAsImportData();
		int dbListIdx = 0;
		int dbListSize = dbList.size();

		StudentImportModel dbStudent;
		for (int i = 0; i < importList.size(); i++) {
			StudentImportModel importStudent = importList.get(i);

			// Log any missing data
			if (importStudent.getGithubName().equals("")) {
				logData.add(new LogDataModel(LogDataModel.MISSING_GITHUB_NAME,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), ""));
			}

			if (importStudent.getGradYear() == 0)
				logData.add(new LogDataModel(LogDataModel.MISSING_GRAD_YEAR,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), ""));

			if (importStudent.getStartDate().equals(""))
				logData.add(new LogDataModel(LogDataModel.MISSING_FIRST_VISIT_DATE,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), ""));

			if (importStudent.getHomeLocation() == LocationModel.CLASS_LOCATION_UNKNOWN)
				logData.add(new LogDataModel(LogDataModel.MISSING_HOME_LOCATION,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), ""));

			// If at end of DB list, then default operation is insert (1)
			int compare = 1;
			if (dbListIdx < dbListSize) {
				dbStudent = dbList.get(dbListIdx);
				compare = dbStudent.compareTo(importStudent);
			}

			if (compare == 0) {
				// ClientID and all data matches
				dbListIdx++;
				continue;

			} else if (compare == -1) {
				// Extra clientID in database
				while (dbListIdx < dbListSize && dbList.get(dbListIdx).getClientID() < importStudent.getClientID()) {
					// Mark student as not in master DB
					if (dbList.get(dbListIdx).getIsInMasterDb() == 1)
						updateStudent(dbList.get(dbListIdx), 0);
					dbListIdx++;
				}
				if (dbList.get(dbListIdx).getClientID() == importStudent.getClientID()) {
					// Now that clientID's match, compare and update again
					if (dbList.get(dbListIdx).compareTo(importStudent) != 0) {
						updateStudent(importStudent, 1);
					}
					dbListIdx++;
				}

			} else if (compare == 1) {
				// Insert new student into DB
				insertStudent(importStudent);

			} else {
				// ClientID matches but data has changed
				updateStudent(importStudent, 1);
				dbListIdx++;
			}
		}
	}

	private ArrayList<StudentImportModel> getAllStudentsAsImportData() {
		ArrayList<StudentImportModel> nameList = new ArrayList<StudentImportModel>();

		// Convert student data to import data format
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students ORDER BY ClientID;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					nameList.add(new StudentImportModel(result.getInt("ClientID"), result.getString("LastName"),
							result.getString("FirstName"), result.getString("GithubName"), result.getInt("Gender"),
							result.getDate("StartDate").toString(), result.getInt("Location"),
							result.getInt("GradYear"), result.getInt("isInMasterDb")));
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

	private void insertStudent(StudentImportModel student) {
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addStudentStmt = dbConnection.prepareStatement(
						"INSERT INTO Students (ClientID, LastName, FirstName, GithubName, Gender, StartDate, Location, GradYear, isInMasterDb) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1);");

				int col = 1;
				addStudentStmt.setInt(col++, student.getClientID());
				addStudentStmt.setString(col++, student.getLastName());
				addStudentStmt.setString(col++, student.getFirstName());
				if (student.getGithubName().equals(""))
					addStudentStmt.setString(col++, null);
				else
					addStudentStmt.setString(col++, student.getGithubName());
				addStudentStmt.setInt(col++, student.getGender());
				if (!student.getStartDate().equals(""))
					addStudentStmt.setDate(col++, java.sql.Date.valueOf(student.getStartDate()));
				else
					addStudentStmt.setDate(col++, null);
				addStudentStmt.setInt(col++, student.getHomeLocation());
				addStudentStmt.setInt(col++, student.getGradYear());

				addStudentStmt.executeUpdate();
				addStudentStmt.close();

				if (student.getGithubName() == null)
					logData.add(new LogDataModel(LogDataModel.ADD_NEW_STUDENT_NO_GITHUB,
							new StudentNameModel(student.getFirstName(), student.getLastName(), true),
							student.getClientID(), ""));
				else
					logData.add(new LogDataModel(LogDataModel.ADD_NEW_STUDENT,
							new StudentNameModel(student.getFirstName(), student.getLastName(), true),
							student.getClientID(), ""));
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database: " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				System.out.println("Add student database failure: " + e2.getMessage());
				break;
			}
		}
	}

	private void updateStudent(StudentImportModel student, int isInDb) {
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateStudentStmt = dbConnection.prepareStatement(
						"UPDATE Students SET LastName=?, FirstName=?, GithubName=?, StartDate=?, Location=?, GradYear=?, isInMasterDb=? "
								+ "WHERE ClientID=?;");

				int col = 1;
				updateStudentStmt.setString(col++, student.getLastName());
				updateStudentStmt.setString(col++, student.getFirstName());
				if (student.getGithubName().equals(""))
					updateStudentStmt.setString(col++, null);
				else
					updateStudentStmt.setString(col++, student.getGithubName());
				if (!student.getStartDate().equals(""))
					updateStudentStmt.setDate(col++, java.sql.Date.valueOf(student.getStartDate()));
				else {
					updateStudentStmt.setDate(col++, null);
				}
				updateStudentStmt.setInt(col++, student.getHomeLocation());
				updateStudentStmt.setInt(col++, student.getGradYear());
				updateStudentStmt.setInt(col++, isInDb);
				updateStudentStmt.setInt(col, student.getClientID());

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();

				logData.add(new LogDataModel(LogDataModel.UPDATE_STUDENT_INFO,
						new StudentNameModel(student.getFirstName(), student.getLastName(), true),
						student.getClientID(), ""));
				break;

			} catch (CommunicationsException e1) {
				System.out.println("Re-connecting to database: " + e1.getMessage());
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				System.out.println("Update student database failure: " + e2.getMessage());
				break;
			}
		}
	}

	/*
	 * ------- Activity Database Queries -------
	 */
	public ArrayList<ActivityModel> getAllActivities() {
		ArrayList<ActivityModel> activityList = new ArrayList<ActivityModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.ClientID = Students.ClientID "
								+ "ORDER BY Activities.ClientID, ServiceDate DESC, EventName;");
				ResultSet result = selectStmt.executeQuery();
				getActivitiesList(activityList, result);
				Collections.sort(activityList);

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
						"SELECT * FROM Activities, Students WHERE Activities.ClientID = Students.ClientID AND "
								+ "EventName=? ORDER BY Activities.ClientID, ServiceDate DESC, EventName;");
				selectStmt.setString(1, className);

				ResultSet result = selectStmt.executeQuery();
				getActivitiesList(activityList, result);
				Collections.sort(activityList);

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
						"SELECT * FROM Activities, Students WHERE Activities.ClientID = Students.ClientID AND "
								+ "FirstName='" + studentName.getFirstName() + "' AND " + "LastName='"
								+ studentName.getLastName()
								+ "' ORDER BY Activities.ClientID, ServiceDate DESC, EventName;");
				ResultSet result = selectStmt.executeQuery();
				getActivitiesList(activityList, result);
				Collections.sort(activityList);

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

	public ArrayList<ActivityModel> getActivitiesByClientID(String clientID) {
		ArrayList<ActivityModel> activityList = new ArrayList<ActivityModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.ClientID = Students.ClientID AND "
								+ "Activities.ClientID=? ORDER BY Activities.ClientID, ServiceDate DESC, EventName;");
				selectStmt.setInt(1, Integer.parseInt(clientID));

				ResultSet result = selectStmt.executeQuery();
				getActivitiesList(activityList, result);
				Collections.sort(activityList);

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

	private ArrayList<ActivityEventModel> getEventsWithNoComments() {
		ArrayList<ActivityEventModel> eventList = new ArrayList<ActivityEventModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.ClientID = Students.ClientID AND "
								+ "Comments IS NULL AND GithubName IS NOT NULL;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					eventList.add(new ActivityEventModel(result.getInt("ClientID"), result.getDate("ServiceDate"),
							result.getString("EventName"), result.getString("GithubName"), result.getString("RepoName"),
							result.getString("Comments"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), true)));
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
		return eventList;
	}

	private void getActivitiesList(ArrayList<ActivityModel> activityList, ResultSet result) {
		int lastClientID = -1;
		ActivityModel lastActivityModel = null;

		try {
			while (result.next()) {
				int thisClientID = result.getInt("Students.ClientID");
				if (lastClientID == thisClientID) {
					// Add more data to existing client
					lastActivityModel.addActivityData(new ActivityEventModel(result.getInt("ClientID"),
							result.getDate("ServiceDate"), result.getString("EventName"),
							result.getString("GithubName"), result.getString("RepoName"), result.getString("Comments"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), true)));

				} else {
					// Create student model for new client
					lastActivityModel = new ActivityModel(thisClientID,
							new StudentNameModel(result.getString("Students.FirstName"),
									result.getString("Students.LastName"), result.getBoolean("isInMasterDb")),
							result.getString("GithubName"),
							new ActivityEventModel(result.getInt("CLientID"), result.getDate("ServiceDate"),
									result.getString("EventName"), result.getString("GithubName"),
									result.getString("RepoName"), result.getString("Comments"), new StudentNameModel(
											result.getString("FirstName"), result.getString("LastName"), true)));
					activityList.add(lastActivityModel);
					lastClientID = thisClientID;
				}
			}

		} catch (SQLException e) {
			System.out.println("Get Activity database error: " + e.getMessage());
			e.printStackTrace();
			return;
		}
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

	public void addActivity(int clientID, String serviceDate, String eventName) {
		for (int i = 0; i < 2; i++) {
			// TODO: Make sure student exists before trying to add data
			try {
				PreparedStatement addActivityStmt = dbConnection.prepareStatement(
						"INSERT INTO Activities " + "(ClientID, ServiceDate, EventName) VALUES (?, ?, ?);");

				int col = 1;
				addActivityStmt.setInt(col++, clientID);
				addActivityStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));
				addActivityStmt.setString(col++, eventName);

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
				// Attendance data already exists, do nothing
				break;

			} catch (SQLException e3) {
				System.out.println("Failed to add attendance data for ID=" + clientID + ", event=" + eventName + ": "
						+ e3.getMessage());
				break;
			}
		}
	}

	private void updateActivity(int clientID, StudentNameModel nameModel, String serviceDate, String repoName,
			String comments) {
		PreparedStatement updateActivityStmt;
		try {
			// The only fields that should be updated are the comments and repo name
			updateActivityStmt = dbConnection.prepareStatement(
					"UPDATE Activities SET Comments=?, RepoName=? WHERE ClientID=? AND ServiceDate=?;");

			int col = 1;
			updateActivityStmt.setString(col++, comments);
			updateActivityStmt.setString(col++, repoName);
			updateActivityStmt.setInt(col++, clientID);
			updateActivityStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));

			updateActivityStmt.executeUpdate();
			updateActivityStmt.close();

			logData.add(new LogDataModel(LogDataModel.UPDATE_STUDENT_ATTENDANCE, nameModel, clientID,
					" for repo " + repoName + " (" + serviceDate + ")"));
			return;

		} catch (SQLException e) {
			System.out.println("Update activities database failure: " + e.getMessage());
		}
	}

	// TODO: Get this from file
	private static final String token = "223bcb4816e95309f88c1154377f721e3d77568a";

	public void importGithubComments() {
		// Get all activities w/ github name and no comments
		ArrayList<ActivityEventModel> eventList = getEventsWithNoComments();

		for (int i = 0; i < eventList.size(); i++) {
			// Get commit info for each student/date combo
			ActivityEventModel event = eventList.get(i);
			String url = "https://api.github.com/search/commits?q=committer-name:\"" + event.getGithubName().trim()
					+ "\"+committer-date:" + event.getServiceDate().toString().trim();
			String[] command = { "C:\\curl\\curl.exe", "-u", "wavis421:" + token, "-H",
					"Accept:application/vnd.github.cloak-preview+json", url };

			ProcessBuilder process = new ProcessBuilder(command);
			Process p;
			try {
				p = process.start();
				processGithubInputStream(event, p.getInputStream());

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void processGithubInputStream(ActivityEventModel event, InputStream inputStream) {
		JsonReader commitReader;

		try {
			commitReader = Json.createReader(inputStream);
			JsonObject jsonObject = commitReader.readObject();

			// Get commit items from JSON input stream
			JsonArray commitJsonArray = jsonObject.getJsonArray("items");

			if (commitJsonArray == null) {
				// Error occurred -- no JSON data
				System.out.println("Error occurred: " + jsonObject.getString("message"));
				commitReader.close();
				return;
			}

			if (commitJsonArray.size() == 0) {
				// No data found for this github username/date combo
				logData.add(new LogDataModel(LogDataModel.MISSING_COMMIT_DATA, event.getStudentNameModel(),
						event.getClientID(), " " + event.getGithubName() + " (" + event.getServiceDate() + ")"));
				commitReader.close();
				return;
			}

			for (int i = 0; i < commitJsonArray.size(); i++) {
				// Process each commit
				JsonObject value = (JsonObject) commitJsonArray.get(i);
				String repository = ((JsonObject) value.getJsonObject("repository")).getString("name");
				String message = value.getJsonObject("commit").getString("message");

				// Trim message to get only summary data
				int idx = message.indexOf("\n");
				if (idx > -1)
					message = message.trim().substring(0, idx);

				// Update comments & repo name
				updateActivity(event.getClientID(), event.getStudentNameModel(), event.getServiceDate().toString(),
						repository, message);
			}
			commitReader.close();

		} catch (JsonException e) {
			System.out.println(e.getMessage());
		}
	}
}

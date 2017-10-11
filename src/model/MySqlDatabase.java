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
import javax.json.JsonStructure;
import javax.json.stream.JsonParsingException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.joda.time.DateTime;

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
				System.out.println("Get Attendance DB error: " + e2.getMessage());
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
				System.out.println("Get Attendance DB error: " + e2.getMessage());
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
				System.out.println("Get Attendance DB error: " + e2.getMessage());
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
				System.out.println("Get Attendance DB error: " + e2.getMessage());
				e2.printStackTrace();
				break;
			}
		}
		return activityList;
	}

	private ArrayList<ActivityEventModel> getAllEvents() {
		ArrayList<ActivityEventModel> eventList = new ArrayList<ActivityEventModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// Get attendance data from the DB for all students that have a github user name
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.ClientID = Students.ClientID "
								+ "ORDER BY Activities.ClientID, ServiceDate DESC, EventName;");
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
				System.out.println("Get Attendance DB error: " + e2.getMessage());
				e2.printStackTrace();
				break;
			}
		}
		return eventList;
	}

	private ArrayList<ActivityEventModel> getEventsWithNoComments() {
		ArrayList<ActivityEventModel> eventList = new ArrayList<ActivityEventModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// Get attendance data from the DB for all students that have a github user name
				// and the comment field is blank
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
				System.out.println("Get Attendance DB error: " + e2.getMessage());
				e2.printStackTrace();
				break;
			}
		}
		return eventList;
	}

	private void getActivitiesList(ArrayList<ActivityModel> activityList, ResultSet result) {
		int lastClientID = -1;
		ActivityModel lastActivityModel = null;

		// Process DB query result containing activities by grouping the activities by
		// student and then adding the resulting Activity Model to the the activityList.
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
			System.out.println("Get Attendance DB error: " + e.getMessage());
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

	public void importActivities(ArrayList<ActivityEventModel> importList) {
		ArrayList<ActivityEventModel> dbList = getAllEvents();
		ArrayList<StudentModel> studentList = getAllStudents();
		int dbListIdx = 0;
		int dbListSize = dbList.size();
		Collections.sort(importList);

		ActivityEventModel dbActivity;
		for (int i = 0; i < importList.size(); i++) {
			ActivityEventModel importEvent = importList.get(i);

			// If at end of DB list, then default operation is insert (1)
			int compare = 1;
			if (dbListIdx < dbListSize) {
				dbActivity = dbList.get(dbListIdx);
				compare = dbActivity.compareTo(importEvent);
			}

			if (compare == 0) {
				// All data matches, so continue through list
				dbListIdx++;
				continue;

			} else if (compare == -1) {
				// Extra events in DB; toss data until caught up with import list
				while (dbListIdx < dbListSize && dbList.get(dbListIdx).compareTo(importEvent) < 0) {
					dbListIdx++;
				}

				// Caught up, now compare again and process
				if (dbListIdx < dbListSize) {
					if (dbList.get(dbListIdx).compareTo(importEvent) == 0) {
						dbListIdx++;

					} else if (isClientInStudentList(studentList, importEvent.getClientID())) {
						addActivity(importEvent.getClientID(), importEvent.getServiceDateString(),
								importEvent.getEventName(), dbList.get(dbListIdx).getStudentNameModel());

					} else
						logData.add(new LogDataModel(LogDataModel.STUDENT_NOT_FOUND,
								new StudentNameModel(importEvent.getStudentNameModel().getFirstName(), "", false),
								importEvent.getClientID(),
								": " + importEvent.getEventName() + " on " + importEvent.getServiceDateString()));
				}

			} else if (isClientInStudentList(studentList, importEvent.getClientID())) {
				// New event, insert into DB
				addActivity(importEvent.getClientID(), importEvent.getServiceDateString(), importEvent.getEventName(),
						dbList.get(dbListIdx).getStudentNameModel());

			} else {
				logData.add(new LogDataModel(LogDataModel.STUDENT_NOT_FOUND,
						new StudentNameModel(importEvent.getStudentNameModel().getFirstName(), "", false),
						importEvent.getClientID(),
						": " + importEvent.getEventName() + " on " + importEvent.getServiceDateString()));
			}
		}
	}

	private boolean isClientInStudentList(ArrayList<StudentModel> list, int clientID) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getClientID() == clientID)
				return true;
		}
		return false;
	}

	public void addActivity(int clientID, String serviceDate, String eventName, StudentNameModel nameModel) {
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addActivityStmt = dbConnection.prepareStatement(
						"INSERT INTO Activities " + "(ClientID, ServiceDate, EventName) VALUES (?, ?, ?);");

				int col = 1;
				addActivityStmt.setInt(col++, clientID);
				addActivityStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));
				addActivityStmt.setString(col++, eventName);

				addActivityStmt.executeUpdate();
				addActivityStmt.close();

				logData.add(new LogDataModel(LogDataModel.UPDATE_STUDENT_ATTENDANCE, nameModel, clientID, ""));
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
		for (int i = 0; i < 2; i++) {
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
				System.out.println("Update attendance DB failure for " + nameModel.toString() + ": " + e.getMessage());
			}
		}
	}

	// TODO: Get this from a file or website
	private static final String token = "223bcb4816e95309f88c1154377f721e3d77568a";

	public void importGithubComments() {
		// Get all activities w/ github user name and no comments
		ArrayList<ActivityEventModel> eventList = getEventsWithNoComments();
		String lastGithubUser = "";
		JsonArray repoJsonArray = null;

		for (int i = 0; i < eventList.size(); i++) {
			// Get commit info from DB for each student/date combo
			ActivityEventModel event = eventList.get(i);
			String gitUser = event.getGithubName();

			if (!gitUser.equals(lastGithubUser)) {
				// New github user, get new repo array
				lastGithubUser = gitUser;
				repoJsonArray = getReposForGithubUser(event);
				if (repoJsonArray == null)
					continue;

			} else if (repoJsonArray == null) {
				// This git account does not exist!!
				continue;
			}

			DateTime startDate = new DateTime(event.getServiceDate().toString());
			DateTime endDate = startDate.plusDays(1);

			for (int j = 0; j < repoJsonArray.size(); j++) {
				// Get commits data for each repo/date match
				String repoName = ((JsonObject) repoJsonArray.get(j)).getString("name").trim();
				String url = "https://api.github.com/repos/" + event.getGithubName() + "/" + repoName
						+ "/commits?since=" + startDate.toString("YYYY-MM-dd") + "&until="
						+ endDate.toString("YYYY-MM-dd");
				InputStream commitStream = executeCurlCommand(url);

				if (commitStream == null) {
					logData.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
							event.getClientID(), " for user '" + event.getGithubName() + "'"));
					continue;
				}
				// TODO: If more than 1 commit for this date, append comments
				processGithubCommitsStream(event, commitStream, repoName);
			}
		}
	}

	// Repo name format: level-X-module-Y-username
	private static final String URL_MODULE_PATTERN_MATCH = "module-";
	private static final int modulePatternLength = URL_MODULE_PATTERN_MATCH.length();

	public void importGithubCommentsByLevel(int level) {
		// Get all activities w/ github user name and no comments
		ArrayList<ActivityEventModel> eventList = getEventsWithNoComments();

		JsonArray repoJsonArray = getReposByLevel(level);
		if (repoJsonArray == null) {
			logData.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, new StudentNameModel("", "", false), 0,
					" for Level " + level));
			return;
		}

		for (int i = 0; i < repoJsonArray.size(); i++) {
			// Parse repo to get user name; allow for multiple digit module #
			String repoName = ((JsonObject) repoJsonArray.get(i)).getString("name");
			int idx = repoName.indexOf(URL_MODULE_PATTERN_MATCH) + modulePatternLength;
			idx += (repoName.substring(idx)).indexOf('-') + 1;
			String userName = repoName.substring(idx);

			// Search for user in eventList
			for (int j = 0; j < eventList.size(); j++) {
				ActivityEventModel event = eventList.get(j);
				if (userName.equals(event.getGithubName())) {
					DateTime startDate = new DateTime(event.getServiceDate().toString());
					DateTime endDate = startDate.plusDays(1);

					// Get commits data for repo/date match
					String url = "https://api.github.com/repos/League-Level" + level + "-Student/" + repoName
							+ "/commits?since=" + startDate.toString("YYYY-MM-dd") + "&until="
							+ endDate.toString("YYYY-MM-dd");
					InputStream commitStream = executeCurlCommand(url);

					if (commitStream == null) {
						logData.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
								event.getClientID(), " for user '" + userName + "'"));
						continue;
					}
					// TODO: If more than 1 commit for this date, append comments
					processGithubCommitsStream(event, commitStream, repoName);
				}
			}
		}
	}

	private InputStream executeCurlCommand(String url) {
		String[] command = { "C:\\curl\\curl.exe", "-u", "wavis421:" + token, url };

		ProcessBuilder process = new ProcessBuilder(command);
		Process p;
		InputStream inputStream = null;

		try {
			p = process.start();
			inputStream = p.getInputStream();

		} catch (IOException e) {
			System.out.println("Error executing Curl command: " + e.getMessage());
		}
		return inputStream;
	}

	private JsonArray getReposForGithubUser(ActivityEventModel event) {
		// Get all repos for this github user
		String gitUser = event.getGithubName();
		String url = "https://api.github.com/users/" + gitUser + "/repos";
		InputStream inputStream = executeCurlCommand(url);
		JsonArray jsonArray = null;

		if (inputStream == null) {
			logData.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
					event.getClientID(), " for Github user '" + gitUser + "'"));
			return null;
		}

		try {
			// Get all repos for this user
			JsonReader repoReader = Json.createReader(inputStream);
			JsonStructure repoStruct = repoReader.read();

			if (repoStruct instanceof JsonObject) {
				// Expecting an array of data, so this is an error!
				logData.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
						event.getClientID(),
						" for Github user '" + gitUser + "': " + ((JsonObject) repoStruct).getString("message")));

			} else {
				jsonArray = (JsonArray) repoStruct;
			}

			repoReader.close();
			inputStream.close();

		} catch (JsonParsingException e1) {
			logData.add(new LogDataModel(LogDataModel.GITHUB_PARSING_ERROR, event.getStudentNameModel(),
					event.getClientID(), " for Github user '" + event.getGithubName() + "': " + e1.getMessage()));

		} catch (IOException e2) {
			logData.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
					event.getClientID(), " (IO Excpetion) for Github user '" + event.getGithubName() + "': " + e2.getMessage()));
		}
		return jsonArray;
	}

	private JsonArray getReposByLevel(int level) {
		// Get all repos for this level
		String url = "https://api.github.com/users/League-Level" + level + "-Student/repos";
		InputStream inputStream = executeCurlCommand(url);
		JsonArray jsonArray = null;

		if (inputStream == null) {
			logData.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, new StudentNameModel("", "", false), 0,
					" for Level " + level + ": Input Stream null"));
			return null;
		}

		try {
			// Get all repos for this level
			JsonReader repoReader = Json.createReader(inputStream);
			JsonStructure repoStruct = repoReader.read();

			if (repoStruct instanceof JsonObject) {
				// Expecting an array of data, so this is an error!
				logData.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, new StudentNameModel("", "", false), 0,
						" for Level " + level + ": " + ((JsonObject) repoStruct).getString("message")));
			} else {
				jsonArray = (JsonArray) repoStruct;
			}

			repoReader.close();
			inputStream.close();

		} catch (JsonParsingException e1) {
			System.out.println("Github parsing error for Level " + level + ": " + e1.getMessage());

		} catch (IOException e2) {
			System.out.println("IO Exception while parsing Level " + level + ": " + e2.getMessage());
		}
		return jsonArray;
	}

	private void processGithubCommitsStream(ActivityEventModel event, InputStream inputStream, String repoName) {
		try {
			JsonReader commitReader = Json.createReader(inputStream);
			JsonStructure jsonStruct = commitReader.read();

			if (jsonStruct instanceof JsonObject) {
				commitReader.close();
				return;
			}

			// Get commit items from JSON input stream
			JsonArray commitJsonArray = (JsonArray) jsonStruct;

			if (commitJsonArray == null || commitJsonArray.size() == 0) {
				// No JSON data (repository is empty)
				commitReader.close();
				return;
			}

			for (int i = 0; i < commitJsonArray.size(); i++) {
				// Process each commit
				String message = ((JsonObject) commitJsonArray.get(i)).getJsonObject("commit").getString("message");

				// Trim message to get only summary data
				int idx = message.indexOf("\n");
				if (idx > -1)
					message = message.trim().substring(0, idx);

				// Update comments & repo name
				updateActivity(event.getClientID(), event.getStudentNameModel(), event.getServiceDate().toString(),
						repoName, message);
			}
			commitReader.close();

		} catch (JsonException e) {
			System.out.println("Failure parsing Github input stream: " + e.getMessage());
		}
	}
}

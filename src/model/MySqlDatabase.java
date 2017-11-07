package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.joda.time.DateTime;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

import gui.MainFrame;

public class MySqlDatabase {
	private static final int COMMENT_WIDTH = 150;
	private static final int LOG_APPEND_WIDTH = 75;
	private static Connection dbConnection = null;
	private JFrame parent;
	private String awsPassword;

	public MySqlDatabase(JFrame parent, String awsPassword) {
		this.parent = parent;
		this.awsPassword = awsPassword;

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
				dbConnection = MySqlConnection.connectToServer(parent, 1, awsPassword);

			} catch (SQLException e) {
				// Error handling performed in connectToServer
			}

			if (dbConnection == null) {
				int answer = JOptionPane.showConfirmDialog(null, "Do you want to retry?", "Failure connecting to DB",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
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
	 * ------- Student Database Queries -------
	 */
	public ArrayList<StudentModel> getAllStudents() {
		ArrayList<StudentModel> nameList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students ORDER BY FirstName, LastName;");
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, 0, e2.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, clientID, e2.getMessage());
				break;
			}
		}
	}

	public ArrayList<StudentModel> getStudentsNotInMasterDB() {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Students WHERE NOT isInMasterDb ORDER BY FirstName, LastName;");
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, 0, e2.getMessage());
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
					insertLogData(LogDataModel.REMOVE_INACTIVE_STUDENT,
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), false),
							result.getInt("ClientID"), "");

					removeStudentByClientID(result.getInt("ClientID"));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, 0, e2.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, 0, e2.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, clientID, e2.getMessage());
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
				insertLogData(LogDataModel.MISSING_GITHUB_NAME,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), "");
			}

			if (importStudent.getGradYear() == 0)
				insertLogData(LogDataModel.MISSING_GRAD_YEAR,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), "");

			if (importStudent.getStartDate().equals(""))
				insertLogData(LogDataModel.MISSING_FIRST_VISIT_DATE,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), "");

			if (importStudent.getHomeLocation() == LocationModel.CLASS_LOCATION_UNKNOWN)
				insertLogData(LogDataModel.MISSING_HOME_LOCATION,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), "");

			if (importStudent.getGender() == GenderModel.getGenderUnknown())
				insertLogData(LogDataModel.MISSING_GENDER,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), "");

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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, 0, e2.getMessage());
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
					insertLogData(LogDataModel.ADD_NEW_STUDENT_NO_GITHUB,
							new StudentNameModel(student.getFirstName(), student.getLastName(), true),
							student.getClientID(), "");
				else
					insertLogData(LogDataModel.ADD_NEW_STUDENT,
							new StudentNameModel(student.getFirstName(), student.getLastName(), true),
							student.getClientID(), "");
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel(student.getFirstName(), student.getLastName(),
						student.getIsInMasterDb() == 1 ? true : false);
				insertLogData(LogDataModel.STUDENT_DB_ERROR, studentModel, 0, e2.getMessage());
				break;
			}
		}
	}

	private void updateStudent(StudentImportModel student, int isInDb) {
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateStudentStmt = dbConnection.prepareStatement(
						"UPDATE Students SET LastName=?, FirstName=?, GithubName=?, Gender=?, StartDate=?, Location=?, GradYear=?, isInMasterDb=? "
								+ "WHERE ClientID=?;");

				int col = 1;
				updateStudentStmt.setString(col++, student.getLastName());
				updateStudentStmt.setString(col++, student.getFirstName());
				if (student.getGithubName().equals(""))
					updateStudentStmt.setString(col++, null);
				else
					updateStudentStmt.setString(col++, student.getGithubName());
				updateStudentStmt.setInt(col++, student.getGender());
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

				insertLogData(LogDataModel.UPDATE_STUDENT_INFO,
						new StudentNameModel(student.getFirstName(), student.getLastName(), true),
						student.getClientID(), "");
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel(student.getFirstName(), student.getLastName(),
						isInDb == 1 ? true : false);
				insertLogData(LogDataModel.STUDENT_DB_ERROR, studentModel, 0, e2.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, null, 0, e2.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, null, 0, e2.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel(studentName.getFirstName(),
						studentName.getLastName(), studentName.getIsInMasterDb());
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, 0, e2.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, null, 0, e2.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, null, 0, e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	public ArrayList<ActivityEventModel> getEventsWithNoComments(String startDate) {
		ArrayList<ActivityEventModel> eventList = new ArrayList<ActivityEventModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// Get attendance data from the DB for all students that have a github user name
				// and the comment field is blank
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Activities, Students WHERE Activities.ClientID = Students.ClientID AND "
								+ "Comments IS NULL AND GithubName IS NOT NULL AND "
								+ "ServiceDate >= ? ORDER BY GithubName;");
				selectStmt.setDate(1, java.sql.Date.valueOf(startDate));
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, null, 0, e2.getMessage());
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
			insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, null, 0, e.getMessage());
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, null, 0, e2.getMessage());
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
						"SELECT LastName, FirstName, isInMasterDb FROM Students ORDER BY FirstName, LastName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					studentList.add(new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
							result.getBoolean("isInMasterDb")));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, 0, e2.getMessage());
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

					} else if (getClientIdxInStudentList(studentList, importEvent.getClientID()) >= 0) {
						addActivity(importEvent.getClientID(), importEvent.getServiceDateString(),
								importEvent.getEventName(), dbList.get(dbListIdx).getStudentNameModel());

					} else
						insertLogData(LogDataModel.STUDENT_NOT_FOUND,
								new StudentNameModel(importEvent.getStudentNameModel().getFirstName(), "", false),
								importEvent.getClientID(),
								": " + importEvent.getEventName() + " on " + importEvent.getServiceDateString());
				}

			} else {
				// Data does not match existing student
				int idx = getClientIdxInStudentList(studentList, importEvent.getClientID());

				if (idx >= 0) {
					// Student exists in DB, so add activity data for this student
					addActivity(importEvent.getClientID(), importEvent.getServiceDateString(),
							importEvent.getEventName(), studentList.get(idx).getNameModel());

				} else {
					// Student not found
					insertLogData(LogDataModel.STUDENT_NOT_FOUND,
							new StudentNameModel(importEvent.getStudentNameModel().getFirstName(), "", false),
							importEvent.getClientID(),
							": " + importEvent.getEventName() + " on " + importEvent.getServiceDateString());
				}
			}
		}
	}

	private int getClientIdxInStudentList(ArrayList<StudentModel> list, int clientID) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getClientID() == clientID)
				return i;
		}
		return -1;
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

				insertLogData(LogDataModel.UPDATE_STUDENT_ATTENDANCE, nameModel, clientID, " for " + serviceDate);
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Attendance data already exists, do nothing
				break;

			} catch (SQLException e3) {
				StudentNameModel studentModel = new StudentNameModel(nameModel.getFirstName(), nameModel.getLastName(),
						nameModel.getIsInMasterDb());
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, clientID, e3.getMessage());
				break;
			}
		}
	}

	public void updateActivity(int clientID, StudentNameModel nameModel, String serviceDate, String repoName,
			String comments) {
		PreparedStatement updateActivityStmt;
		for (int i = 0; i < 2; i++) {
			try {
				// The only fields that should be updated are the comments and repo name
				updateActivityStmt = dbConnection.prepareStatement(
						"UPDATE Activities SET Comments=?, RepoName=? WHERE ClientID=? AND ServiceDate=?;");

				int col = 1;
				if (comments.length() >= COMMENT_WIDTH)
					comments = comments.substring(0, COMMENT_WIDTH);
				updateActivityStmt.setString(col++, comments);
				updateActivityStmt.setString(col++, repoName);
				updateActivityStmt.setInt(col++, clientID);
				updateActivityStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));

				updateActivityStmt.executeUpdate();
				updateActivityStmt.close();

				insertLogData(LogDataModel.UPDATE_GITHUB_COMMENTS, nameModel, clientID,
						" for repo " + repoName + " (" + serviceDate + ")");
				return;

			} catch (SQLException e) {
				StudentNameModel studentModel = new StudentNameModel(nameModel.getFirstName(), nameModel.getLastName(),
						nameModel.getIsInMasterDb());
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, clientID, e.getMessage());
			}
		}
	}

	/*
	 * ------- Logging Database Queries -------
	 */
	public void insertLogData(int logType, StudentNameModel studentNameModel, int clientID, String appendedMsg) {
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addLogDataStmt = dbConnection.prepareStatement(
						"INSERT INTO LogData (ClientID, LogType, StudentName, AppendedString, LogDate) "
								+ "VALUES (?, ?, ?, ?, ?);");

				int col = 1;
				addLogDataStmt.setInt(col++, clientID);
				addLogDataStmt.setInt(col++, logType);
				if (studentNameModel == null)
					addLogDataStmt.setString(col++, null);
				else
					addLogDataStmt.setString(col++, studentNameModel.toString());
				if (appendedMsg.length() >= LOG_APPEND_WIDTH)
					appendedMsg = appendedMsg.substring(0, LOG_APPEND_WIDTH);
				addLogDataStmt.setString(col++, appendedMsg);
				addLogDataStmt.setString(col++, new DateTime().toString("yyyy-MM-dd HH:mm"));

				addLogDataStmt.executeUpdate();
				addLogDataStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				if (!e2.getMessage().startsWith("Duplicate entry"))
					// TODO: Can't log this error! What to do instead?
					JOptionPane.showMessageDialog(parent, "Unable to Log data: " + e2.getMessage());
				break;
			}
		}
	}

	public ArrayList<LogDataModel> getLogData() {
		ArrayList<LogDataModel> logData = new ArrayList<LogDataModel>();

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * FROM LogData ORDER BY LogDate;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					logData.add(new LogDataModel(result.getInt("LogType"), result.getString("LogDate").substring(0, 16),
							new StudentNameModel(result.getString("StudentName"), "", true), result.getInt("ClientID"),
							result.getString("AppendedString")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, null, 0, e2.getMessage());
				break;
			}
		}
		return logData;
	}

	public int getLogDataSize() {
		int logDataSize = 0;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM LogData");
				ResultSet result = selectStmt.executeQuery();

				if (result.next()) {
					logDataSize = result.getInt(1);
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.LOG_DATA_DB_ERROR, null, 0, ": " + e2.getMessage());
				break;
			}
		}
		return logDataSize;
	}

	public void clearLogData() {
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("TRUNCATE LogData");
				selectStmt.executeUpdate();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.LOG_DATA_DB_ERROR, null, 0, ": " + e2.getMessage());
				break;
			}
		}
	}
}

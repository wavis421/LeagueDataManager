package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

public class MySqlDatabase {
	// Use different port for Tracker App and Import to allow simultaneous connects
	public static final int TRACKER_APP_SSH_PORT = 5000;
	public static final int STUDENT_IMPORT_SSH_PORT = 6000;
	public static final int SALES_FORCE_SYNC_SSH_PORT = 7000;
	public static final int STUDENT_IMPORT_NO_SSH = 0;

	private static final int MAX_CONNECTION_ATTEMPTS = 3;
	private static final int MAX_CONNECT_ATTEMPTS_LAMBDA = 1;
	private static final int COMMENT_WIDTH = 150;
	private static final int REPO_NAME_WIDTH = 50;
	private static final int LOG_APPEND_WIDTH = 120;
	private static final int CLASS_NAME_WIDTH = 40;
	private static final int NUM_CLASS_LEVELS = 10;
	private static final String[] dayOfWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
			"Saturday" };
	public static final int CLASS_ATTEND_NUM_DAYS_TO_KEEP = 30;

	private static Connection dbConnection = null;
	private JFrame parent;
	private String awsPassword;
	private MySqlConnection mySqlConnection;
	private int localPort;

	public MySqlDatabase(JFrame parent, String awsPassword, int localPort) {
		// This constructor is used by LeagueDataManager GUI
		this.parent = parent;
		this.awsPassword = awsPassword;
		this.localPort = localPort;

		mySqlConnection = new MySqlConnection(localPort);
	}

	public MySqlDatabase(String awsPassword, int localPort) {
		// This constructor is used by the Student Import app (no GUI)
		this.awsPassword = awsPassword;
		mySqlConnection = new MySqlConnection(localPort);
	}

	/*
	 * ------- Database Connections -------
	 */
	public boolean connectDatabase() {
		int numRetries = MAX_CONNECTION_ATTEMPTS;
		if (localPort == STUDENT_IMPORT_NO_SSH)
			numRetries = MAX_CONNECT_ATTEMPTS_LAMBDA;

		for (int i = 0; i < numRetries; i++) {
			try {
				dbConnection = mySqlConnection.connectToServer(parent, awsPassword);

			} catch (SQLException e) {
				// TODO: How to handle this exception?
			}

			if (dbConnection != null)
				return true;
		}
		return false;
	}

	public void disconnectDatabase() {
		if (dbConnection != null) {
			mySqlConnection.closeConnections();
			dbConnection = null;
		}
	}

	/*
	 * ------- Student Database Queries -------
	 */
	public ArrayList<StudentModel> getActiveStudents() {
		ArrayList<StudentModel> nameList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students WHERE isInMasterDb ORDER BY FirstName, LastName;");
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return nameList;
	}

	public void removeStudentByClientID(int clientID) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement deleteStudentStmt = dbConnection
						.prepareStatement("DELETE FROM Students WHERE ClientID=?;");

				// Delete student
				deleteStudentStmt.setInt(1, clientID);
				deleteStudentStmt.executeUpdate();
				deleteStudentStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), clientID,
						": " + e2.getMessage());
				break;
			}
		}
	}

	public ArrayList<StudentModel> getStudentsNotInMasterDB() {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return studentList;
	}

	public void removeInactiveStudents() {
		// Remove any student not in master DB who have no attendance data
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students WHERE NOT isInMasterDb AND "
								+ "(SELECT COUNT(*) FROM Attendance WHERE Attendance.ClientID = Students.ClientID AND State = 'completed') = 0;");
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
	}

	public StudentModel getStudentByGithubName(String githubName) {
		StudentModel student = null;

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return student;
	}

	public ArrayList<StudentModel> getStudentByClientID(int clientID) {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), clientID,
						": " + e2.getMessage());
				break;
			}
		}
		return studentList;
	}

	public ArrayList<StudentModel> getStudentsUsingFlag(String flagName) {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students WHERE " + flagName + " = 1;");

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

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
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

			if (importStudent.getHomeLocation() == LocationModel.CLASS_LOCATION_UNKNOWN) {
				if (importStudent.getHomeLocAsString().equals(""))
					insertLogData(LogDataModel.MISSING_HOME_LOCATION,
							new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
							importStudent.getClientID(), "");
				else
					insertLogData(LogDataModel.UNKNOWN_HOME_LOCATION,
							new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
							importStudent.getClientID(), " (" + importStudent.getHomeLocAsString() + ")");
			}

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
						updateIsInMasterDb(dbList.get(dbListIdx), 0);
					dbListIdx++;
				}
				if (dbListIdx < dbListSize) {
					if (dbList.get(dbListIdx).getClientID() == importStudent.getClientID()) {
						// Now that clientID's match, compare and update again
						if (dbList.get(dbListIdx).compareTo(importStudent) != 0) {
							updateStudent(importStudent, dbList.get(dbListIdx));
						}
						dbListIdx++;
					} else {
						// Import student is new, insert into DB
						insertStudent(importStudent);
					}
				}

			} else if (compare == 1) {
				// Insert new student into DB
				insertStudent(importStudent);

			} else {
				// ClientID matches but data has changed
				updateStudent(importStudent, dbList.get(dbListIdx));
				dbListIdx++;
			}
		}
	}

	private ArrayList<StudentImportModel> getAllStudentsAsImportData() {
		ArrayList<StudentImportModel> nameList = new ArrayList<StudentImportModel>();

		// Convert student data to import data format
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students ORDER BY ClientID;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					String startDateString;
					if (result.getDate("StartDate") == null)
						startDateString = "";
					else
						startDateString = result.getDate("StartDate").toString();

					nameList.add(new StudentImportModel(result.getInt("ClientID"), result.getString("LastName"),
							result.getString("FirstName"), result.getString("GithubName"), result.getInt("Gender"),
							startDateString, result.getInt("Location"), result.getInt("GradYear"),
							result.getInt("isInMasterDb")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return nameList;
	}

	private void insertStudent(StudentImportModel student) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement addStudentStmt = dbConnection.prepareStatement(
						"INSERT INTO Students (ClientID, LastName, FirstName, GithubName, NewGithub, NewStudent,"
								+ "Gender, StartDate, Location, GradYear, isInMasterDb) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1);");

				int col = 1;
				addStudentStmt.setInt(col++, student.getClientID());
				addStudentStmt.setString(col++, student.getLastName());
				addStudentStmt.setString(col++, student.getFirstName());
				if (student.getGithubName().equals("")) {
					addStudentStmt.setString(col++, null);
					addStudentStmt.setInt(col++, 0);
				} else {
					addStudentStmt.setString(col++, student.getGithubName());
					addStudentStmt.setInt(col++, 1);
				}
				addStudentStmt.setInt(col++, 1);
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel(student.getFirstName(), student.getLastName(),
						student.getIsInMasterDb() == 1 ? true : false);
				insertLogData(LogDataModel.STUDENT_DB_ERROR, studentModel, 0, ": " + e2.getMessage());
				break;
			}
		}
	}

	private void updateStudent(StudentImportModel importStudent, StudentImportModel dbStudent) {
		for (int i = 0; i < 2; i++) {
			// Before updating database, determine what fields have changed
			String changedFields = getStudentChangedFields(importStudent, dbStudent);
			boolean githubChanged = false;
			boolean newStudent = false;

			// If student added back to DB, mark as new
			if (changedFields.contains("Added back"))
				newStudent = true;
			// If github user has changed or new student, force github updates
			if (changedFields.contains("Github") || (newStudent && !importStudent.getGithubName().equals("")))
				githubChanged = true;

			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateStudentStmt = dbConnection.prepareStatement(
						"UPDATE Students SET LastName=?, FirstName=?, GithubName=?, NewGithub=?, NewStudent=?,"
								+ "Gender=?, StartDate=?, Location=?, GradYear=?, isInMasterDb=? "
								+ "WHERE ClientID=?;");

				int col = 1;
				updateStudentStmt.setString(col++, importStudent.getLastName());
				updateStudentStmt.setString(col++, importStudent.getFirstName());
				if (importStudent.getGithubName().equals(""))
					updateStudentStmt.setString(col++, null);
				else
					updateStudentStmt.setString(col++, importStudent.getGithubName());
				updateStudentStmt.setInt(col++, githubChanged ? 1 : 0);
				updateStudentStmt.setInt(col++, newStudent ? 1 : 0);
				updateStudentStmt.setInt(col++, importStudent.getGender());
				if (importStudent.getStartDate() != null && !importStudent.getStartDate().equals(""))
					updateStudentStmt.setDate(col++, java.sql.Date.valueOf(importStudent.getStartDate()));
				else {
					updateStudentStmt.setDate(col++, null);
				}
				updateStudentStmt.setInt(col++, importStudent.getHomeLocation());
				updateStudentStmt.setInt(col++, importStudent.getGradYear());
				updateStudentStmt.setInt(col++, 1); // is in master DB
				updateStudentStmt.setInt(col, importStudent.getClientID());

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();

				insertLogData(LogDataModel.UPDATE_STUDENT_INFO,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), changedFields);
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel(importStudent.getFirstName(),
						importStudent.getLastName(), true);
				insertLogData(LogDataModel.STUDENT_DB_ERROR, studentModel, 0, ": " + e2.getMessage());
				break;
			}
		}
	}

	public void updateStudentFlags(StudentModel student, String flagName, int newFlagState) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateStudentStmt = dbConnection
						.prepareStatement("UPDATE Students SET " + flagName + "=? WHERE ClientID=?;");

				updateStudentStmt.setInt(1, newFlagState);
				updateStudentStmt.setInt(2, student.getClientID());

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, student.getNameModel(), student.getClientID(),
						": " + e2.getMessage());
				break;
			}
		}
	}

	public void updateIsInMasterDb(StudentImportModel student, int isInMasterDb) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateStudentStmt = dbConnection
						.prepareStatement("UPDATE Students SET isInMasterDb=? WHERE ClientID=?;");

				updateStudentStmt.setInt(1, isInMasterDb);
				updateStudentStmt.setInt(2, student.getClientID());

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel model = new StudentNameModel(student.getFirstName(), student.getLastName(),
						(isInMasterDb == 1) ? true : false);
				insertLogData(LogDataModel.STUDENT_DB_ERROR, model, student.getClientID(), ": " + e2.getMessage());
				break;
			}
		}
	}

	private String getStudentChangedFields(StudentImportModel importStudent, StudentImportModel dbStudent) {
		String changes = "";

		if (!importStudent.getFirstName().equals(dbStudent.getFirstName())) {
			if (changes.equals(""))
				changes += " (first name";
			else
				changes += ", first name";
		}
		if (!importStudent.getLastName().equals(dbStudent.getLastName())) {
			if (changes.equals(""))
				changes += " (last name";
			else
				changes += ", last name";
		}
		if (importStudent.getGender() != dbStudent.getGender()) {
			if (changes.equals(""))
				changes += " (gender";
			else
				changes += ", gender";
		}
		if (!importStudent.getGithubName().equals(dbStudent.getGithubName())) {
			if (changes.equals(""))
				changes += " (Github user";
			else
				changes += ", Github user";
		}
		if (importStudent.getGradYear() != dbStudent.getGradYear()) {
			if (changes.equals(""))
				changes += " (Grad year";
			else
				changes += ", Grad year";
		}
		if (importStudent.getHomeLocation() != dbStudent.getHomeLocation()) {
			if (changes.equals(""))
				changes += " (Home Location";
			else
				changes += ", Home Location";
		}
		if (!importStudent.getStartDate().equals(dbStudent.getStartDate())) {
			if (changes.equals(""))
				changes += " (Start Date";
			else
				changes += ", Start Date";
		}
		if (importStudent.getIsInMasterDb() != dbStudent.getIsInMasterDb()) {
			if (changes.equals(""))
				changes += " (Added back to Master DB";
			else
				changes += ", Added back to Master DB";
		}

		if (!changes.equals(""))
			changes += ")";

		return changes;
	}

	/*
	 * ------- Attendance Database Queries -------
	 */
	public ArrayList<AttendanceModel> getAllAttendance() {
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE isInMasterDb AND Attendance.ClientID = Students.ClientID "
								+ "AND State = 'completed' ORDER BY Attendance.ClientID, ServiceDate DESC, EventName;");
				ResultSet result = selectStmt.executeQuery();
				getAttendanceList(attendanceList, result, false);
				Collections.sort(attendanceList);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	public ArrayList<AttendanceModel> getAttendanceByClassName(String className) {
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();
		ArrayList<AttendanceModel> listByClient;
		String sinceDate = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"))
				.minusDays(CLASS_ATTEND_NUM_DAYS_TO_KEEP).toString("yyyy-MM-dd");

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE isInMasterDb AND Attendance.ClientID = Students.ClientID "
								+ "AND State = 'completed' AND EventName=? AND ServiceDate > ? GROUP BY Students.ClientID;");
				selectStmt.setString(1, className);
				selectStmt.setString(2, sinceDate);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					Integer thisClientID = result.getInt("Students.ClientID");
					listByClient = getAttendanceByClientID(thisClientID.toString());
					attendanceList.addAll(listByClient);
				}
				Collections.sort(attendanceList);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	public ArrayList<AttendanceModel> getAttendanceByClassByDate(String className, String date) {
		// Special case of getAttendanceByClassName, selected for specific day
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();
		ArrayList<AttendanceModel> listByClient;

		for (int i = 0; i < 2; i++) {
			// If Database no longer connected, the exception code will re-connect
			try {
				// Get attendance by class and by date
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT Students.ClientID, Students.FirstName, Students.LastName, Attendance.State, "
								+ "Attendance.ServiceCategory FROM Attendance, Students "
								+ "WHERE isInMasterDb AND Attendance.ClientID = Students.ClientID "
								+ "AND (State = 'completed' OR State = 'registered') AND EventName=? "
								+ "AND ServiceDate=? GROUP BY Students.ClientID;");
				selectStmt.setString(1, className);
				selectStmt.setString(2, date);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					// Get attendance for each student in this particular make-up class
					Integer thisClientID = result.getInt("Students.ClientID");
					listByClient = getAttendanceByClientID(thisClientID.toString());
					attendanceList.addAll(listByClient);
				}
				Collections.sort(attendanceList);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	public ArrayList<AttendanceModel> getAttendanceByCourseName(String courseName) {
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();
		ArrayList<AttendanceModel> listByClient;

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT Students.ClientID, FirstName, LastName, GithubName, State, "
								+ "ServiceCategory FROM Attendance, Students "
								+ "WHERE isInMasterDb AND Attendance.ClientID = Students.ClientID "
								+ "AND (State = 'completed' OR State = 'registered') AND EventName=? "
								+ "GROUP BY Students.ClientID;");
				selectStmt.setString(1, courseName);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					Integer thisClientID = result.getInt("Students.ClientID");
					StudentNameModel name = new StudentNameModel(result.getString("FirstName"),
							result.getString("LastName"), true);

					listByClient = getAttendanceByClientID(thisClientID.toString());
					if (listByClient.size() == 0)
						attendanceList.add(new AttendanceModel(thisClientID, name, result.getString("GithubName"),
								new AttendanceEventModel(thisClientID, 0, null, "   ", result.getString("GithubName"),
										"", "", name, result.getString("ServiceCategory"), result.getString("State"))));
					else
						attendanceList.addAll(listByClient);
				}
				Collections.sort(attendanceList);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	public ArrayList<AttendanceModel> getAttendanceByClientID(String clientID) {
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE Attendance.ClientID = Students.ClientID AND State = 'completed' "
								+ "AND Attendance.ClientID=? ORDER BY Attendance.ClientID, ServiceDate DESC, EventName;");
				selectStmt.setInt(1, Integer.parseInt(clientID));

				ResultSet result = selectStmt.executeQuery();
				getAttendanceList(attendanceList, result, false);
				Collections.sort(attendanceList);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	public ArrayList<GithubModel> getStudentsWithNoRecentGithub(String sinceDate, int minClassesWithoutGithub) {
		ArrayList<GithubModel> githubList = new ArrayList<GithubModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE isInMasterDb AND Attendance.ClientID = Students.ClientID "
								+ "AND State = 'completed' AND ServiceDate > ?  "
								+ "ORDER BY Attendance.ClientID, ServiceDate ASC, EventName;");
				selectStmt.setString(1, sinceDate);

				ResultSet result = selectStmt.executeQuery();

				int count = 0;
				boolean ignore = false;
				int lastClientID = -1;
				GithubModel lastGithubModel = null;

				while (result.next()) {
					int thisClientID = result.getInt("Students.ClientID");

					if (thisClientID != lastClientID) {
						// Next client ID
						if (!ignore && count >= minClassesWithoutGithub && lastGithubModel != null) {
							// No comments for at least 4 class visits, so add to list
							githubList.add(lastGithubModel);
						}

						// Reset flags/counters for next Client ID
						count = 0;
						ignore = false;
						lastClientID = thisClientID;
						lastGithubModel = null;
					}

					String comments = result.getString("Comments");
					if (!ignore && (comments == null || comments.equals(""))) {
						// No github comments for this client either previously or now
						String eventName = result.getString("EventName");
						count++;

						if (eventName.charAt(0) >= '0' && eventName.charAt(0) <= '3') {
							// Save this record for possible addition to list
							DateTime serviceDate = new DateTime(result.getDate("ServiceDate"));
							lastGithubModel = new GithubModel(thisClientID,
									result.getString("FirstName") + " " + result.getString("LastName"),
									serviceDate.toString("EEEEE"), eventName, result.getString("GithubName"),
									result.getString("TeacherNames"));
						}

					} else // Ignore this student if ANY github comments exist
						ignore = true;
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}

		Collections.sort(githubList); // Sort by student name
		return githubList;
	}

	public ArrayList<AttendanceEventModel> getAllEvents() {
		ArrayList<AttendanceEventModel> eventList = new ArrayList<AttendanceEventModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// Get attendance data from the DB for all students that have a github user name
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE Attendance.ClientID = Students.ClientID "
								+ "AND (State = 'completed' OR State = 'registered') "
								+ "ORDER BY Attendance.ClientID, ServiceDate DESC, EventName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					eventList.add(new AttendanceEventModel(result.getInt("ClientID"), result.getInt("VisitID"),
							result.getDate("ServiceDate"), result.getString("EventName"),
							result.getString("GithubName"), result.getString("RepoName"), result.getString("Comments"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), true),
							result.getString("ServiceCategory"), result.getString("State")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	public ArrayList<AttendanceEventModel> getEventsWithNoComments(String startDate, int clientID,
			boolean includeEmpty) {
		ArrayList<AttendanceEventModel> eventList = new ArrayList<AttendanceEventModel>();

		// Can either filter on null comments or both null + empty comments.
		// Null comments transition to empty comments to mark them as processed.
		String clientIdFilter = "";
		if (clientID != 0) // Specific github user being updated
			clientIdFilter = "Students.ClientID = " + clientID + " AND ";

		if (includeEmpty)
			clientIdFilter += "(Comments IS NULL OR Comments = '') ";
		else
			clientIdFilter += "Comments IS NULL ";

		for (int i = 0; i < 2; i++) {
			try {
				// Get attendance data from the DB for all students that have a github user name
				// and the comment field is blank
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE Attendance.ClientID = Students.ClientID AND "
								+ clientIdFilter + " AND State = 'completed' "
								+ "AND GithubName IS NOT NULL AND ServiceDate >= ? ORDER BY GithubName;");
				selectStmt.setDate(1, java.sql.Date.valueOf(startDate));
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					eventList.add(new AttendanceEventModel(result.getInt("ClientID"), result.getInt("VisitID"),
							result.getDate("ServiceDate"), result.getString("EventName"),
							result.getString("GithubName"), result.getString("RepoName"), result.getString("Comments"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), true),
							result.getString("ServiceCategory"), result.getString("State")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	private void getAttendanceList(ArrayList<AttendanceModel> attendanceList, ResultSet result, boolean filterOnDate) {
		int lastClientID = -1;
		AttendanceModel lastAttendanceModel = null;
		boolean removeAttendance = false;
		String beginDate = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"))
				.minusDays(CLASS_ATTEND_NUM_DAYS_TO_KEEP).toString("yyyy-MM-dd");

		// Process DB query result containing attendance by grouping the attendance by
		// student and then adding the resulting Attendance Model to the attendanceList.
		try {
			while (result.next()) {
				int thisClientID = result.getInt("Students.ClientID");
				if (lastClientID == thisClientID) {
					if (filterOnDate && removeAttendance) {
						// Don't use attendance if too far back
						continue;
					}
					// Add more data to existing client
					lastAttendanceModel.addAttendanceData(new AttendanceEventModel(result.getInt("ClientID"),
							result.getInt("VisitID"), result.getDate("ServiceDate"), result.getString("EventName"),
							result.getString("GithubName"), result.getString("RepoName"), result.getString("Comments"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), true),
							result.getString("ServiceCategory"), result.getString("State")));

				} else {
					lastClientID = thisClientID;

					if (filterOnDate && result.getString("ServiceDate").compareTo(beginDate) < 0) {
						// Don't use attendance if too far back
						removeAttendance = true;
						continue;

					} else
						removeAttendance = false;

					// Create student model for new client
					lastAttendanceModel = new AttendanceModel(thisClientID,
							new StudentNameModel(result.getString("Students.FirstName"),
									result.getString("Students.LastName"), result.getBoolean("isInMasterDb")),
							result.getString("GithubName"),
							new AttendanceEventModel(result.getInt("CLientID"), result.getInt("VisitID"),
									result.getDate("ServiceDate"), result.getString("EventName"),
									result.getString("GithubName"), result.getString("RepoName"),
									result.getString("Comments"), new StudentNameModel(result.getString("FirstName"),
											result.getString("LastName"), true),
									result.getString("ServiceCategory"), result.getString("State")));
					attendanceList.add(lastAttendanceModel);
				}
			}

		} catch (SQLException e) {
			insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
					": " + e.getMessage());
			return;
		}
	}

	public ArrayList<String> getClassNamesByLevel(int filter) {
		ArrayList<String> classList = new ArrayList<String>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt;
				if (filter < NUM_CLASS_LEVELS) { // Classes 0 through 9
					selectStmt = dbConnection.prepareStatement(
							"SELECT EventName FROM Attendance WHERE EventName != '' AND LEFT(EventName,2) = ? "
									+ "AND State = 'completed' GROUP BY EventName ORDER BY EventName;");
					selectStmt.setString(1, String.valueOf(filter) + "@");
				} else {
					selectStmt = dbConnection
							.prepareStatement("SELECT EventName FROM Attendance WHERE EventName != '' AND "
									+ "(LEFT(EventName,2) = ? OR LEFT(EventName,2) = ? OR LEFT(EventName,3) = ?"
									+ " OR Left(EventName,3) = ?) "
									+ "AND State = 'completed' GROUP BY EventName ORDER BY EventName;");
					selectStmt.setString(1, "L@");
					selectStmt.setString(2, "E@");
					selectStmt.setString(3, "AP@");
					selectStmt.setString(4, "E1@");
				}

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					classList.add(result.getString("EventName"));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return classList;
	}

	public ArrayList<StudentNameModel> getAllStudentNames() {
		ArrayList<StudentNameModel> studentList = new ArrayList<StudentNameModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
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

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return studentList;
	}

	public void importAttendance(ArrayList<AttendanceEventModel> importList) {
		ArrayList<AttendanceEventModel> dbList = getAllEvents();
		ArrayList<StudentModel> studentList = getActiveStudents();
		int dbListIdx = 0;
		int dbListSize = dbList.size();
		Collections.sort(importList);
		Collections.sort(dbList);

		AttendanceEventModel dbAttendance;
		for (int i = 0; i < importList.size(); i++) {
			AttendanceEventModel importEvent = importList.get(i);
			String teachers = parseTeacherNames(importEvent.getTeacherNames());

			// If at end of DB list, then default operation is insert (1)
			int compare = 1;
			if (dbListIdx < dbListSize) {
				dbAttendance = dbList.get(dbListIdx);
				compare = dbAttendance.compareTo(importEvent);
				if (compare == 0 && !dbAttendance.getState().equals(importEvent.getState()))
					compare = 2;
			}

			if (compare == 0) {
				// All data matches, so continue through list
				dbListIdx++;
				continue;

			} else if (compare == -1) {
				// Extra events in DB; toss data until caught up with import list
				while (dbListIdx < dbListSize && dbList.get(dbListIdx).compareTo(importEvent) < 0)
					dbListIdx++;

				// Caught up, now compare again and process
				compare = 1;
				if (dbListIdx < dbListSize) {
					dbAttendance = dbList.get(dbListIdx);
					compare = dbAttendance.compareTo(importEvent);
					if (compare == 0 && !dbAttendance.getState().equals(importEvent.getState()))
						compare = 2;
				}

				if (compare == 0) {
					dbListIdx++;

				} else {
					int idx = getClientIdxInStudentList(studentList, importEvent.getClientID());
					if (idx >= 0) {
						if (compare == 1)
							addAttendance(importEvent.getClientID(), importEvent.getVisitID(),
									importEvent.getServiceDateString(), importEvent.getEventName(),
									studentList.get(idx).getNameModel(), teachers, importEvent.getServiceCategory(),
									importEvent.getState());
						else // state field has changed, so update
							updateAttendanceState(importEvent.getClientID(), studentList.get(idx).getNameModel(),
									importEvent.getServiceDateString(), importEvent.getEventName(),
									importEvent.getState());

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
					// Student exists in DB, so add attendance data for this student
					if (compare == 1)
						addAttendance(importEvent.getClientID(), importEvent.getVisitID(),
								importEvent.getServiceDateString(), importEvent.getEventName(),
								studentList.get(idx).getNameModel(), teachers, importEvent.getServiceCategory(),
								importEvent.getState());
					else // state field has changed, so update
						updateAttendanceState(importEvent.getClientID(), studentList.get(idx).getNameModel(),
								importEvent.getServiceDateString(), importEvent.getEventName(), importEvent.getState());

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

	private String parseTeacherNames(String origTeachers) {
		if (origTeachers == null || origTeachers.equals(""))
			return "";

		String teachers = "";
		String[] values = origTeachers.split("\\s*,\\s*");
		for (int i = 0; i < values.length; i++) {
			if (values[i].startsWith("TA-") || values[i].startsWith("Open Lab") || values[i].startsWith("Sub Teacher")
					|| values[i].startsWith("Padres Game") || values[i].startsWith("Make-Up"))
				continue;

			if (!teachers.equals(""))
				teachers += ", ";
			teachers += values[i];
		}
		return teachers;
	}

	private int getClientIdxInStudentList(ArrayList<StudentModel> list, int clientID) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getClientID() == clientID)
				return i;
		}
		return -1;
	}

	private void addAttendance(int clientID, int visitID, String serviceDate, String eventName,
			StudentNameModel nameModel, String teacherNames, String serviceCategory, String state) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement addAttendanceStmt = dbConnection.prepareStatement(
						"INSERT INTO Attendance (ClientID, ServiceDate, EventName, VisitID, TeacherNames, ServiceCategory, State) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?);");

				int col = 1;
				addAttendanceStmt.setInt(col++, clientID);
				addAttendanceStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));
				addAttendanceStmt.setString(col++, eventName);
				addAttendanceStmt.setInt(col++, visitID);
				addAttendanceStmt.setString(col++, teacherNames);
				addAttendanceStmt.setString(col++, serviceCategory);
				addAttendanceStmt.setString(col, state);

				addAttendanceStmt.executeUpdate();
				addAttendanceStmt.close();

				insertLogData(LogDataModel.UPDATE_STUDENT_ATTENDANCE, nameModel, clientID,
						" for " + eventName + " " + serviceDate);
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
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
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, clientID, ": " + e3.getMessage());
				break;
			}
		}
	}

	private void updateAttendanceState(int clientID, StudentNameModel nameModel, String serviceDate, String eventName,
			String state) {
		PreparedStatement updateAttendanceStmt;
		for (int i = 0; i < 2; i++) {
			try {
				// The only fields that should be updated are the comments and repo name
				updateAttendanceStmt = dbConnection
						.prepareStatement("UPDATE Attendance SET State=? WHERE ClientID=? AND ServiceDate=?;");

				int col = 1;
				updateAttendanceStmt.setString(col++, state);
				updateAttendanceStmt.setInt(col++, clientID);
				updateAttendanceStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));

				updateAttendanceStmt.executeUpdate();
				updateAttendanceStmt.close();

				insertLogData(LogDataModel.UPDATE_ATTENDANCE_STATE, nameModel, clientID,
						" for " + eventName + ": " + state + " (" + serviceDate + ")");
				return;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e) {
				StudentNameModel studentModel = new StudentNameModel(nameModel.getFirstName(), nameModel.getLastName(),
						nameModel.getIsInMasterDb());
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, clientID, ": " + e.getMessage());
			}
		}
	}

	public void updateAttendance(int clientID, StudentNameModel nameModel, String serviceDate, String eventName,
			String repoName, String comments) {
		PreparedStatement updateAttendanceStmt;
		for (int i = 0; i < 2; i++) {
			try {
				// The only fields that should be updated are the comments and repo name
				updateAttendanceStmt = dbConnection.prepareStatement(
						"UPDATE Attendance SET Comments=?, RepoName=? WHERE ClientID=? AND ServiceDate=?;");

				int col = 1;
				if (comments != null && comments.length() >= COMMENT_WIDTH)
					comments = comments.substring(0, COMMENT_WIDTH);
				updateAttendanceStmt.setString(col++, comments);
				if (repoName != null && repoName.length() >= REPO_NAME_WIDTH)
					repoName = repoName.substring(0, REPO_NAME_WIDTH);
				updateAttendanceStmt.setString(col++, repoName);
				updateAttendanceStmt.setInt(col++, clientID);
				updateAttendanceStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));

				updateAttendanceStmt.executeUpdate();
				updateAttendanceStmt.close();

				if (repoName != null)
					insertLogData(LogDataModel.UPDATE_GITHUB_COMMENTS, nameModel, clientID,
							" for " + eventName + ", repo: " + repoName + " (" + serviceDate + ")");
				return;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e) {
				StudentNameModel studentModel = new StudentNameModel(nameModel.getFirstName(), nameModel.getLastName(),
						nameModel.getIsInMasterDb());
				insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, clientID, ": " + e.getMessage());
			}
		}
	}

	public void importSchedule(ArrayList<ScheduleModel> importList) {
		ArrayList<ScheduleModel> dbList = getClassSchedule();
		int dbListIdx = 0;
		int dbListSize = dbList.size();

		Collections.sort(importList);

		for (int i = 0; i < importList.size(); i++) {
			ScheduleModel importEvent = importList.get(i);

			// If at end of DB list, then default operation is insert (1)
			int compare = 1;
			if (dbListIdx < dbListSize)
				compare = dbList.get(dbListIdx).compareTo(importEvent);

			if (compare == 0) {
				// All data matches
				dbListIdx++;

			} else if (compare > 0) {
				// Insert new event into DB
				addClassToSchedule(importEvent);

			} else {
				// Extra event(s) in database, so delete them
				while (compare < 0) {
					removeClassFromSchedule(dbList.get(dbListIdx));
					dbListIdx++;

					if (dbListIdx < dbListSize)
						// Continue to compare until dbList catches up
						compare = dbList.get(dbListIdx).compareTo(importEvent);
					else
						// End of database list, insert remaining imports
						compare = 1;
				}
				// One final check to get in sync with importEvent
				if (compare == 0) {
					// Match, so continue incrementing through list
					dbListIdx++;
				} else {
					// Insert new event into DB
					addClassToSchedule(importEvent);
				}
			}
		}

		// Delete extra entries at end of dbList
		while (dbListIdx < dbListSize) {
			removeClassFromSchedule(dbList.get(dbListIdx));
			dbListIdx++;
		}
	}

	public ArrayList<ScheduleModel> getClassSchedule() {
		ArrayList<ScheduleModel> eventList = new ArrayList<ScheduleModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// Get attendance data from the DB for all students that have a github user name
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Schedule ORDER BY DayOfWeek, StartTime, ClassName, Duration;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					String className = result.getString("ClassName");
					eventList.add(new ScheduleModel(result.getInt("ScheduleID"), result.getInt("DayOfWeek"),
							result.getString("StartTime"), result.getInt("Duration"), className));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.SCHEDULE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	public void addClassToSchedule(ScheduleModel importEvent) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement addScheduleStmt = dbConnection.prepareStatement(
						"INSERT INTO Schedule (DayOfWeek, StartTime, Duration, ClassName) VALUES (?, ?, ?, ?);");

				int col = 1;
				String className = importEvent.getClassName();

				addScheduleStmt.setInt(col++, importEvent.getDayOfWeek());
				addScheduleStmt.setString(col++, importEvent.getStartTime());
				addScheduleStmt.setInt(col++, importEvent.getDuration());
				if (className.length() >= CLASS_NAME_WIDTH)
					className = className.substring(0, CLASS_NAME_WIDTH);
				addScheduleStmt.setString(col, className);

				addScheduleStmt.executeUpdate();
				addScheduleStmt.close();

				insertLogData(LogDataModel.ADD_CLASS_TO_SCHEDULE, new StudentNameModel("", "", false), 0,
						": " + importEvent.getClassName() + " on " + dayOfWeek[importEvent.getDayOfWeek()] + " at "
								+ importEvent.getStartTimeFormatted());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Schedule data already exists, do nothing
				break;

			} catch (SQLException e3) {
				insertLogData(LogDataModel.SCHEDULE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e3.getMessage());
				break;
			}
		}
	}

	private void removeClassFromSchedule(ScheduleModel model) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement deleteClassStmt = dbConnection
						.prepareStatement("DELETE FROM Schedule WHERE ScheduleID=?;");

				// Delete class from schedule
				deleteClassStmt.setInt(1, model.getScheduleID());
				deleteClassStmt.executeUpdate();
				deleteClassStmt.close();

				insertLogData(LogDataModel.REMOVE_CLASS_FROM_SCHEDULE, new StudentNameModel("", "", false), 0,
						": " + model.getClassName() + " on " + dayOfWeek[model.getDayOfWeek()] + " at "
								+ model.getStartTimeFormatted());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.SCHEDULE_DB_ERROR, null, 0, ": " + e2.getMessage());
				break;
			}
		}
	}

	public void importCourses(ArrayList<CoursesModel> importList) {
		ArrayList<CoursesModel> dbList = getCourseSchedule("CourseID");
		int dbListIdx = 0;
		int dbListSize = dbList.size();
		int lastCourseIdx = 0;

		Collections.sort(importList);

		for (int i = 0; i < importList.size(); i++) {
			CoursesModel importEvent = importList.get(i);

			// Ignore duplicate courses, only process 1st day
			if (lastCourseIdx == importEvent.getScheduleID())
				continue;
			lastCourseIdx = importEvent.getScheduleID();

			// If at end of DB list, then default operation is insert (1)
			int compare = 1;
			if (dbListIdx < dbListSize)
				compare = dbList.get(dbListIdx).compareTo(importEvent);

			if (compare == 0) {
				// All data matches
				dbListIdx++;

			} else if (compare == 1) {
				// Insert new event into DB
				addCourseToSchedule(importEvent);

			} else if (compare == 2) {
				// Same record but content has changed, so update
				updateCourse(importEvent);
				dbListIdx++;

			} else {
				// Extra event(s) in database, so delete them
				while (compare < 0) {
					removeCourseFromSchedule(dbList.get(dbListIdx));
					dbListIdx++;

					compare = 1;
					if (dbListIdx < dbListSize)
						// Continue to compare until dbList catches up
						compare = dbList.get(dbListIdx).compareTo(importEvent);
				}
				// One final check to get in sync with importEvent
				if (compare == 0) {
					// Match, so continue incrementing through list
					dbListIdx++;

				} else if (compare == 1) {
					// Insert new event into DB
					addCourseToSchedule(importEvent);

				} else if (compare == 2) {
					// Same record but content has changed, so update
					updateCourse(importEvent);
					dbListIdx++;
				}
			}
		}

		// Delete extra entries at end of dbList
		while (dbListIdx < dbListSize) {
			removeCourseFromSchedule(dbList.get(dbListIdx));
			dbListIdx++;
		}
	}

	public ArrayList<CoursesModel> getCourseSchedule(String sortOrder) {
		ArrayList<CoursesModel> eventList = new ArrayList<CoursesModel>();
		PreparedStatement selectStmt;

		for (int i = 0; i < 2; i++) {
			try {
				// Get course data from the DB; if sort order null, then no sort required
				if (sortOrder == null)
					selectStmt = dbConnection.prepareStatement("SELECT * FROM Courses;");
				else
					selectStmt = dbConnection.prepareStatement("SELECT * FROM Courses ORDER BY " + sortOrder + " ASC;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					eventList.add(new CoursesModel(result.getInt("CourseID"), result.getString("EventName"),
							result.getInt("Enrolled")));
				}

				result.close();
				selectStmt.close();
				
				if (sortOrder == null)
					Collections.sort(eventList, new CoursesCompareByDate());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.COURSES_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	private void addCourseToSchedule(CoursesModel courseEvent) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement addCourseStmt = dbConnection
						.prepareStatement("INSERT INTO Courses (CourseID, EventName, Enrolled) " + "VALUES (?, ?, ?);");

				int col = 1;
				addCourseStmt.setInt(col++, courseEvent.getScheduleID());
				addCourseStmt.setString(col++, courseEvent.getEventName());
				addCourseStmt.setInt(col, courseEvent.getEnrollment());

				addCourseStmt.executeUpdate();
				addCourseStmt.close();

				insertLogData(LogDataModel.ADD_COURSES_TO_SCHEDULE, new StudentNameModel("", "", false), 0,
						": " + courseEvent.getEventName());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Schedule data already exists, do nothing
				break;

			} catch (SQLException e3) {
				insertLogData(LogDataModel.COURSES_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e3.getMessage());
				break;
			}
		}
	}

	private void updateCourse(CoursesModel course) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateCourseStmt = dbConnection
						.prepareStatement("UPDATE Courses SET EventName=?, Enrolled=? WHERE CourseID=?;");

				int col = 1;
				updateCourseStmt.setString(col++, course.getEventName());
				updateCourseStmt.setInt(col++, course.getEnrollment());
				updateCourseStmt.setInt(col, course.getScheduleID());

				updateCourseStmt.executeUpdate();
				updateCourseStmt.close();

				insertLogData(LogDataModel.UPDATE_COURSES_INFO, new StudentNameModel("", "", true), 0,
						": " + course.getEventName());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel("", "", true);
				insertLogData(LogDataModel.COURSES_DB_ERROR, studentModel, 0,
						" for " + course.getEventName() + ": " + e2.getMessage());
				break;
			}
		}
	}

	private void removeCourseFromSchedule(CoursesModel course) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement deleteClassStmt = dbConnection
						.prepareStatement("DELETE FROM Courses WHERE CourseID=?;");

				// Delete class from schedule
				deleteClassStmt.setInt(1, course.getScheduleID());
				deleteClassStmt.executeUpdate();
				deleteClassStmt.close();

				insertLogData(LogDataModel.REMOVE_COURSES_FROM_SCHEDULE, new StudentNameModel("", "", false), 0,
						": " + course.getEventName());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.COURSES_DB_ERROR, null, 0, ": " + e2.getMessage());
				break;
			}
		}
	}

	/*
	 * ------- Logging Database Queries -------
	 */
	public void insertLogData(int logType, StudentNameModel studentNameModel, int clientID, String appendedMsg) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
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
				addLogDataStmt.setString(col++, new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"))
						.toString("yyyy-MM-dd HH:mm:ss"));

				addLogDataStmt.executeUpdate();
				addLogDataStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				if (!e2.getMessage().startsWith("Duplicate entry")) {
					// TODO: Can't log this error! What to do instead?
				}
				break;
			}
		}
	}

	public ArrayList<LogDataModel> getLogData() {
		ArrayList<LogDataModel> logData = new ArrayList<LogDataModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * FROM LogData;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					logData.add(new LogDataModel(result.getInt("LogType"), result.getString("LogDate").substring(0, 19),
							new StudentNameModel(result.getString("StudentName"), "", true), result.getInt("ClientID"),
							result.getString("AppendedString")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return logData;
	}

	public void clearLogData() {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement("TRUNCATE LogData");
				selectStmt.executeUpdate();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.LOG_DATA_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
	}
}

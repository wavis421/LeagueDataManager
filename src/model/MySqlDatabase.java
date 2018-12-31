package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JFrame;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

public class MySqlDatabase {
	// Use different port for Tracker App and Import to allow simultaneous connects
	public static final int TRACKER_APP_SSH_PORT = 5000;
	public static final int MINI_TRACKER_SSH_PORT = 5050;
	public static final int STUDENT_IMPORT_SSH_PORT = 6000;
	public static final int SALES_FORCE_SYNC_SSH_PORT = 7000;
	public static final int STUDENT_IMPORT_NO_SSH = 0;

	private static final int MAX_CONNECTION_ATTEMPTS = 3;
	private static final int MAX_CONNECT_ATTEMPTS_LAMBDA = 1;
	private static final int COMMENT_WIDTH = 150;
	private static final int REPO_NAME_WIDTH = 50;
	public static final int CLASS_ATTEND_NUM_DAYS_TO_KEEP = 30;

	public static final String GRAD_MODEL_PROCESSED_FIELD = "Processed";
	public static final String GRAD_MODEL_IN_SF_FIELD = "InSalesForce";
	public static final String GRAD_MODEL_SKIP_LEVEL_FIELD = "SkipLevel";
	public static final String GRAD_MODEL_PROMOTED_FIELD = "Promoted";

	public Connection dbConnection = null;
	private JFrame parent;
	private String awsPassword;
	private MySqlConnection mySqlConnection;
	private int localPort;
	private boolean connectError = false;

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
		this.localPort = localPort;

		mySqlConnection = new MySqlConnection(localPort);
	}

	public boolean getConnectError() {
		return connectError;
	}

	public void clearConnectError() {
		connectError = false;
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
				System.out.println("Connect DB failed: " + e.getMessage());
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
	 * ------- Student Database Queries used for GUI -------
	 */
	public ArrayList<StudentModel> getActiveStudents() {
		ArrayList<StudentModel> nameList = new ArrayList<StudentModel>();
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));

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
							getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear"), result.getString("CurrentClass"), result.getString("Email"),
							result.getString("AcctMgrEmail"), result.getString("EmergencyEmail"),
							result.getString("Phone"), result.getString("AcctMgrPhone"), result.getString("HomePhone"),
							result.getString("EmergencyPhone"), result.getString("CurrentModule"),
							result.getString("CurrentLevel"), result.getString("RegisterClass"), result.getDate("LastVisitDate")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return nameList;
	}

	public ArrayList<StudentModel> getActiveStudentsWithClass() {
		ArrayList<StudentModel> nameList = new ArrayList<StudentModel>();
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students WHERE isInMasterDb AND CurrentClass != '' "
								+ "ORDER BY FirstName, LastName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					nameList.add(new StudentModel(result.getInt("ClientID"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear"), result.getString("CurrentClass"), result.getString("Email"),
							result.getString("AcctMgrEmail"), result.getString("EmergencyEmail"),
							result.getString("Phone"), result.getString("AcctMgrPhone"), result.getString("HomePhone"),
							result.getString("EmergencyPhone"), result.getString("CurrentModule"),
							result.getString("CurrentLevel"), result.getString("RegisterClass"), result.getDate("LastVistiDate")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return nameList;
	}

	public ArrayList<StudentModel> getActiveTAs(String minNumClasses, int minAge, int minLevel) {
		ArrayList<StudentModel> nameList = new ArrayList<StudentModel>();
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));
		String latestBirthdate = today.minusYears(minAge).toString("yyyy-MM-dd");

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Students WHERE isInMasterDb AND TASinceDate != '' "
								+ "AND TAPastEvents >= " + minNumClasses + " AND CurrentLevel >= " + minLevel
								+ " AND Birthdate <= '" + latestBirthdate + "' ORDER BY FirstName, LastName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					nameList.add(new StudentModel(result.getInt("ClientID"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							getAge(today, result.getString("Birthdate")), result.getString("CurrentClass"),
							result.getString("CurrentLevel"), result.getString("TASinceDate"),
							result.getInt("TAPastEvents"), result.getString("Email"), result.getString("Phone")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return nameList;
	}

	private StudentModel getStudentCurrentLevel(int clientID) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT CurrentLevel, CurrentModule FROM Students WHERE ClientID=?;");
				selectStmt.setInt(1, clientID);
				ResultSet result = selectStmt.executeQuery();

				if (result.next()) {
					return new StudentModel(clientID, result.getString("CurrentLevel"), result.getString("CurrentModule"));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return null;
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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false),
						clientID, ": " + e2.getMessage());
				break;
			}
		}
	}

	public ArrayList<StudentModel> getStudentsNotInMasterDB() {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));

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
							getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear"), result.getString("CurrentClass"), result.getString("Email"),
							result.getString("AcctMgrEmail"), result.getString("EmergencyEmail"),
							result.getString("Phone"), result.getString("AcctMgrPhone"), result.getString("HomePhone"),
							result.getString("EmergencyPhone"), result.getString("CurrentModule"),
							result.getString("CurrentLevel"), result.getString("RegisterClass"), result.getDate("LastVisitDate")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
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
					MySqlDbLogging.insertLogData(LogDataModel.REMOVE_INACTIVE_STUDENT,
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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
	}

	public ArrayList<StudentModel> getStudentByClientID(int clientID) {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));

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
							getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear"), result.getString("CurrentClass"), result.getString("Email"),
							result.getString("AcctMgrEmail"), result.getString("EmergencyEmail"),
							result.getString("Phone"), result.getString("AcctMgrPhone"), result.getString("HomePhone"),
							result.getString("EmergencyPhone"), result.getString("CurrentModule"),
							result.getString("CurrentLevel"), result.getString("RegisterClass"), result.getDate("LastVisitDate")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false),
						clientID, ": " + e2.getMessage());
				break;
			}
		}
		return studentList;
	}

	public ArrayList<StudentModel> getStudentsUsingFlag(String flagName) {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));

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
							getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear"), result.getString("CurrentClass"), result.getString("Email"),
							result.getString("AcctMgrEmail"), result.getString("EmergencyEmail"),
							result.getString("Phone"), result.getString("AcctMgrPhone"), result.getString("HomePhone"),
							result.getString("EmergencyPhone"), result.getString("CurrentModule"),
							result.getString("CurrentLevel"), result.getString("RegisterClass"), result.getDate("LastVisitDate")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return studentList;
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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, student.getNameModel(),
						student.getClientID(), ": " + e2.getMessage());
				break;
			}
		}
	}

	public void updateLastEventInfoByStudent(int clientID, String eventName, String lastVisitDate, String module) {		
		boolean addModule = false;
		String separator = "";
		String updateFields = "";
		
		if (eventName != null) {
			updateFields = "CurrentClass=? ";
			separator = ", ";
		}
		if (module != null) {
			if (module.equals("NULL"))
				// Module cleared due to new class level
				updateFields += separator + "CurrentModule=NULL ";
			else {
				updateFields += separator + "CurrentModule=? ";
				addModule = true;
			}
		}
		if (lastVisitDate != null)
			updateFields += separator + "LastVisitDate=? ";

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateStudentStmt = dbConnection
							.prepareStatement("UPDATE Students SET " + updateFields + "WHERE ClientID=?;");
				
				int col = 1;
				if (eventName != null)
					updateStudentStmt.setString(col++, eventName);
				if (addModule)
					updateStudentStmt.setString(col++, module);
				if (lastVisitDate != null)
					updateStudentStmt.setDate(col++, java.sql.Date.valueOf(lastVisitDate));
				updateStudentStmt.setInt(col, clientID);

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false),
						clientID, ": " + e2.getMessage());
				break;
			}
		}
	}

	public void clearRegisteredClasses() {	
		// Clear registered classes field for all students (updated daily)
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateStudentStmt = dbConnection
							.prepareStatement("UPDATE Students SET RegisterClass = '';");

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false),
						0, ": " + e2.getMessage());
				break;
			}
		}
	}

	public void updateRegisteredClassByStudent(int clientID, String eventName) {
		// Update registered (future) class for a student
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateStudentStmt = dbConnection
							.prepareStatement("UPDATE Students SET RegisterClass=? WHERE ClientID=?;");
				updateStudentStmt.setString(1, eventName);
				updateStudentStmt.setInt(2, clientID);

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false),
						clientID, ": " + e2.getMessage());
				break;
			}
		}
	}

	private void updateStudentModule(int clientID, StudentModel student, String repoName) {
		// Extract module from repo name: level must match student current level
		if (repoName != null && student != null) {
			String currLevel = student.getCurrentLevel();
			if (!currLevel.equals("") && currLevel.charAt(0) <= '5') {
				int idx;
				// Github classroom names are formatted level-0-module-3 OR level3-module1
				if (repoName.startsWith("level" + currLevel + "-module"))
					idx = 13;
				else if (repoName.startsWith("level-" + currLevel + "-module"))
					idx = 14;
				else if (repoName.startsWith("old-level" + currLevel + "-module")) {
					System.out.println(student.getNameModel().toString() + ": " + repoName);
					idx = 17;
				} else if (currLevel.equals("5") && repoName.length() > 10 
						&& repoName.startsWith("level5-0") && repoName.charAt(9) == '-') {
					System.out.println(student.getNameModel().toString() + ": " + repoName.charAt(8) + ", " + repoName);
					idx = 8;
				} else {
					System.out.println(student.getNameModel().toString() + ": (unknown) " + repoName);
					return; // No matching level in repo name
				}

				if (repoName.charAt(idx) == '-')
					idx++;

				// Now find module
				if (repoName.charAt(idx) >= '0' && repoName.charAt(idx) <= '9' // module #
						&& (repoName.length() == (idx + 1) || repoName.charAt(idx + 1) == '-')) {
					String newModuleName = repoName.substring(idx, idx + 1);
			
					// Done parsing repo name; update student module if changed
					if (newModuleName.compareTo(student.getCurrentModule()) > 0) {
						updateLastEventInfoByStudent(clientID, null, null, newModuleName);
					}
				}
			}
		}
	}

	private Double getAge(DateTime today, String birthdate) {
		Double days = 0.0;

		if (birthdate != null && !birthdate.equals("")) {
			days = (Double) (Days.daysBetween(new DateTime(birthdate), today).getDays() / 365.25);
		}

		return days;
	}

	/*
	 * ------- Attendance Database Queries used for GUI -------
	 */
	public ArrayList<AttendanceModel> getAllAttendance() {
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();

		// Try twice: if DB no longer connected, the exception code will re-connect
		for (int i = 0; i < 2; i++) {
			try {
				// Get 1st 4 attendance records for each client; use sorted attendance table
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * "
						+ "FROM (SELECT Students.ClientID as StudID, ServiceDate, ServiceTime, EventName, VisitID, "
						+ "         ServiceCategory, State, LastSFState, FirstName, LastName, isInMasterDb, "
						+ "         GithubName, Birthdate, RepoName, Comments, TeacherNames, ClassLevel, "
						+ "         @num := IF(@lastId = Students.ClientID, @num + 1, if (@lastId := Students.ClientId, 1, 1)) as row "
						+ "      FROM SortedAttendance, Students "
						+ "      WHERE isInMasterDb AND SortedAttendance.ClientID = Students.ClientID "
						+ "            AND State = 'completed' "
						+ "      ORDER BY Students.ClientID, ServiceDate DESC, EventName) AS Base "
						+ "WHERE row <= 4;");
				ResultSet result = selectStmt.executeQuery();
				getAttendanceList(attendanceList, result, "StudID", false);
				Collections.sort(attendanceList);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	public ArrayList<AttendanceEventModel> getAllEvents(String startDate) {
		ArrayList<AttendanceEventModel> eventList = new ArrayList<AttendanceEventModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// Get attendance data from the DB for all students
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE Attendance.ClientID = Students.ClientID "
								+ "AND (State = 'completed' OR State = 'registered') AND ServiceDate >= ? "
								+ "ORDER BY Attendance.ClientID ASC, ServiceDate DESC, VisitID ASC;");
				selectStmt.setString(1, startDate);
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					eventList.add(new AttendanceEventModel(result.getInt("ClientID"), result.getInt("VisitID"),
							result.getDate("ServiceDate"), result.getString("ServiceTime"), result.getString("EventName"),
							result.getString("GithubName"), result.getString("RepoName"), result.getString("Comments"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), true),
							result.getString("ServiceCategory"), result.getString("State"), result.getString("LastSFState"),
							result.getString("TeacherNames"), result.getString("ClassLevel")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	public ArrayList<AttendanceModel> getAttendanceByClassName(String className) {
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();
		ArrayList<AttendanceModel> listByClient;
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));
		String sinceDate = today.minusDays(CLASS_ATTEND_NUM_DAYS_TO_KEEP).toString("yyyy-MM-dd");
		String endDate = today.plusDays(7).toString("yyyy-MM-dd");

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE isInMasterDb AND Attendance.ClientID = Students.ClientID "
								+ "AND ((State = 'completed' AND EventName = CurrentClass) OR State = 'registered') "
								+ "AND EventName=? AND ServiceDate > ? AND ServiceDate < ? GROUP BY Students.ClientID;");
				selectStmt.setString(1, className);
				selectStmt.setString(2, sinceDate);
				selectStmt.setString(3, endDate);

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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	public ArrayList<AttendanceModel> getAttendanceByCourseName(String courseName) {
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();
		ArrayList<AttendanceModel> listByClient;
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT Students.ClientID, FirstName, LastName, GithubName, State, "
								+ "LastSFState, ServiceCategory, TeacherNames, Birthdate, ClassLevel "
								+ "FROM Attendance, Students "
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
						attendanceList.add(new AttendanceModel(thisClientID, name,
								getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
								new AttendanceEventModel(thisClientID, 0, null, "   ", "",
										result.getString("GithubName"), "", "", name,
										result.getString("ServiceCategory"), result.getString("State"),
										result.getString("LastSFState"), result.getString("TeacherNames"),
										result.getString("ClassLevel"))));
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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
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
								+ "AND Attendance.ClientID=? ORDER BY Attendance.ClientID, ServiceDate DESC, EventName LIMIT 52;");
				selectStmt.setInt(1, Integer.parseInt(clientID));

				ResultSet result = selectStmt.executeQuery();
				getAttendanceList(attendanceList, result, "Students.ClientID", false);
				Collections.sort(attendanceList);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
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
						String classLevel = result.getString("ClassLevel");
						count++;

						// Process if student is in levels 0 - 4
						if ((eventName.charAt(0) >= '0' && eventName.charAt(0) <= '5') 
								|| (!classLevel.equals("") && classLevel.charAt(0) >= '0' && classLevel.charAt(0) <= '5')) {
							// Save this record for possible addition to list
							DateTime serviceDate = new DateTime(result.getDate("ServiceDate"));
							int dowInt = serviceDate.getDayOfWeek();
							if (dowInt > 6)
								dowInt -= 7;
							lastGithubModel = new GithubModel(thisClientID,
									result.getString("FirstName") + " " + result.getString("LastName"),
									serviceDate.toString("EEEEE"), dowInt, eventName, result.getString("GithubName"),
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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}

		Collections.sort(githubList); // Sort by student name
		return githubList;
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
							result.getDate("ServiceDate"), result.getString("ServiceTime"),	result.getString("EventName"),
							result.getString("GithubName"), result.getString("RepoName"), result.getString("Comments"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), true),
							result.getString("ServiceCategory"), result.getString("State"),
							result.getString("LastSFState"), result.getString("TeacherNames"),
							result.getString("ClassLevel")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	private void getAttendanceList(ArrayList<AttendanceModel> attendanceList, ResultSet result, String clientIdString,
			boolean filterOnDate) {
		int lastClientID = -1;
		AttendanceModel lastAttendanceModel = null;
		boolean removeAttendance = false;
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));
		String beginDate = today.minusDays(CLASS_ATTEND_NUM_DAYS_TO_KEEP).toString("yyyy-MM-dd");

		// Process DB query result containing attendance by grouping the attendance by
		// student and then adding the resulting Attendance Model to the attendanceList.
		try {
			while (result.next()) {
				int thisClientID = result.getInt(clientIdString);
				if (lastClientID == thisClientID) {
					if (filterOnDate && removeAttendance) {
						// Don't use attendance if too far back
						continue;
					}
					// Add more data to existing client
					lastAttendanceModel.addAttendanceData(new AttendanceEventModel(thisClientID, 
							result.getInt("VisitID"), result.getDate("ServiceDate"), result.getString("ServiceTime"),
							result.getString("EventName"), result.getString("GithubName"), result.getString("RepoName"),
							result.getString("Comments"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"), true),
							result.getString("ServiceCategory"), result.getString("State"),
							result.getString("LastSFState"), result.getString("TeacherNames"),
							result.getString("ClassLevel")));

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
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							new AttendanceEventModel(thisClientID, result.getInt("VisitID"),
									result.getDate("ServiceDate"), result.getString("ServiceTime"),
									result.getString("EventName"), result.getString("GithubName"),
									result.getString("RepoName"), result.getString("Comments"),
									new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
											true),
									result.getString("ServiceCategory"), result.getString("State"),
									result.getString("LastSFState"), result.getString("TeacherNames"),
									result.getString("ClassLevel")));
					attendanceList.add(lastAttendanceModel);
				}
			}

		} catch (SQLException e) {
			MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
					": " + e.getMessage());
			return;
		}
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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return studentList;
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
				
				// Now update student latest module using repo name
				updateStudentModule(clientID, getStudentCurrentLevel(clientID), repoName);
				return;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e) {
				StudentNameModel studentModel = new StudentNameModel(nameModel.getFirstName(), nameModel.getLastName(),
						nameModel.getIsInMasterDb());
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, clientID,
						": " + e.getMessage());
			}
		}
	}

	public void updateAttendLevelChanges(int visitID, String state) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateAttendanceStmt = dbConnection
						.prepareStatement("UPDATE Attendance SET LastSFState = ? WHERE VisitID = ?;");

				updateAttendanceStmt.setString(1, state);
				updateAttendanceStmt.setInt(2, visitID);

				updateAttendanceStmt.executeUpdate();
				updateAttendanceStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				}

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						" updating LastSFState: " + e2.getMessage());
				break;
			}
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

				int totalCount = 0;
				while (result.next()) {
					totalCount++;
					ScheduleModel sched = new ScheduleModel(result.getInt("ScheduleID"), result.getInt("DayOfWeek"),
							result.getString("StartTime"), result.getInt("Duration"), result.getString("ClassName"));
					sched.setMiscSchedFields(result.getInt("NumStudents"), result.getString("Youngest"),
							result.getString("Oldest"), result.getString("AverageAge"),
							result.getString("ModuleCount"));
					eventList.add(sched);
				}
				System.out.println("Total # classes: " + totalCount);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.SCHEDULE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	public ArrayList<ScheduleModel> getClassDetails(boolean[] dowSelectList) {
		ArrayList<ScheduleModel> eventList = new ArrayList<ScheduleModel>();

		// Create dow select fields for the mySql command
		String dowSelect = " AND (";
		boolean isDowSelected = false;
		for (int i = 0; i < dowSelectList.length; i++) {
			if (dowSelectList[i]) {
				if (isDowSelected)
					dowSelect += " OR ";
				dowSelect += "DayOfWeek=" + i;
				isDowSelected = true;
			}
		}
		if (isDowSelected)
			dowSelect += ") ";
		else
			dowSelect = "";

		for (int i = 0; i < 2; i++) {
			try {
				// Get schedule data for weekly classes
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Schedule WHERE ((LEFT(ClassName,1) >= '0' AND LEFT(ClassName,1) <= '7') "
								+ "OR LEFT(ClassName,2) = 'AD' OR LEFT(ClassName,2) = 'AG' OR LEFT(ClassName,2) = 'PG') "
								+ dowSelect + "ORDER BY DayOfWeek, StartTime, ClassName;");
				ResultSet result = selectStmt.executeQuery();

				int totalCount = 0;
				while (result.next()) {
					totalCount++;
					ScheduleModel sched = new ScheduleModel(result.getInt("ScheduleID"), result.getInt("DayOfWeek"),
							result.getString("StartTime"), result.getInt("Duration"), result.getString("ClassName"));
					sched.setMiscSchedFields(result.getInt("NumStudents"), result.getString("Youngest"),
							result.getString("Oldest"), result.getString("AverageAge"),
							result.getString("ModuleCount"));
					eventList.add(sched);
				}
				System.out.println("Total # classes: " + totalCount);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.SCHEDULE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
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
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.COURSES_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}

	/*
	 * ------- Graduation Data used for GUI -------
	 */
	public String getStartDateByClientIdAndLevel(int clientID, int level) {
		String levelString = ((Integer) level).toString();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT ServiceDate, EventName, ClassLevel FROM Attendance "
								+ "WHERE ClientID = ? AND State = 'completed' ORDER BY ServiceDate ASC;");
				selectStmt.setInt(1, clientID);

				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					String eventName = result.getString("EventName");
					String classLevel = result.getString("ClassLevel");
					if ((eventName.charAt(1) == '@' && eventName.startsWith(levelString))
							|| classLevel.equals(levelString)) {
						String startDate = result.getDate("ServiceDate").toString();
						if (startDate.compareTo("2017-09-30") < 0)
							return "";
						else
							return startDate;
					}
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return "";
	}

	public void addGraduationRecord(GraduationModel gradModel) {
		for (int i = 0; i < 2; i++) {
			try {
				// Insert graduation record into database
				String cmdString = "INSERT INTO Graduation (ClientID, GradLevel, SkipLevel, Promoted, EndDate, CurrentClass";
				String values = ") VALUES (?, ?, ?, ?, ?, ?";

				// Don't update dates or score if no data
				if (!gradModel.getStartDate().equals("")) {
					cmdString += ", StartDate";
					values += ", ?";
				}
				if (!gradModel.getScore().equals("")) {
					cmdString += ", Score";
					values += ", ?";
				}

				// Now add graduation info to database
				PreparedStatement addGrad = dbConnection.prepareStatement(cmdString + values + ");");

				// Fill in the input fields
				int col = 1;
				addGrad.setInt(col++, gradModel.getClientID());
				addGrad.setInt(col++, gradModel.getGradLevel());
				addGrad.setBoolean(col++, gradModel.isSkipLevel());
				addGrad.setBoolean(col++,  gradModel.isPromoted());
				addGrad.setDate(col++, java.sql.Date.valueOf(gradModel.getEndDate()));
				addGrad.setString(col++,  gradModel.getCurrentClass());
				if (!gradModel.getStartDate().equals(""))
					addGrad.setDate(col++, java.sql.Date.valueOf(gradModel.getStartDate()));
				if (!gradModel.getScore().equals(""))
					addGrad.setString(col++, gradModel.getScore());

				// Execute update
				addGrad.executeUpdate();
				addGrad.close();
				break;

			} catch (MySQLIntegrityConstraintViolationException e0) {
				// Record already exists in database, so update instead
				updateGraduationRecord(gradModel);
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false),
						gradModel.getClientID(), " for Graduation: " + e2.getMessage());
				break;
			}
		}
	}

	private void updateGraduationRecord(GraduationModel gradModel) {
		// Graduation records are uniquely identified by clientID & level pair.
		// Update only EndDate, Score, SkipLevel, Promoted. Set 'in SF' false to forces update.
		for (int i = 0; i < 2; i++) {
			try {
				// Update graduation record in database
				String cmdString = "UPDATE Graduation SET EndDate=?, " + GRAD_MODEL_IN_SF_FIELD + "=0";

				// Only update fields if valid
				if (!gradModel.getScore().equals(""))
					cmdString += ", Score=?";
				if (gradModel.isSkipLevel())
					cmdString += ", SkipLevel=?";
				if (gradModel.isPromoted())
					cmdString += ", Promoted=?";

				// Update database
				PreparedStatement updateGraduateStmt = dbConnection
						.prepareStatement(cmdString + " WHERE ClientID=? AND GradLevel=?;");

				// Fill in the input fields
				int col = 1;
				updateGraduateStmt.setDate(col++, java.sql.Date.valueOf(gradModel.getEndDate()));
				if (!gradModel.getScore().equals(""))
					updateGraduateStmt.setString(col++, gradModel.getScore());
				if (gradModel.isSkipLevel())
					updateGraduateStmt.setBoolean(col++, true);
				if (gradModel.isPromoted())
					updateGraduateStmt.setBoolean(col++, true);
				updateGraduateStmt.setInt(col++, gradModel.getClientID());
				updateGraduateStmt.setInt(col++, gradModel.getGradLevel());

				// Execute update
				updateGraduateStmt.executeUpdate();
				updateGraduateStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false),
						gradModel.getClientID(), " for Graduation: " + e2.getMessage());
				break;
			}
		}
	}

	public void updateGraduationField(int clientID, String studentName, String gradLevel, String fieldName,
			boolean newValue) {
		// Only the boolean flags may be updated (InSalesForce, Processed)
		if (!fieldName.equals(GRAD_MODEL_IN_SF_FIELD) && !fieldName.equals(GRAD_MODEL_PROCESSED_FIELD)) {
			System.out.println("Graduation field name invalid: " + fieldName);
			return;
		}

		// Graduation records are uniquely identified by clientID & level pair.
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateGraduateStmt = dbConnection.prepareStatement(
						"UPDATE Graduation SET " + fieldName + "=? WHERE ClientID=? AND GradLevel=?;");

				updateGraduateStmt.setInt(1, newValue ? 1 : 0);
				updateGraduateStmt.setInt(2, clientID);
				updateGraduateStmt.setInt(3, Integer.parseInt(gradLevel));

				updateGraduateStmt.executeUpdate();
				updateGraduateStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR,
						new StudentNameModel(studentName, "", false), clientID, " for Graduation: " + e2.getMessage());
				break;
			}
		}
	}

	public void removeProcessedGraduations() {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * FROM Graduation;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					// When both flags are true, record can be removed
					boolean inSf = result.getBoolean(GRAD_MODEL_IN_SF_FIELD);
					boolean processed = result.getBoolean(GRAD_MODEL_PROCESSED_FIELD);

					if (inSf && processed) {
						// Graduation record has been processed, so remove from DB
						PreparedStatement deleteGradStmt = dbConnection
								.prepareStatement("DELETE FROM Graduation WHERE ClientID=? AND GradLevel=?;");

						// Delete student
						deleteGradStmt.setInt(1, result.getInt("ClientID"));
						deleteGradStmt.setInt(2, result.getInt("GradLevel"));
						deleteGradStmt.executeUpdate();
						deleteGradStmt.close();
					}
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						" for Graduation: " + e2.getMessage());
				break;
			}
		}
	}

	public ArrayList<GraduationModel> getAllGradRecords() {
		ArrayList<GraduationModel> gradList = new ArrayList<GraduationModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Graduation, Students WHERE Students.ClientID = Graduation.ClientID "
								+ "ORDER BY FirstName, LastName, GradLevel;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					gradList.add(new GraduationModel(result.getInt("ClientID"),
							result.getString("FirstName") + " " + result.getString("LastName"),
							result.getInt("GradLevel"), result.getString("Score"), result.getString("CurrentClass"),
							result.getString("StartDate"), result.getString("EndDate"), 
							result.getBoolean(GRAD_MODEL_IN_SF_FIELD), result.getBoolean(GRAD_MODEL_PROCESSED_FIELD),
							result.getBoolean(GRAD_MODEL_SKIP_LEVEL_FIELD), result.getBoolean(GRAD_MODEL_PROMOTED_FIELD)));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return gradList;
	}

	/*
	 * ------- Location Data used for GUI -------
	 */
	public ArrayList<LocationModel> getLocationList() {
		ArrayList<LocationModel> locList = new ArrayList<LocationModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * FROM Location;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					locList.add(new LocationModel(result.getInt("LocIdx"), result.getString("LocCode"),
							result.getString("LocName"), result.getString("LocNameLong"), result.getString("Notes")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					connectDatabase();
				} else
					connectError = true;

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return locList;
	}

	public class CoursesCompareByDate implements Comparator<CoursesModel> {
		@Override
		public int compare(CoursesModel c1, CoursesModel c2) {
			int comp = c1.getDate().compareTo(c2.getDate());
			if (comp == 0) {
				comp = c1.getEventName().compareTo(c2.getEventName());
			}
			return comp;
		}
	}
}

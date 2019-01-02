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
import org.joda.time.Days;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

/** 
 * MySqlDatase: This class is used to access the AWS mySql Student Tracker database.
 * 
 * This code is used by both the Student Tracker and the nightly data Importer code that
 * runs on AWS Lambda. When changing the mySql database content or this file, then
 * the model package should be exported as a jar file for the Importer code in order for
 * the Importer to stay in sync with the database.
 * 
 * MySql code used only by the Student Tracker GUI is in the model_for_gui package.
 *  
 * @author wavis
 *
 */
public class MySqlDatabase {
	// Use different port for Tracker App and Import to allow simultaneous connects
	public static final int TRACKER_APP_SSH_PORT = 5000;
	public static final int MINI_TRACKER_SSH_PORT = 5050;
	public static final int STUDENT_IMPORT_SSH_PORT = 6000;
	public static final int SALES_FORCE_SYNC_SSH_PORT = 7000;
	public static final int STUDENT_IMPORT_NO_SSH = 0;

	private static final int MAX_CONNECTION_ATTEMPTS = 3;
	private static final int MAX_CONNECT_ATTEMPTS_LAMBDA = 1;
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

	public void setConnectError(boolean err) {
		connectError = err;
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
	 * ------- Schedule Database Queries used by both Tracker and Importer -------
	 */
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
	 * ------- Graduation Data used by both Tracker and Importer -------
	 */
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
							result.getBoolean(GRAD_MODEL_SKIP_LEVEL_FIELD),
							result.getBoolean(GRAD_MODEL_PROMOTED_FIELD)));
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
	 * ------- Location Data used by both Tracker and Importer -------
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

	/*
	 * ------- Miscellaneous methods used by both Tracker and Importer -------
	 */
	public Double getAge(DateTime today, String birthdate) {
		Double days = 0.0;

		if (birthdate != null && !birthdate.equals("")) {
			days = (Double) (Days.daysBetween(new DateTime(birthdate), today).getDays() / 365.25);
		}

		return days;
	}

	private class CoursesCompareByDate implements Comparator<CoursesModel> {
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

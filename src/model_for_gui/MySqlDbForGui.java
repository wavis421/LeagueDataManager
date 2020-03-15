package model_for_gui;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

import model.AttendanceEventModel;
import model.LogDataModel;
import model.MySqlDatabase;
import model.MySqlDbLogging;
import model.ScheduleModel;
import model.StudentModel;
import model.StudentNameModel;

/**
 * MySqlDbForGui: This class is used to access the AWS mySql Student Tracker
 *                database to support the Student Tracker GUI.
 * 
 *                Any tracker database access not related to the GUI is in the model package.
 * 
 * @author wavis
 *
 */

public class MySqlDbForGui {
	MySqlDatabase sqlDb;

	public MySqlDbForGui(MySqlDatabase sqlDb) {
		this.sqlDb = sqlDb;
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
				PreparedStatement selectStmt = sqlDb.dbConnection
						.prepareStatement("SELECT * FROM Students WHERE isInMasterDb ORDER BY FirstName, LastName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					nameList.add(new StudentModel(result.getInt("ClientID"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							sqlDb.getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear"), result.getString("CurrentClass"), result.getString("Email"),
							result.getString("AcctMgrEmail"), result.getString("EmergencyEmail"),
							result.getString("Phone"), result.getString("AcctMgrPhone"), result.getString("HomePhone"),
							result.getString("EmergencyPhone"), result.getString("CurrentModule"),
							result.getString("CurrentLevel"), result.getString("RegisterClass"),
							result.getDate("LastVisitDate")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

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
				PreparedStatement selectStmt = sqlDb.dbConnection
						.prepareStatement("SELECT * FROM Students WHERE isInMasterDb AND TASinceDate != '' "
								+ "AND TAPastEvents >= " + minNumClasses + " AND CurrentLevel >= " + minLevel
								+ " AND Birthdate <= '" + latestBirthdate + "' ORDER BY FirstName, LastName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					nameList.add(new StudentModel(result.getInt("ClientID"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							sqlDb.getAge(today, result.getString("Birthdate")), result.getString("CurrentClass"),
							result.getString("CurrentLevel"), result.getString("TASinceDate"),
							result.getInt("TAPastEvents"), result.getString("Email"), result.getString("Phone")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return nameList;
	}

	public ArrayList<StudentModel> getStudentsNotInMasterDB() {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement(
						"SELECT * FROM Students WHERE NOT isInMasterDb ORDER BY FirstName, LastName;");
				ResultSet result = selectStmt.executeQuery();

				while (result.next()) {
					studentList.add(new StudentModel(result.getInt("ClientID"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							sqlDb.getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear"), result.getString("CurrentClass"), result.getString("Email"),
							result.getString("AcctMgrEmail"), result.getString("EmergencyEmail"),
							result.getString("Phone"), result.getString("AcctMgrPhone"), result.getString("HomePhone"),
							result.getString("EmergencyPhone"), result.getString("CurrentModule"),
							result.getString("CurrentLevel"), result.getString("RegisterClass"),
							result.getDate("LastVisitDate")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return studentList;
	}

	public ArrayList<StudentModel> getStudentByClientID(int clientID) {
		ArrayList<StudentModel> studentList = new ArrayList<StudentModel>();
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = sqlDb.dbConnection
						.prepareStatement("SELECT * FROM Students WHERE ClientID=?;");
				selectStmt.setInt(1, clientID);

				ResultSet result = selectStmt.executeQuery();
				if (result.next()) {
					studentList.add(new StudentModel(result.getInt("ClientID"),
							new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
									result.getBoolean("isInMasterDb")),
							sqlDb.getAge(today, result.getString("Birthdate")), result.getString("GithubName"),
							result.getInt("Gender"), result.getDate("StartDate"), result.getInt("Location"),
							result.getInt("GradYear"), result.getString("CurrentClass"), result.getString("Email"),
							result.getString("AcctMgrEmail"), result.getString("EmergencyEmail"),
							result.getString("Phone"), result.getString("AcctMgrPhone"), result.getString("HomePhone"),
							result.getString("EmergencyPhone"), result.getString("CurrentModule"),
							result.getString("CurrentLevel"), result.getString("RegisterClass"),
							result.getDate("LastVisitDate")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false),
						clientID, ": " + e2.getMessage());
				break;
			}
		}
		return studentList;
	}

	public ArrayList<GithubModel> getStudentsWithNoRecentGithub(String sinceDate, int minClassesWithoutGithub) {
		ArrayList<GithubModel> githubList = new ArrayList<GithubModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement(
						"SELECT * FROM Attendance, Students WHERE isInMasterDb AND Attendance.ClientID = Students.ClientID "
								+ "AND State = 'completed' AND ServiceDate > ? AND (EventName LIKE 'Java@%' OR EventName LIKE '%Make-Up%') "
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
						String currLevel = result.getString("CurrentLevel");
						count++;

						// Process if student is in levels 0 - 5
						if (!currLevel.equals("") && currLevel.charAt(0) >= '0' && currLevel.charAt(0) <= '5') {
							// Save this record for possible addition to list
							DateTime serviceDate = new DateTime(result.getDate("ServiceDate"));
							int dowInt = serviceDate.getDayOfWeek();
							if (dowInt > 6)
								dowInt -= 7;
							lastGithubModel = new GithubModel(thisClientID,
									result.getString("FirstName") + " " + result.getString("LastName"), currLevel,
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
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}

		Collections.sort(githubList); // Sort by student name
		return githubList;
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
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement("SELECT * "
						+ "FROM (SELECT Students.ClientID as StudID, ServiceDate, ServiceTime, EventName, VisitID, "
						+ "         ServiceCategory, State, LastSFState, FirstName, LastName, CurrentLevel, isInMasterDb, "
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
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	public ArrayList<AttendanceModel> getAttendanceByClassName(String className) {
		ArrayList<AttendanceModel> attendanceList = new ArrayList<AttendanceModel>();
		ArrayList<AttendanceModel> listByClient;
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));
		String sinceDate = today.minusDays(MySqlDatabase.CLASS_ATTEND_NUM_DAYS_TO_KEEP).toString("yyyy-MM-dd");
		String endDate = today.plusDays(7).toString("yyyy-MM-dd");

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement(
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
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

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
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement(
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
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

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
				PreparedStatement selectStmt = sqlDb.dbConnection
						.prepareStatement("SELECT Students.ClientID, FirstName, LastName, GithubName, State, "
								+ "CurrentLevel, LastSFState, ServiceCategory, TeacherNames, Birthdate, ClassLevel "
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
								sqlDb.getAge(today, result.getString("Birthdate")), result.getString("CurrentLevel"), result.getString("GithubName"),
								new AttendanceEventModel(thisClientID, 0, null, "   ", "",
										result.getString("GithubName"), "", "", null, name,
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
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

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
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement(
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
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return attendanceList;
	}

	private void getAttendanceList(ArrayList<AttendanceModel> attendanceList, ResultSet result, String clientIdString,
			boolean filterOnDate) {
		int lastClientID = -1;
		AttendanceModel lastAttendanceModel = null;
		boolean removeAttendance = false;
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));
		String beginDate = today.minusDays(MySqlDatabase.CLASS_ATTEND_NUM_DAYS_TO_KEEP).toString("yyyy-MM-dd");

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
					lastAttendanceModel
							.addAttendanceData(new AttendanceEventModel(thisClientID, result.getInt("VisitID"),
									result.getDate("ServiceDate"), result.getString("ServiceTime"),
									result.getString("EventName"), result.getString("GithubName"),
									result.getString("RepoName"), result.getString("Comments"), null,
									new StudentNameModel(result.getString("FirstName"), result.getString("LastName"),
											true),
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
							sqlDb.getAge(today, result.getString("Birthdate")), result.getString("CurrentLevel"), result.getString("GithubName"),
							new AttendanceEventModel(thisClientID, result.getInt("VisitID"),
									result.getDate("ServiceDate"), result.getString("ServiceTime"),
									result.getString("EventName"), result.getString("GithubName"),
									result.getString("RepoName"), result.getString("Comments"), null,
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

	/*
	 * ------- Course & Class Database Queries used for GUI -------
	 */
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
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement(
						"SELECT * FROM Schedule WHERE (LEFT(ClassName,4) = 'Java') "
								+ dowSelect + "ORDER BY DayOfWeek, StartTime, ClassName;");
				ResultSet result = selectStmt.executeQuery();

				int totalCount = 0;
				while (result.next()) {
					totalCount++;
					ScheduleModel sched = new ScheduleModel(result.getInt("ScheduleID"), result.getInt("DayOfWeek"),
							result.getString("StartTime"), result.getInt("Duration"), result.getString("ClassName"));
					sched.setMiscSchedFields(result.getInt("NumStudents"), result.getString("Youngest"),
							result.getString("Oldest"), result.getString("AverageAge"),
							result.getString("ModuleCount"), result.getString("Room"), result.getBoolean("RoomMismatch"));
					eventList.add(sched);
				}
				System.out.println("Total # classes: " + totalCount);

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				} else
					sqlDb.setConnectError(true);

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.SCHEDULE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return eventList;
	}
}

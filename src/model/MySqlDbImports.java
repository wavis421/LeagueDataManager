package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

/**
 * MySqlDbImports
 * 
 * @author wavis
 *
 *         This file imports data from Pike13 and updates the Student Trackr AWS
 *         Database.
 * 
 */
public class MySqlDbImports {
	private static final int CLASS_NAME_WIDTH = 40;

	private MySqlDatabase sqlDb;

	private static final String[] dayOfWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
			"Saturday" };

	public MySqlDbImports(MySqlDatabase sqlDb) {
		this.sqlDb = sqlDb;
	}

	/*
	 * ------- Student Import Database Queries -------
	 */
	public void importStudents(ArrayList<StudentImportModel> importList) {
		ArrayList<StudentImportModel> dbList = getAllStudentsAsImportData();
		int dbListIdx = 0;
		int dbListSize = dbList.size();

		StudentImportModel dbStudent;
		for (int i = 0; i < importList.size(); i++) {
			StudentImportModel importStudent = importList.get(i);

			// Log any missing data
			if (importStudent.getGithubName().equals("")) {
				MySqlDbLogging.insertLogData(LogDataModel.MISSING_GITHUB_NAME,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), "");
			}

			if (importStudent.getGradYear() == 0)
				MySqlDbLogging.insertLogData(LogDataModel.MISSING_GRAD_YEAR,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), "");

			if (importStudent.getHomeLocation() == LocationModel.CLASS_LOCATION_UNKNOWN) {
				if (importStudent.getHomeLocAsString().equals(""))
					MySqlDbLogging.insertLogData(LogDataModel.MISSING_HOME_LOCATION,
							new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
							importStudent.getClientID(), "");
				else
					MySqlDbLogging.insertLogData(LogDataModel.UNKNOWN_HOME_LOCATION,
							new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
							importStudent.getClientID(), " (" + importStudent.getHomeLocAsString() + ")");
			}

			if (importStudent.getGender() == GenderModel.getGenderUnknown())
				MySqlDbLogging.insertLogData(LogDataModel.MISSING_GENDER,
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
				} else {
					// Import student is new, insert into DB
					insertStudent(importStudent);
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
				PreparedStatement selectStmt = sqlDb.dbConnection
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
							result.getInt("isInMasterDb"), result.getString("Email"), result.getString("AcctMgrEmail"),
							result.getString("EmergencyEmail")));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
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
				PreparedStatement addStudentStmt = sqlDb.dbConnection.prepareStatement(
						"INSERT INTO Students (ClientID, LastName, FirstName, GithubName, NewGithub, NewStudent, "
								+ "Gender, StartDate, Location, GradYear, isInMasterDb, Email, EmergencyEmail, "
								+ "AcctMgrEmail) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?);");

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
				addStudentStmt.setString(col++, student.getEmail());
				addStudentStmt.setString(col++, student.getEmergContactEmail());
				addStudentStmt.setString(col++, student.getAccountMgrEmails());

				addStudentStmt.executeUpdate();
				addStudentStmt.close();

				if (student.getGithubName() == null)
					MySqlDbLogging.insertLogData(LogDataModel.ADD_NEW_STUDENT_NO_GITHUB,
							new StudentNameModel(student.getFirstName(), student.getLastName(), true),
							student.getClientID(), "");
				else
					MySqlDbLogging.insertLogData(LogDataModel.ADD_NEW_STUDENT,
							new StudentNameModel(student.getFirstName(), student.getLastName(), true),
							student.getClientID(), "");
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel(student.getFirstName(), student.getLastName(),
						student.getIsInMasterDb() == 1 ? true : false);
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, studentModel, 0, ": " + e2.getMessage());
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
				PreparedStatement updateStudentStmt = sqlDb.dbConnection.prepareStatement(
						"UPDATE Students SET LastName=?, FirstName=?, GithubName=?, NewGithub=?, NewStudent=?,"
								+ "Gender=?, StartDate=?, Location=?, GradYear=?, isInMasterDb=?, Email=?,"
								+ "EmergencyEmail=?, AcctMgrEmail=? WHERE ClientID=?;");

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
				updateStudentStmt.setString(col++, importStudent.getEmail());
				updateStudentStmt.setString(col++, importStudent.getEmergContactEmail());
				updateStudentStmt.setString(col++, importStudent.getAccountMgrEmails());
				updateStudentStmt.setInt(col, importStudent.getClientID());

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();

				MySqlDbLogging.insertLogData(LogDataModel.UPDATE_STUDENT_INFO,
						new StudentNameModel(importStudent.getFirstName(), importStudent.getLastName(), true),
						importStudent.getClientID(), changedFields);
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel(importStudent.getFirstName(),
						importStudent.getLastName(), true);
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, studentModel, 0, ": " + e2.getMessage());
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

	private void updateIsInMasterDb(StudentImportModel student, int isInMasterDb) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateStudentStmt = sqlDb.dbConnection
						.prepareStatement("UPDATE Students SET isInMasterDb=? WHERE ClientID=?;");

				updateStudentStmt.setInt(1, isInMasterDb);
				updateStudentStmt.setInt(2, student.getClientID());

				updateStudentStmt.executeUpdate();
				updateStudentStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel model = new StudentNameModel(student.getFirstName(), student.getLastName(),
						(isInMasterDb == 1) ? true : false);
				MySqlDbLogging.insertLogData(LogDataModel.STUDENT_DB_ERROR, model, student.getClientID(),
						": " + e2.getMessage());
				break;
			}
		}
	}

	/*
	 * ------- Attendance Import Database Queries -------
	 */
	public void importAttendance(ArrayList<AttendanceEventModel> importList) {
		// Import attendance from Pike13 to the Tracker database
		ArrayList<AttendanceEventModel> dbList = sqlDb.getAllEvents();
		ArrayList<StudentModel> studentList = sqlDb.getActiveStudents();
		int dbListIdx = 0;
		int dbListSize = dbList.size();
		int addedAttCount = 0, updatedAttCount = 0;
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

				// Compare attendance; if matched, also check state & teachers
				compare = dbAttendance.compareTo(importEvent);
				if (compare == 0 && (!dbAttendance.getState().equals(importEvent.getState())
						|| !dbAttendance.getTeacherNames().equals(teachers)))
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

					// Compare attendance; if matched, also check state & teachers
					compare = dbAttendance.compareTo(importEvent);
					if (compare == 0 && (!dbAttendance.getState().equals(importEvent.getState())
							|| !dbAttendance.getTeacherNames().equals(teachers)))
						compare = 2;
				}

				if (compare == 0) {
					dbListIdx++;

				} else {
					int idx = getClientIdxInStudentList(studentList, importEvent.getClientID());
					if (idx >= 0) {
						if (compare == 1)
							addedAttCount += addAttendance(importEvent.getClientID(), importEvent.getVisitID(),
									importEvent.getServiceDateString(), importEvent.getEventName(),
									studentList.get(idx).getNameModel(), teachers, importEvent.getServiceCategory(),
									importEvent.getState());
						else // state field has changed, so update
							updatedAttCount += updateAttendanceState(importEvent.getClientID(),
									studentList.get(idx).getNameModel(), importEvent.getServiceDateString(),
									importEvent.getEventName(), importEvent.getState(), teachers);

					} else
						MySqlDbLogging.insertLogData(LogDataModel.STUDENT_NOT_FOUND,
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
						addedAttCount += addAttendance(importEvent.getClientID(), importEvent.getVisitID(),
								importEvent.getServiceDateString(), importEvent.getEventName(),
								studentList.get(idx).getNameModel(), teachers, importEvent.getServiceCategory(),
								importEvent.getState());
					else // state field has changed, so update
						updatedAttCount += updateAttendanceState(importEvent.getClientID(),
								studentList.get(idx).getNameModel(), importEvent.getServiceDateString(),
								importEvent.getEventName(), importEvent.getState(), teachers);

				} else {
					// Student not found
					MySqlDbLogging.insertLogData(LogDataModel.STUDENT_NOT_FOUND,
							new StudentNameModel(importEvent.getStudentNameModel().getFirstName(), "", false),
							importEvent.getClientID(),
							": " + importEvent.getEventName() + " on " + importEvent.getServiceDateString());
				}
			}
		}

		// Log number of attendance records added/updated
		if (addedAttCount > 0)
			MySqlDbLogging.insertLogData(LogDataModel.UPDATE_STUDENT_ATTENDANCE, new StudentNameModel("", "", false), 0,
					": " + addedAttCount + " records changed");
		if (updatedAttCount > 0)
			MySqlDbLogging.insertLogData(LogDataModel.UPDATE_ATTENDANCE_STATE, new StudentNameModel("", "", false), 0,
					": " + updatedAttCount + " records changed");
	}

	public void createSortedAttendanceList() {
		for (int i = 0; i < 2; i++) {
			try {
				// Empty the sorted list
				PreparedStatement truncateStmt = sqlDb.dbConnection
						.prepareStatement("TRUNCATE TABLE SortedAttendance;");
				truncateStmt.executeUpdate();
				truncateStmt.close();

				// Now re-sort in descending date order
				PreparedStatement insertStmt = sqlDb.dbConnection.prepareStatement("INSERT INTO SortedAttendance "
						+ "SELECT * FROM Attendance ORDER BY ClientID, ServiceDate DESC, EventName;");
				insertStmt.executeUpdate();
				insertStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, new StudentNameModel("", "", false), 0,
						" sorting: " + e2.getMessage());
				break;
			}
		}
	}

	private int addAttendance(int clientID, int visitID, String serviceDate, String eventName,
			StudentNameModel nameModel, String teacherNames, String serviceCategory, String state) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement addAttendanceStmt = sqlDb.dbConnection.prepareStatement(
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

				return 1;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Attendance data already exists, do nothing
				break;

			} catch (SQLException e3) {
				StudentNameModel studentModel = new StudentNameModel(nameModel.getFirstName(), nameModel.getLastName(),
						nameModel.getIsInMasterDb());
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, clientID,
						": " + e3.getMessage());
				break;
			}
		}
		return 0;
	}

	private int updateAttendanceState(int clientID, StudentNameModel nameModel, String serviceDate, String eventName,
			String state, String teachers) {
		PreparedStatement updateAttendanceStmt;
		for (int i = 0; i < 2; i++) {
			try {
				// The only fields that should be updated are the State & Teacher fields
				updateAttendanceStmt = sqlDb.dbConnection.prepareStatement(
						"UPDATE Attendance SET State=?, TeacherNames=? WHERE ClientID=? AND ServiceDate=?;");

				int col = 1;
				updateAttendanceStmt.setString(col++, state);
				updateAttendanceStmt.setString(col++, teachers);
				updateAttendanceStmt.setInt(col++, clientID);
				updateAttendanceStmt.setDate(col++, java.sql.Date.valueOf(serviceDate));

				updateAttendanceStmt.executeUpdate();
				updateAttendanceStmt.close();

				return 1;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e) {
				StudentNameModel studentModel = new StudentNameModel(nameModel.getFirstName(), nameModel.getLastName(),
						nameModel.getIsInMasterDb());
				MySqlDbLogging.insertLogData(LogDataModel.ATTENDANCE_DB_ERROR, studentModel, clientID,
						": " + e.getMessage());
				break;
			}
		}
		return 0;
	}

	private String parseTeacherNames(String origTeachers) {
		if (origTeachers == null || origTeachers.equals(""))
			return "";

		String teachers = "";
		String[] values = origTeachers.split("\\s*,\\s*");
		for (int i = 0; i < values.length; i++) {
			String valueLC = values[i].toLowerCase();
			if (values[i].startsWith("TA-") || valueLC.startsWith("open lab") || valueLC.startsWith("sub teacher")
					|| valueLC.startsWith("padres game") || valueLC.startsWith("make-up")
					|| valueLC.startsWith("intro to java") || valueLC.startsWith("league admin")
					|| valueLC.startsWith("summer prog") || valueLC.startsWith("need assist"))
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

	/*
	 * ------- Schedule Import Database Queries -------
	 */
	public void importSchedule(ArrayList<ScheduleModel> importList) {
		ArrayList<ScheduleModel> dbList = sqlDb.getClassSchedule();
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

	private void removeClassFromSchedule(ScheduleModel model) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement deleteClassStmt = sqlDb.dbConnection
						.prepareStatement("DELETE FROM Schedule WHERE ScheduleID=?;");

				// Delete class from schedule
				deleteClassStmt.setInt(1, model.getScheduleID());
				deleteClassStmt.executeUpdate();
				deleteClassStmt.close();

				MySqlDbLogging.insertLogData(LogDataModel.REMOVE_CLASS_FROM_SCHEDULE,
						new StudentNameModel("", "", false), 0, ": " + model.getClassName() + " on "
								+ dayOfWeek[model.getDayOfWeek()] + " at " + model.getStartTimeFormatted());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.SCHEDULE_DB_ERROR, null, 0, ": " + e2.getMessage());
				break;
			}
		}
	}

	public void addClassToSchedule(ScheduleModel importEvent) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement addScheduleStmt = sqlDb.dbConnection.prepareStatement(
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

				MySqlDbLogging.insertLogData(LogDataModel.ADD_CLASS_TO_SCHEDULE, new StudentNameModel("", "", false), 0,
						": " + importEvent.getClassName() + " on " + dayOfWeek[importEvent.getDayOfWeek()] + " at "
								+ importEvent.getStartTimeFormatted());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Schedule data already exists, do nothing
				break;

			} catch (SQLException e3) {
				MySqlDbLogging.insertLogData(LogDataModel.SCHEDULE_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e3.getMessage());
				break;
			}
		}
	}

	/*
	 * ------- Courses Import Database Queries -------
	 */
	public void importCourses(ArrayList<CoursesModel> importList) {
		ArrayList<CoursesModel> dbList = sqlDb.getCourseSchedule("CourseID");
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

	private void addCourseToSchedule(CoursesModel courseEvent) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement addCourseStmt = sqlDb.dbConnection
						.prepareStatement("INSERT INTO Courses (CourseID, EventName, Enrolled) " + "VALUES (?, ?, ?);");

				int col = 1;
				addCourseStmt.setInt(col++, courseEvent.getScheduleID());
				addCourseStmt.setString(col++, courseEvent.getEventName());
				addCourseStmt.setInt(col, courseEvent.getEnrollment());

				addCourseStmt.executeUpdate();
				addCourseStmt.close();

				MySqlDbLogging.insertLogData(LogDataModel.ADD_COURSES_TO_SCHEDULE, new StudentNameModel("", "", false),
						0, ": " + courseEvent.getEventName());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLIntegrityConstraintViolationException e2) {
				// Schedule data already exists, do nothing
				break;

			} catch (SQLException e3) {
				MySqlDbLogging.insertLogData(LogDataModel.COURSES_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e3.getMessage());
				break;
			}
		}
	}

	private void updateCourse(CoursesModel course) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement updateCourseStmt = sqlDb.dbConnection
						.prepareStatement("UPDATE Courses SET EventName=?, Enrolled=? WHERE CourseID=?;");

				int col = 1;
				updateCourseStmt.setString(col++, course.getEventName());
				updateCourseStmt.setInt(col++, course.getEnrollment());
				updateCourseStmt.setInt(col, course.getScheduleID());

				updateCourseStmt.executeUpdate();
				updateCourseStmt.close();

				MySqlDbLogging.insertLogData(LogDataModel.UPDATE_COURSES_INFO, new StudentNameModel("", "", true), 0,
						": " + course.getEventName());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				StudentNameModel studentModel = new StudentNameModel("", "", true);
				MySqlDbLogging.insertLogData(LogDataModel.COURSES_DB_ERROR, studentModel, 0,
						" for " + course.getEventName() + ": " + e2.getMessage());
				break;
			}
		}
	}

	private void removeCourseFromSchedule(CoursesModel course) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement deleteClassStmt = sqlDb.dbConnection
						.prepareStatement("DELETE FROM Courses WHERE CourseID=?;");

				// Delete class from schedule
				deleteClassStmt.setInt(1, course.getScheduleID());
				deleteClassStmt.executeUpdate();
				deleteClassStmt.close();

				MySqlDbLogging.insertLogData(LogDataModel.REMOVE_COURSES_FROM_SCHEDULE,
						new StudentNameModel("", "", false), 0, ": " + course.getEventName());
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				MySqlDbLogging.insertLogData(LogDataModel.COURSES_DB_ERROR, null, 0, ": " + e2.getMessage());
				break;
			}
		}
	}
}

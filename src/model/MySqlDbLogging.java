package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

/** MySqlDbLogging
 * 
 * @author wavis
 *
 * This file handles the read/write of the AWS Student Trackr database logging table.
 * API's exist for the following:
 *    - insert new log messages into the table
 *    - retrieve table data
 *    - clear all table data
 * 
 */
public class MySqlDbLogging {
	private static final int LOG_APPEND_WIDTH = 120;
	private static MySqlDatabase sqlDb;

	public MySqlDbLogging(MySqlDatabase db) {
		sqlDb = db;
	}

	public static void insertLogData(int logType, StudentNameModel studentNameModel, int clientID, String appendedMsg) {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement addLogDataStmt = sqlDb.dbConnection.prepareStatement(
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
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				if (!e2.getMessage().startsWith("Duplicate entry")) {
					// TODO: Can't log this error! What to do instead?
				}
				break;
			}
		}
	}

	public static ArrayList<LogDataModel> getLogData() {
		ArrayList<LogDataModel> logData = new ArrayList<LogDataModel>();

		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement("SELECT * FROM LogData;");
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
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.STUDENT_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
		return logData;
	}

	public static void clearLogData() {
		for (int i = 0; i < 2; i++) {
			try {
				// If Database no longer connected, the exception code will re-connect
				PreparedStatement selectStmt = sqlDb.dbConnection.prepareStatement("TRUNCATE LogData");
				selectStmt.executeUpdate();
				selectStmt.close();
				break;

			} catch (CommunicationsException | MySQLNonTransientConnectionException | NullPointerException e1) {
				if (i == 0) {
					// First attempt to re-connect
					sqlDb.connectDatabase();
				}

			} catch (SQLException e2) {
				insertLogData(LogDataModel.LOG_DATA_DB_ERROR, new StudentNameModel("", "", false), 0,
						": " + e2.getMessage());
				break;
			}
		}
	}
}

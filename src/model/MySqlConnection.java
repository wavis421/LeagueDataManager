package model;

import java.awt.Cursor;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import gui.MainFrame;

public class MySqlConnection {
	// Constants
	private static final int LOCAL_PORT = 8740; // any free port can be used
	private static final String SSH_HOST = "www.ProgramPlanner.org";
	private static final String SSH_USER = "wavis421";
	// TODO: This should be on server??
	private static final String SSH_KEY_FILE_PATH = "C:\\Users\\wavis\\Documents\\AppDevelopment\\keystore\\wavisadmin-keypair-ncal.pem";
	private static final String REMOTE_HOST = "127.0.0.1";
	private static final int REMOTE_PORT = 3306;

	// Save SSH Session and database connection
	private static Session session = null;
	private static Connection connection = null;

	public static Connection connectToServer(JFrame parent, String dataBaseName, String user, String password)
			throws SQLException {
		// Save current cursor and set to "wait" cursor
		Cursor cursor = parent.getCursor();
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// If re-connecting, close current database connection
		closeDataBaseConnection();
		if (session != null && !session.isConnected()) {
			session = null;
		}

		// Create new SSH and database connections
		if (session == null)
			connectSSH();
		connectToDataBase(dataBaseName, user, password);

		// Set cursor back to original setting
		parent.setCursor(cursor);
		return connection;
	}

	private static void connectSSH() {
		try {
			java.util.Properties config = new java.util.Properties();
			JSch jsch = new JSch();
			session = jsch.getSession(SSH_USER, SSH_HOST, 22);
			jsch.addIdentity(SSH_KEY_FILE_PATH);
			config.put("StrictHostKeyChecking", "no");
			config.put("ConnectionAttempts", "2");
			session.setConfig(config);

			session.setServerAliveInterval(60 * 1000); // in milliseconds
			session.setServerAliveCountMax(20);
			session.setConfig("TCPKeepAlive", "yes");

			session.connect();
			session.setPortForwardingL(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);

		} catch (Exception e) {
			// Ask if user wants to exit
			if (JOptionPane.showOptionDialog(null,
					"Failed to establish a secure SSH tunnel.\n(" + e.getMessage() + ")\n"
							+ "Please make sure Program Planner is not already running.\n\nDo you want to continue?\n",
					"Failed to create secure connection", 0, JOptionPane.PLAIN_MESSAGE, null,
					new String[] { "Yes", "Exit Program" }, 0) != JOptionPane.YES_OPTION) {

				MainFrame.shutdown();
			}
		}
	}

	private static void connectToDataBase(String dataBaseName, String user, String password) throws SQLException {
		try {
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName).newInstance();
			MysqlDataSource dataSource = new MysqlDataSource();

			if (session == null) {
				// Connecting directly to database using port 3306
				dataSource.setServerName("www.ProgramPlanner.org");
				dataSource.setPortNumber(REMOTE_PORT);
			} else {
				// Connecting through SSH tunnel
				dataSource.setServerName("localhost");
				dataSource.setPortNumber(LOCAL_PORT);
			}

			dataSource.setDatabaseName(dataBaseName);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			dataSource.setAutoReconnect(true);

			connection = dataSource.getConnection();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	public static void closeConnections() {
		closeDataBaseConnection();
		closeSSHConnection();
	}

	private static void closeDataBaseConnection() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Error closing database connection: " + e.getMessage());
		}
		connection = null;
	}

	private static void closeSSHConnection() {
		if (session != null) {
			session.disconnect();
			session = null;
		}
	}
}

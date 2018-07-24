package model;

import java.awt.Cursor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JFrame;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class MySqlConnection {
	// Constants
	private static final int REMOTE_PORT = 3306;
	private static final String REMOTE_HOST = "league-trackr-db.curyeogxssy9.us-west-1.rds.amazonaws.com";
	private static final String SERVER = "localhost";
	private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";

	// SSH/Database connect constants
	private static final String SSH_HOST = "ec2-52-53-60-28.us-west-1.compute.amazonaws.com";
	private static final String SSH_USER = "ec2-user";
	private static final String SSH_KEY_FILE_PATH = "./student_tracker_key.pem";
	private static final String DATABASE = "StudentTracker";
	private static final String DB_USER = "awsleague";

	// Save SSH Session and database connection
	private int localSshPort;
	private Session session = null;
	private Connection connection = null;

	public MySqlConnection(int localPort) {
		localSshPort = localPort;
	}

	public static String getKeyFilePath() {
		return SSH_KEY_FILE_PATH;
	}

	public Connection connectToServer(JFrame parent, String password) throws SQLException {
		// Save current cursor and set to "wait" cursor
		Cursor cursor = null;
		if (parent != null) {
			cursor = parent.getCursor();
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		// If re-connecting, close current database connection
		closeDataBaseConnection();
		if (session != null && !session.isConnected()) {
			session = null;
		}

		// Create new SSH and database connections
		if (localSshPort == MySqlDatabase.STUDENT_IMPORT_NO_SSH)
			connectToLambdaDatabase(DB_USER, password);
		else {
			if (session == null)
				connectSSH();
			connectToRemoteDataBase(DB_USER, password);
		}

		// Set cursor back to original setting
		if (parent != null)
			parent.setCursor(cursor);
		return connection;
	}

	private void connectSSH() {
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
			session.setPortForwardingL(localSshPort, REMOTE_HOST, REMOTE_PORT);

		} catch (Exception e) {
			// Failed maximum connection attempts: disconnect session
			closeSSHConnection();
			return;
		}
	}

	private void connectToRemoteDataBase(String user, String password) throws SQLException {
		if (session == null)
			return;

		try {
			Class.forName(DRIVER_NAME).newInstance();
			MysqlDataSource dataSource = new MysqlDataSource();

			// Connecting through SSH tunnel
			dataSource.setServerName(SERVER);
			dataSource.setPortNumber(localSshPort);
			dataSource.setDatabaseName(DATABASE);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			dataSource.setAutoReconnect(true);

			connection = dataSource.getConnection();

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO: How to handle this exception?
		}
	}

	private void connectToLambdaDatabase(String user, String password) {
		try {
			Class.forName(DRIVER_NAME);
			connection = DriverManager.getConnection("jdbc:mysql://" + REMOTE_HOST + ":3306/" + DATABASE, user, password);

		} catch (SQLException | ClassNotFoundException e) {
			// TODO: How to handle this exception?
			System.out.println("Lambda function failed to connect to DB: " + e.getMessage());
		}
	}

	public void closeConnections() {
		closeDataBaseConnection();
		closeSSHConnection();
	}

	private void closeDataBaseConnection() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			// Exiting program, not much to be done
		}
		connection = null;
	}

	private void closeSSHConnection() {
		if (session != null) {
			session.disconnect();
			session = null;
		}
	}
}

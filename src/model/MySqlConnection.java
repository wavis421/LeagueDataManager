package model;

import java.awt.Cursor;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import gui.MainFrame;

public class MySqlConnection {
	// Constants
	private static final int LOCAL_PORT = 8740; // any free port can be used
	private static final int REMOTE_PORT = 3306;
	private static final String SERVER = "localhost";

	// SSH/Database connect constants
	// Once DB testing is complete, these will be final constants
	private static String SSH_HOST;
	private static String SSH_USER;
	private static String SSH_KEY_FILE_PATH;
	private static String DATABASE;
	private static String DB_USER;
	private static String REMOTE_HOST;

	// Save SSH Session and database connection
	private static Session session = null;
	private static Connection connection = null;
	private static ImageIcon myIcon;

	public static void setIcon(ImageIcon icon) {
		myIcon = icon;
	}

	public static Connection connectToServer(JFrame parent, int serverSelect, String password) throws SQLException {
		// Save current cursor and set to "wait" cursor
		Cursor cursor = null;
		if (parent != null) {
			cursor = parent.getCursor();
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		// During development/test, need access to multiple databases
		if (serverSelect == 0) { // Wendy's web host server
			SSH_HOST = "www.ProgramPlanner.org";
			SSH_USER = "wavis421";
			SSH_KEY_FILE_PATH = "./wavisadmin-keypair-ncal.pem";
			DATABASE = "LeagueData";
			DB_USER = "tester421";
			REMOTE_HOST = "127.0.0.1";

		} else { // AWS server
			SSH_HOST = "ec2-34-215-59-190.us-west-2.compute.amazonaws.com";
			SSH_USER = "ec2-user";
			SSH_KEY_FILE_PATH = "./league.pem";
			DATABASE = "leagueData";
			DB_USER = "LeagueTeachers";
			REMOTE_HOST = "data-manager-db-test.cryfqyj7jcsy.us-west-2.rds.amazonaws.com";
		}

		// If re-connecting, close current database connection
		closeDataBaseConnection();
		if (session != null && !session.isConnected()) {
			session = null;
		}

		// Create new SSH and database connections
		if (session == null)
			connectSSH();
		connectToDataBase(DB_USER, password);

		// Set cursor back to original setting
		if (parent != null)
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
			// Failed maximum connection attempts, exit program
			JOptionPane.showMessageDialog(null,
					e.getMessage() + "\nVerify that the password you entered is correct and"
							+ "\nmake sure League Data Manager is not already running.\n",
					"Failed to establish secure SSH tunnel", JOptionPane.ERROR_MESSAGE, myIcon);
			MainFrame.shutdown();
		}
	}

	private static void connectToDataBase(String user, String password) throws SQLException {
		if (session == null)
			return;

		try {
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName).newInstance();
			MysqlDataSource dataSource = new MysqlDataSource();

			// Connecting through SSH tunnel
			dataSource.setServerName(SERVER);
			dataSource.setPortNumber(LOCAL_PORT);
			dataSource.setDatabaseName(DATABASE);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			dataSource.setAutoReconnect(true);

			connection = dataSource.getConnection();

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Database Connection Failed", JOptionPane.ERROR_MESSAGE,
					myIcon);
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
			// Exiting program, not much to be done
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

package gui;

/**
 * File: MainFrame.java
 * -----------------------
 * This class creates the GUI for the League App.
 **/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import controller.Controller;
import model.AttendanceModel;
import model.CoursesModel;
import model.DateRangeEvent;
import model.GithubModel;
import model.InvoiceModel;
import model.LogDataModel;

public class MainFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 1300;
	private static final int PREF_FRAME_HEIGHT = 700;

	private static final int PREF_TABLE_PANEL_WIDTH = PREF_FRAME_WIDTH;
	private static final int PREF_TABLE_PANEL_HEIGHT = PREF_FRAME_HEIGHT - 58;

	private static final String STUDENT_TITLE = "League Student Info";
	private static final String STUDENTS_NOT_IN_MASTER_TITLE = "Inactive League Students";
	private static final String ATTENDANCE_TITLE = "League Attendance";
	private static final String SCHEDULE_TITLE = "Weekly Class Schedule";
	private static final String COURSE_TITLE = "Workshops and Summer Slam Schedule";
	private static final String INVOICE_TITLE = "Course Invoices for ";
	private static final String GITHUB_TITLE = "Students with no Github comments since ";
	private static final String LOGGING_TITLE = "Logging Data";

	private static final int STUDENT_TABLE_ALL = 0;
	private static final int STUDENT_TABLE_NOT_IN_MASTER_DB = 1;
	private static final int STUDENT_TABLE_BY_STUDENT = 2;

	// Report missing github if 3 or more classes with no github in the last 35 days
	private static final int NO_RECENT_GITHUB_SINCE_DAYS = 35;
	private static final int MIN_CLASSES_WITH_NO_GITHUB = 3;

	/* Private instance variables */
	private Preferences prefs = Preferences.userRoot();
	private static Controller controller;
	private JPanel mainPanel;
	private JPanel tablePanel = new JPanel();
	private JLabel headerLabel = new JLabel();
	private StudentTable studentTable;
	private AttendanceTable attendanceTable;
	private LogTable logTable;
	private ScheduleTable scheduleTable;
	private InvoiceTable invoiceTable;
	private GithubTable githubTable;
	private CoursesTable coursesTable;
	private JTable activeTable;
	private String activeTableHeader;
	private String githubToken, pike13Token;
	ImageIcon icon;
	private static JFrame frame = new JFrame();

	// Class menu names
	private static String[] classMenuNames = { "Level 0 ", "Level 1 ", "Level 2 ", "Level 3 ", "Level 4 ", "Level 5 ",
			"Level 6 ", "Level 7 ", "Level 8 ", "Level 9 ", "Labs " };

	public MainFrame() {
		frame.setTitle("League Student Tracker");
		frame.setLayout(new BorderLayout());
		frame.setBackground(Color.WHITE);

		icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		frame.setIconImage(icon.getImage());

		// Get database password
		PasswordDialog pwDialog = new PasswordDialog();
		String awsPassword = pwDialog.getDialogResponse();

		// Retrieve tokens from registry (Windows only)
		githubToken = prefs.get("GithubToken", "");
		pike13Token = prefs.get("Pike13Token", "");
		if (pike13Token.equals(""))
			pike13Token = getPike13TokenFromFile();

		// Create components
		mainPanel = new JPanel(new BorderLayout());
		frame.add(mainPanel);
		controller = new Controller(frame, awsPassword, githubToken, pike13Token);

		// Connect to database
		if (!controller.connectDatabase()) {
			JOptionPane.showMessageDialog(null,
					"Verify that the password you entered is correct,\n"
							+ "that the League Data Manager is not already running\n"
							+ "and your key file is in the executable directory.\n",
					"Failure connecting to database", JOptionPane.ERROR_MESSAGE, icon);
			shutdown();
		}

		// Configure header
		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		headerLabel.setFont(CustomFonts.TITLE_FONT);
		headerLabel.setForeground(CustomFonts.TITLE_COLOR);
		mainPanel.add(headerLabel, BorderLayout.NORTH);

		// Default tables to display all data
		headerLabel.setText(STUDENT_TITLE);
		activeTableHeader = STUDENT_TITLE;

		// Configure panel and each table
		tablePanel.setPreferredSize(new Dimension(PREF_TABLE_PANEL_WIDTH, PREF_TABLE_PANEL_HEIGHT));
		attendanceTable = new AttendanceTable(tablePanel, new ArrayList<AttendanceModel>());
		logTable = new LogTable(tablePanel, new ArrayList<LogDataModel>());
		scheduleTable = new ScheduleTable(tablePanel);
		invoiceTable = new InvoiceTable(tablePanel, new ArrayList<InvoiceModel>());
		githubTable = new GithubTable(tablePanel, new ArrayList<GithubModel>());
		coursesTable = new CoursesTable(tablePanel, new ArrayList<CoursesModel>());
		studentTable = new StudentTable(tablePanel, controller.getActiveStudents());
		activeTable = studentTable.getTable();

		createTableListeners();

		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(5, 1, 1, 1);
		tablePanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		mainPanel.add(tablePanel, BorderLayout.CENTER);

		frame.setJMenuBar(createMenuBar());

		// Make form visible
		frame.pack();
		frame.setSize(PREF_FRAME_WIDTH, PREF_FRAME_HEIGHT);
		frame.setLocation(100, 100);
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				shutdown();
			}
		});
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		// Add file menu to menu bar
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		createFileMenu(fileMenu);

		// Add student menu to menu bar
		JMenu studentMenu = new JMenu("Students");
		menuBar.add(studentMenu);
		createStudentMenu(studentMenu);

		// Add attendance menu to menu bar
		JMenu attendanceMenu = new JMenu("Attendance");
		menuBar.add(attendanceMenu);
		createAttendanceMenu(attendanceMenu);

		// Add schedule menu to menu bar
		JMenu scheduleMenu = new JMenu("Schedule");
		menuBar.add(scheduleMenu);
		createScheduleMenu(scheduleMenu);

		// Add Reports menu to menu bar
		if (!pike13Token.equals("")) {
			JMenu reportsMenu = new JMenu("Reports");
			menuBar.add(reportsMenu);
			createReportsMenu(reportsMenu);
		}

		// Add help menu to menu bar
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		createHelpMenu(helpMenu);

		return menuBar;
	}

	private void createFileMenu(JMenu fileMenu) {
		// Create file sub-menus
		JMenuItem viewLogDataItem = new JMenuItem("View Log Data ");
		JMenuItem clearLogDataItem = new JMenuItem("Clear Log Data ");
		JMenuItem printTableItem = new JMenuItem("Print Table");
		JMenuItem exportTableItem = new JMenuItem("Export Table to CSV File");
		JMenuItem exitItem = new JMenuItem("Exit ");

		// Add these sub-menus to File menu
		fileMenu.add(viewLogDataItem);
		fileMenu.add(clearLogDataItem);
		fileMenu.add(printTableItem);
		fileMenu.add(exportTableItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Set up listeners for File menu
		viewLogDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshLogTable();
			}
		});
		clearLogDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.clearDbLogData();
				refreshLogTable();
			}
		});
		printTableItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Set cursor to "wait" cursor
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				String header = activeTableHeader;
				if (header.startsWith(SCHEDULE_TITLE)) {
					activeTable = scheduleTable.getTable();
					header += " for " + activeTable.getName();
				}

				try {
					MessageFormat headerFormat = new MessageFormat(header);
					MessageFormat footerFormat = new MessageFormat("- {0} -");
					activeTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);

				} catch (PrinterException e1) {
					frame.setCursor(Cursor.getDefaultCursor());
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Printer Failure", JOptionPane.ERROR_MESSAGE,
							icon);
				}

				// Set cursor back to default
				frame.setCursor(Cursor.getDefaultCursor());
			}
		});
		exportTableItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activeTableHeader.startsWith(SCHEDULE_TITLE)) {
					activeTable = scheduleTable.getTable();
				}
				CsvFileWriter.writeTableToCsvFile(frame, activeTable.getModel());
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shutdown();
			}
		});
	}

	private void createStudentMenu(JMenu studentMenu) {
		// Create sub-menus for the Students menu
		JMenuItem studentNotInMasterMenu = new JMenuItem("View inactive students ");
		JMenuItem studentRemoveInactiveMenu = new JMenuItem("Remove inactive students ");
		JMenuItem studentNoRecentGitItem = new JMenuItem("View students without recent Github ");
		JMenuItem studentViewAllMenu = new JMenuItem("View all students ");

		// Add these sub-menus to the Student menu
		studentMenu.add(studentNotInMasterMenu);
		studentMenu.add(studentRemoveInactiveMenu);
		studentMenu.add(studentNoRecentGitItem);
		studentMenu.add(studentViewAllMenu);

		// Set up listeners for the Student menu
		studentNotInMasterMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Show all students not in master database
				refreshStudentTable(STUDENT_TABLE_NOT_IN_MASTER_DB, 0);
			}
		});
		studentRemoveInactiveMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.removeInactiveStudents();
				refreshStudentTable(STUDENT_TABLE_NOT_IN_MASTER_DB, 0);
			}
		});
		studentNoRecentGitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeDataFromTables();
				String sinceDate = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"))
						.minusDays(NO_RECENT_GITHUB_SINCE_DAYS).toString("yyyy-MM-dd");
				headerLabel.setText(GITHUB_TITLE + sinceDate);
				githubTable.setData(tablePanel,
						controller.getStudentsWithNoRecentGithub(sinceDate, MIN_CLASSES_WITH_NO_GITHUB));

				activeTable = githubTable.getTable();
				activeTableHeader = headerLabel.getText();
			}
		});
		studentViewAllMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshStudentTable(STUDENT_TABLE_ALL, 0);
			}
		});
	}

	private void createAttendanceMenu(JMenu attendanceMenu) {
		// Create sub-menus for the Attendance menu
		JMenu attendanceViewByClassMenu = new JMenu("View Attendance by Class ");
		JMenuItem attendanceViewAllItem = new JMenuItem("View All Attendance ");
		attendanceMenu.add(attendanceViewByClassMenu);
		attendanceMenu.add(attendanceViewAllItem);

		// Set up listeners for Attendance menu
		for (int i = 0; i < classMenuNames.length; i++) {
			int classFilter = i;
			JMenu subMenu = new JMenu(classMenuNames[i]);
			attendanceViewByClassMenu.add(subMenu);

			subMenu.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					viewClassMenuListener(subMenu, classFilter);
				}
			});
		}

		attendanceViewAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshAttendanceTable(controller.getAllAttendance(), "", false);
			}
		});
	}

	private void createScheduleMenu(JMenu scheduleMenu) {
		// Create sub-menu for the Schedule menu
		JMenuItem scheduleViewMenu = new JMenuItem("View Weekly Class Schedule ");
		JMenuItem courseViewMenu = new JMenuItem("View Workshop and Summer Slam Schedule ");
		scheduleMenu.add(scheduleViewMenu);
		scheduleMenu.add(courseViewMenu);

		// Set up listeners for Schedule menu
		scheduleViewMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshScheduleTable();
			}
		});
		courseViewMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshCoursesTable();
			}
		});
	}

	private void createReportsMenu(JMenu reportsMenu) {
		// Create sub-menu for the Reports menu
		JMenuItem invoiceMenu = new JMenuItem("Course Invoices ");
		reportsMenu.add(invoiceMenu);

		// Set up listeners for Reports menu
		invoiceMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SelectDateRangeDialog dateSelector = new SelectDateRangeDialog(mainPanel);
				DateRangeEvent dateRange = dateSelector.getDialogResponse();
				if (dateRange != null) {
					refreshInvoiceTable(dateRange);
				}
			}
		});
	}

	private void createHelpMenu(JMenu helpMenu) {
		// Create sub-menus for the Help menu
		JMenuItem menuDescriptionItem = new JMenuItem("Menu Description ");
		JMenuItem exampleUsageItem = new JMenuItem("Example usage ");
		JMenuItem feedbackItem = new JMenuItem("Provide Feedback ");
		JMenuItem aboutItem = new JMenuItem("About League Data Manager ");

		// Add these sub-menus to the Help menu
		helpMenu.add(menuDescriptionItem);
		helpMenu.add(exampleUsageItem);
		helpMenu.add(feedbackItem);
		helpMenu.addSeparator();
		helpMenu.add(aboutItem);

		// Set up listeners for each of the sub-menus
		menuDescriptionItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NotesWindow(NotesWindow.MENU_DESCRIPTION);
			}
		});
		exampleUsageItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NotesWindow(NotesWindow.EXAMPLES);
			}
		});
		feedbackItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NotesWindow(NotesWindow.FEEDBACK);
			}
		});
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NotesWindow(NotesWindow.ABOUT);
			}
		});
	}

	private void viewClassMenuListener(JMenu menu, int filter) {
		// filter: 0 - 9 for levels 0-9, 10 for labs
		menu.removeAll();

		ArrayList<String> classList;
		classList = controller.getClassNamesByLevel(filter);

		for (int i = 0; i < classList.size(); i++) {
			JMenuItem classItem = new JMenuItem(classList.get(i).toString());
			menu.add(classItem);

			classItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					classList.clear();
					menu.removeAll();

					// Add attendance table and header
					refreshAttendanceTable(controller.getAttendanceByClassName(classItem.getText()),
							" for '" + classItem.getText() + "'", false);
				}
			});
		}
		if (classList.size() < 20)
			menu.getPopupMenu().setLayout(new GridLayout(classList.size(), 1));
		else {
			menu.getPopupMenu().setLayout(new GridLayout(20, 1));
		}
	}

	private void createTableListeners() {
		TableListeners listener = new TableListeners() {
			@Override
			public void viewStudentTableByStudent(int clientID) {
				refreshStudentTable(STUDENT_TABLE_BY_STUDENT, clientID);
			}

			@Override
			public void viewAttendanceByStudent(String clientID, String studentName) {
				refreshAttendanceTable(controller.getAttendanceByClientID(clientID), " for " + studentName, true);
			}

			@Override
			public void viewAttendanceByClass(String className, String classDate) {
				// Display class by class name
				if (className.contains("Open Lab")) {
					refreshAttendanceTable(controller.getAttendanceByClassByDate(className, classDate),
							" for '" + className + "' on " + classDate, false);
				} else
					refreshAttendanceTable(controller.getAttendanceByClassName(className), " for '" + className + "'",
							false);
			}

			@Override
			public void viewAttendanceByCourse(String courseName) {
				// Get attendance by course name
				refreshAttendanceTable(controller.getAttendanceByCourseName(courseName), " for '" + courseName + "'",
						false);
			}

			@Override
			public void updateGithubUser(String clientID, String name) {
				// Get Github user name
				new GithubUserDialog(clientID, name);
			}
		};

		// Now provide this listener to each table
		scheduleTable.setTableListener(listener);
		attendanceTable.setTableListener(listener);
		studentTable.setTableListener(listener);
		logTable.setTableListener(listener);
		githubTable.setTableListener(listener);
		coursesTable.setTableListener(listener);
	}

	private void refreshStudentTable(int tableType, int clientID) {
		// Remove data being displayed
		removeDataFromTables();

		// Add student table and header
		if (tableType == STUDENT_TABLE_ALL) {
			headerLabel.setText(STUDENT_TITLE);
			studentTable.setData(tablePanel, controller.getActiveStudents());
		} else if (tableType == STUDENT_TABLE_NOT_IN_MASTER_DB) {
			headerLabel.setText(STUDENTS_NOT_IN_MASTER_TITLE);
			studentTable.setData(tablePanel, controller.getStudentsNotInMasterDB());
		} else { // STUDENT_TABLE_BY_STUDENT
			headerLabel.setText(STUDENT_TITLE);
			studentTable.setData(tablePanel, controller.getStudentByClientID(clientID));
		}

		// Update current table type
		activeTable = studentTable.getTable();
		activeTableHeader = headerLabel.getText();
	}

	private void refreshAttendanceTable(ArrayList<AttendanceModel> list, String titleExtension,
			boolean attendanceByStudent) {
		// Remove data being displayed
		removeDataFromTables();

		// Add attendance table and header
		attendanceTable.setData(tablePanel, list, attendanceByStudent);
		headerLabel.setText(ATTENDANCE_TITLE + titleExtension);

		activeTable = attendanceTable.getTable();
		activeTableHeader = headerLabel.getText();
	}

	private void refreshLogTable() {
		// Remove data being displayed
		removeDataFromTables();

		// Add log data table and header
		logTable.setData(tablePanel, controller.getDbLogData());
		headerLabel.setText(LOGGING_TITLE);

		activeTable = logTable.getTable();
		activeTableHeader = headerLabel.getText();
	}

	private void refreshScheduleTable() {
		// Remove data being displayed
		removeDataFromTables();

		// Add log data table and header
		scheduleTable.setData(tablePanel, controller.getClassSchedule());
		headerLabel.setText(SCHEDULE_TITLE);

		activeTable = scheduleTable.getTable();
		activeTableHeader = headerLabel.getText();
	}

	private void refreshCoursesTable() {
		// Remove data being displayed
		removeDataFromTables();

		// Add log data table and header
		coursesTable.setData(tablePanel, controller.getCourseSchedule());
		headerLabel.setText(COURSE_TITLE);

		activeTable = coursesTable.getTable();
		activeTableHeader = headerLabel.getText();
	}

	private void refreshInvoiceTable(DateRangeEvent dateRange) {
		// Remove data being displayed
		removeDataFromTables();

		// Add invoice data table and header
		invoiceTable.setData(tablePanel, controller.getInvoices(dateRange));
		headerLabel.setText(INVOICE_TITLE + dateRange.toString());

		activeTable = invoiceTable.getTable();
		activeTableHeader = headerLabel.getText();
	}

	private void removeDataFromTables() {
		// Remove data from Student table and Attendance table
		studentTable.removeData();
		attendanceTable.removeData();
		logTable.removeData();
		scheduleTable.removeData();
		invoiceTable.removeData();
		githubTable.removeData();
		coursesTable.removeData();
	}

	private String getPike13TokenFromFile() {
		try {
			File file = new File("./Pike13Token.txt");
			FileInputStream fis = new FileInputStream(file);

			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();

			return new String(data, "UTF-8");

		} catch (IOException e) {
			// Do nothing if file is not there
		}
		return "";
	}

	public static void shutdown() {
		// Disconnect database and dispose of frame before exiting
		if (controller != null)
			controller.disconnectDatabase();
		frame.dispose();
		System.exit(0);
	}
}

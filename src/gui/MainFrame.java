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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
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

import controller.Controller;
import model.ActivityModel;
import model.LogDataModel;

public class MainFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 1300;
	private static final int PREF_FRAME_HEIGHT = 700;

	private static final int PREF_TABLE_PANEL_WIDTH = PREF_FRAME_WIDTH;
	private static final int PREF_TABLE_PANEL_HEIGHT = PREF_FRAME_HEIGHT - 58;

	private static final String STUDENT_TITLE = "League Student Info";
	private static final String STUDENTS_NOT_IN_MASTER_TITLE = "Inactive League Students";
	private static final String ACTIVITY_TITLE = "League Attendance";
	private static final String SCHEDULE_TITLE = "Class Schedule";
	private static final String LOGGING_TITLE = "Logging Data";

	private static final int STUDENT_TABLE_ALL = 0;
	private static final int STUDENT_TABLE_NOT_IN_MASTER_DB = 1;
	private static final int STUDENT_TABLE_BY_STUDENT = 2;

	/* Private instance variables */
	private Preferences prefs = Preferences.userRoot();
	private static Controller controller;
	private JPanel mainPanel;
	private JPanel tablePanel = new JPanel();
	private JLabel headerLabel = new JLabel();
	private StudentTable studentTable;
	private ActivityTable activityTable;
	private LogTable logTable;
	private ScheduleTable scheduleTable;
	private int currentStudentTable;
	private JTable activeTable;
	private String activeTableHeader;
	private JFileChooser fileChooser;
	private FileFilterCsv fileFilter;
	private String githubToken, pike13Token;
	ImageIcon icon;
	private static JFrame frame = new JFrame();

	// Class menu names
	private static String[] classMenuNames = { "Level 0 ", "Level 1 ", "Level 2 ", "Level 3 ", "Level 4 ", "Level 5 ",
			"Level 6 ", "Level 7 ", "Level 8 ", "Labs " };

	public MainFrame() {
		frame.setTitle("League Data Manager");
		frame.setLayout(new BorderLayout());
		frame.setBackground(Color.WHITE);

		icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		frame.setIconImage(icon.getImage());

		// Get database password
		PasswordDialog pwDialog = new PasswordDialog();
		String awsPassword = pwDialog.getDialogResponse();

		// Retrieve tokens
		githubToken = prefs.get("GithubToken", "");
		pike13Token = prefs.get("Pike13Token", "");

		// Create components
		mainPanel = new JPanel(new BorderLayout());
		frame.add(mainPanel);
		controller = new Controller(frame, awsPassword, githubToken, pike13Token);

		// Connect to database
		if (!controller.connectDatabase()) {
			JOptionPane.showMessageDialog(null,
					"Verify that the password you entered is correct and\n"
							+ "that the League Data Manager is not already running.\n",
					"Failure connecting to database", JOptionPane.ERROR_MESSAGE, icon);
			shutdown();
		}

		// Configure header
		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		headerLabel.setFont(CustomFonts.TITLE_FONT);
		headerLabel.setForeground(CustomFonts.TITLE_COLOR);
		mainPanel.add(headerLabel, BorderLayout.NORTH);

		// Default tables to display all data
		currentStudentTable = STUDENT_TABLE_ALL;
		headerLabel.setText(STUDENT_TITLE);
		activeTableHeader = STUDENT_TITLE;

		// Configure panel and each table
		tablePanel.setPreferredSize(new Dimension(PREF_TABLE_PANEL_WIDTH, PREF_TABLE_PANEL_HEIGHT));
		activityTable = new ActivityTable(tablePanel, new ArrayList<ActivityModel>());
		logTable = new LogTable(tablePanel, new ArrayList<LogDataModel>());
		scheduleTable = new ScheduleTable(tablePanel);
		studentTable = new StudentTable(tablePanel, controller.getAllStudents());
		activeTable = studentTable.getTable();

		createTableListeners();

		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(5, 1, 1, 1);
		tablePanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		mainPanel.add(tablePanel, BorderLayout.CENTER);

		fileChooser = new JFileChooser();
		fileFilter = new FileFilterCsv();

		frame.setJMenuBar(createMenuBar());
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setFileFilter(fileFilter);

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

		// Add import menu to menu bar
		if (!pike13Token.equals("") && !githubToken.equals("")) {
			// Only show imports if user is authorized
			JMenu importMenu = new JMenu("Import");
			menuBar.add(importMenu);
			createImportMenu(importMenu);
		}

		// Add student menu to menu bar
		JMenu studentMenu = new JMenu("Students");
		menuBar.add(studentMenu);
		createStudentMenu(studentMenu);

		// Add attendance menu to menu bar
		JMenu activitiesMenu = new JMenu("Attendance");
		menuBar.add(activitiesMenu);
		createActivityMenu(activitiesMenu);

		// Add schedule menu to menu bar
		JMenu scheduleMenu = new JMenu("Schedule");
		menuBar.add(scheduleMenu);
		createScheduleMenu(scheduleMenu);

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
		JMenuItem exitItem = new JMenuItem("Exit ");

		// Add these sub-menus to File menu
		fileMenu.add(viewLogDataItem);
		fileMenu.add(clearLogDataItem);
		fileMenu.add(printTableItem);
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
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shutdown();
			}
		});
	}

	private void createImportMenu(JMenu importMenu) {
		// Add Import sub-menus
		JMenuItem importStudentFileItem = new JMenuItem("Import Students from CSV File...  ");
		JMenuItem importStudentPike13Item = new JMenuItem("Import Students from Pike13...  ");
		JMenuItem importActivityLogFileItem = new JMenuItem("Import Attendance Log from CSV File...  ");
		JMenuItem importActivityLogPike13Item = new JMenuItem("Import Attendance Log from Pike13...  ");
		JMenuItem importGithubItem = new JMenuItem("Import Github comments...  ");
		JMenuItem importScheduleItem = new JMenuItem("Import Class Schedule...  ");
		JMenuItem importAllDatabasesItem = new JMenuItem("Import All Databases...  ");

		// Add these sub-menus to the Import menu
		importMenu.add(importStudentFileItem);
		importMenu.add(importStudentPike13Item);
		importMenu.add(importActivityLogFileItem);
		importMenu.add(importActivityLogPike13Item);
		importMenu.add(importGithubItem);
		importMenu.add(importScheduleItem);
		importMenu.add(importAllDatabasesItem);

		// Set up listeners for the Import menu
		importStudentFileItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					controller.importStudentsFromFile(fileChooser.getSelectedFile());
					refreshStudentTable(STUDENT_TABLE_ALL, 0);
				}
			}
		});
		importStudentPike13Item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.importStudentsFromPike13();
				refreshStudentTable(STUDENT_TABLE_ALL, 0);
			}
		});
		importActivityLogFileItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					controller.importActivitiesFromFile(fileChooser.getSelectedFile());
					refreshActivityTable(controller.getAllActivities(), "");
				}
			}
		});
		importActivityLogPike13Item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Get start date for import
				DatePickerUtility datePicker = new DatePickerUtility();
				String startDate = datePicker.getDialogResponse();
				if (startDate != null) {
					controller.importActivitiesFromPike13(startDate);
					refreshActivityTable(controller.getAllActivities(), "");
				}
			}
		});
		importGithubItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Get start date for import
				DatePickerUtility datePicker = new DatePickerUtility();
				String startDate = datePicker.getDialogResponse();
				if (startDate != null) {
					controller.importGithubComments(startDate);
					refreshActivityTable(controller.getAllActivities(), "");
				}
			}
		});
		importScheduleItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.importScheduleFromPike13();
				refreshScheduleTable();
			}
		});
		importAllDatabasesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Get start date for import
				DatePickerUtility datePicker = new DatePickerUtility();
				String startDate = datePicker.getDialogResponse();
				if (startDate != null) {
					controller.importAllDatabases(startDate);
					refreshLogTable();
				}
			}
		});
	}

	private void createStudentMenu(JMenu studentMenu) {
		// Create sub-menus for the Students menu
		JMenuItem studentNotInMasterMenu = new JMenuItem("View inactive students ");
		JMenuItem studentRemoveInactiveMenu = new JMenuItem("Remove inactive students ");
		JMenuItem studentViewAllMenu = new JMenuItem("View all students ");

		// Add these sub-menus to the Student menu
		studentMenu.add(studentNotInMasterMenu);
		studentMenu.add(studentRemoveInactiveMenu);
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
		studentViewAllMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshStudentTable(STUDENT_TABLE_ALL, 0);
			}
		});
	}

	private void createActivityMenu(JMenu activitiesMenu) {
		// Create sub-menus for the Activity menu
		JMenu activitiesViewByClassMenu = new JMenu("View by Class ");
		JMenuItem activitiesViewAllItem = new JMenuItem("View all ");
		activitiesMenu.add(activitiesViewByClassMenu);
		activitiesMenu.add(activitiesViewAllItem);

		// Set up listeners for Activities menu
		for (int i = 0; i < classMenuNames.length; i++) {
			int classFilter = i;
			JMenu subMenu = new JMenu(classMenuNames[i]);
			activitiesViewByClassMenu.add(subMenu);

			subMenu.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					viewClassMenuListener(subMenu, classFilter);
				}
			});
		}

		activitiesViewAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshActivityTable(controller.getAllActivities(), "");
			}
		});
	}

	private void createScheduleMenu(JMenu scheduleMenu) {
		// Create sub-menu for the Schedule menu
		JMenuItem scheduleViewMenu = new JMenuItem("View Class Schedule ");
		scheduleMenu.add(scheduleViewMenu);

		// Set up listeners for Schedule menu
		scheduleViewMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshScheduleTable();
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
		// filter: 0 - 8 for levels 0-8, 9 for labs
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

					// Add activity table and header
					refreshActivityTable(controller.getActivitiesByClassName(classItem.getText()),
							" for '" + classItem.getText() + "'");
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
				activityTable.showActivitiesByPerson(studentName, controller.getActivitiesByClientID(clientID));
			}

			@Override
			public void viewAttendanceByClass(String className) {
				// Display class by class name
				refreshActivityTable(controller.getActivitiesByClassName(className), " for '" + className + "'");
			}

			@Override
			public void removeStudent(int clientID) {
				controller.removeStudentByClientID(clientID);
				refreshStudentTable(currentStudentTable, 0);
			}
		};

		// Now provide this listener to each table
		scheduleTable.setTableListener(listener);
		activityTable.setTableListener(listener);
		studentTable.setTableListener(listener);
		logTable.setTableListener(listener);
	}

	private void refreshStudentTable(int tableType, int clientID) {
		// Remove data being displayed
		removeDataFromTables();

		// Add student table and header
		if (tableType == STUDENT_TABLE_ALL) {
			headerLabel.setText(STUDENT_TITLE);
			studentTable.setData(tablePanel, controller.getAllStudents());
		} else if (tableType == STUDENT_TABLE_NOT_IN_MASTER_DB) {
			headerLabel.setText(STUDENTS_NOT_IN_MASTER_TITLE);
			studentTable.setData(tablePanel, controller.getStudentsNotInMasterDB());
		} else { // CURR_TABLE_BY_STUDENT
			headerLabel.setText(STUDENT_TITLE);
			studentTable.setData(tablePanel, controller.getStudentByClientID(clientID));
		}

		// Update current table type
		currentStudentTable = tableType;
		activeTable = studentTable.getTable();
		activeTableHeader = headerLabel.getText();
	}

	private void refreshActivityTable(ArrayList<ActivityModel> list, String titleExtension) {
		// Remove data being displayed
		removeDataFromTables();

		// Add activity table and header
		activityTable.setData(tablePanel, list);
		headerLabel.setText(ACTIVITY_TITLE + titleExtension);

		activeTable = activityTable.getTable();
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

	private void removeDataFromTables() {
		// Remove data from Student table and Activities table
		studentTable.removeData();
		activityTable.removeData();
		logTable.removeData();
		scheduleTable.removeData();
	}

	public static void shutdown() {
		// Disconnect database and dispose of frame before exiting
		if (controller != null)
			controller.disconnectDatabase();
		frame.dispose();
		System.exit(0);
	}
}

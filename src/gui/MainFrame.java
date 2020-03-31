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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import controller.Controller;
import model.CoursesModel;
import model.LocationLookup;
import model.LogDataModel;
import model.ScheduleModel;
import model.StudentModel;
import model.TableHistoryModel;
import model_for_gui.AttendanceModel;
import model_for_gui.GithubModel;

public class MainFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 1340;
	private static final int PREF_FRAME_HEIGHT = 700;

	private static final int PREF_TABLE_PANEL_WIDTH = PREF_FRAME_WIDTH;
	private static final int PREF_TABLE_PANEL_HEIGHT = PREF_FRAME_HEIGHT - 58;
	
	private static final int MAX_TABLE_HISTORY_DEPTH = 12;

	private static final String STUDENT_TITLE = "League Student Info";
	private static final String STUDENT_EMAIL_TITLE = "League Student Emails";
	private static final String STUDENT_PHONE_TITLE = "League Student Phone Numbers";
	private static final String STUDENT_TA_TITLE = "League Student TA's";
	private static final String STUDENTS_NOT_IN_MASTER_TITLE = "Inactive League Students";
	private static final String ATTENDANCE_TITLE = "League Attendance";
	private static final String SCHEDULE_TITLE = "Weekly Class Schedule";
	private static final String SCHED_DETAILS_TITLE = "Weekly Class Details for Levels 0 - 8";
	private static final String COURSE_TITLE = "Workshops and Summer Slam Schedule";
	private static final String GITHUB_TITLE = "Students with no Github comments since ";
	private static final String LOGGING_TITLE = "Logging Data";

	private static final int STUDENT_TABLE_ALL = 0;
	private static final int STUDENT_TABLE_NOT_IN_MASTER_DB = 1;
	private static final int STUDENT_TABLE_BY_STUDENT = 2;
	private static final int STUDENT_TABLE_EMAIL_ALL = 3;
	private static final int STUDENT_TABLE_PHONE_ALL = 4;
	private static final int STUDENT_TABLE_EMAIL_BY_STUDENT = 5;
	private static final int STUDENT_TABLE_PHONE_BY_STUDENT = 6;
	private static final int STUDENT_TABLE_FOR_TA = 7;
	
	private static final int ATTEND_TABLE_ALL = 0;
	private static final int ATTEND_TABLE_BY_CLIENT_ID = 1;
	private static final int ATTEND_TABLE_BY_CLASS_NAME = 2;
	private static final int ATTEND_TABLE_BY_CLASS_AND_DATE = 3;
	private static final int ATTEND_TABLE_BY_COURSE_NAME = 4;
	private static final int ATTEND_TABLE_BY_COURSE_AND_DATE = 5;

	// Report missing github if 3 or more classes with no github in the last 35 days
	private static final int NO_RECENT_GITHUB_SINCE_DAYS = 35;
	private static final int MIN_CLASSES_WITH_NO_GITHUB = 3;

	/* Private instance variables */
	private static Controller controller;
	private JPanel mainPanel;
	private JPanel tablePanel = new JPanel();
	private JLabel headerLabel = new JLabel();
	private StudentTable studentTable;
	private AttendanceTable attendanceTable;
	private LogTable logTable;
	private ScheduleTable scheduleTable;
	private SchedDetailsTable schedDetailsTable;
	private GithubTable githubTable;
	private CoursesTable coursesTable;
	private JTable activeTable;
	private JTextField searchField;
	private String activeTableHeader;
	private ImageIcon icon;
	private static JFrame frame = new JFrame();
	private TableHistoryModel currTable;
	private ArrayList<TableHistoryModel> tableHistoryList = new ArrayList<TableHistoryModel>();

	// Class and Help menu names
	private final int fileMenuIdx = 0, studentMenuIdx = 1, attendMenuIdx = 2, schedMenuIdx = 3;
	private String[] menuDescripNames = { "File Menu ", "Student Menu ", "Attendance Menu ", "Schedule Menu " };

	public MainFrame() {
		frame.setTitle("League Student Tracker");
		frame.setLayout(new BorderLayout());
		frame.setBackground(Color.WHITE);

		icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		frame.setIconImage(icon.getImage());

		// Get database password
		PasswordDialog pwDialog = new PasswordDialog();
		String awsPassword = pwDialog.getDialogResponse();

		// Create components
		mainPanel = new JPanel(new BorderLayout());
		frame.add(mainPanel);
		controller = new Controller(frame, awsPassword, icon);

		// Check if database key file exists
		File tmpFile = new File(controller.getKeyFilePath());
		if (!tmpFile.exists()) {
			JOptionPane.showMessageDialog(frame,
					"Missing key file. \nPlease place the key file in the executable directory.  \n",
					"Failure connecting to database", JOptionPane.ERROR_MESSAGE, icon);
			shutdown();
		}

		// Connect to database
		if (!controller.connectDatabase()) {
			JOptionPane.showMessageDialog(frame,
					"Verify that the password you entered is correct \n"
							+ "and that the Student Tracker is not already running. \n",
					"Failure connecting to database", JOptionPane.ERROR_MESSAGE, icon);
			shutdown();
		}

		// Configure header
		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		headerLabel.setFont(CustomFonts.TITLE_FONT);
		headerLabel.setForeground(CustomFonts.TITLE_COLOR);
		headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		new TableHeaderBox(headerLabel);
		mainPanel.add(TableHeaderBox.refreshHeader(TableHeaderBox.HDR_EMPTY, false), BorderLayout.NORTH);

		// Configure panel and each table
		tablePanel.setPreferredSize(new Dimension(PREF_TABLE_PANEL_WIDTH, PREF_TABLE_PANEL_HEIGHT));
		LocationLookup.setLocationData(controller.getLocationList());
		attendanceTable = new AttendanceTable(tablePanel, new ArrayList<AttendanceModel>());
		logTable = new LogTable(tablePanel, new ArrayList<LogDataModel>());
		scheduleTable = new ScheduleTable(tablePanel);
		schedDetailsTable = new SchedDetailsTable(tablePanel, new ArrayList<ScheduleModel>());
		githubTable = new GithubTable(tablePanel, new ArrayList<GithubModel>());
		coursesTable = new CoursesTable(tablePanel, new ArrayList<CoursesModel>());
		studentTable = new StudentTable(tablePanel, controller.getActiveStudents());
		activeTable = studentTable.getTable();

		// Default tables to display all data
		headerLabel.setText(STUDENT_TITLE + " (" + studentTable.getTableRowCount() + " Students)");
		activeTableHeader = STUDENT_TITLE;
		currTable = new TableHistoryModel(TableHistoryModel.STUDENT_TABLE, activeTableHeader, 0, STUDENT_TABLE_ALL, "");

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

		// Add help menu to menu bar
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		createHelpMenu(helpMenu);

		// Add search label & field on the right side of menu bar
		JLabel searchLabel = new JLabel(" Search  ");
		searchLabel.setForeground(CustomFonts.TITLE_COLOR);
		searchLabel.setFont(CustomFonts.TABLE_ITALIC_TEXT_FONT);

		searchField = new JTextField();
		searchField.setMaximumSize(new Dimension(15000, searchField.getPreferredSize().height));
		searchField.setPreferredSize(new Dimension(30, searchField.getPreferredSize().height));
		searchField.setBorder(BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true));
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if (activeTable == studentTable.getTable())
					studentTable.updateSearchField(searchField.getText());
				else if (activeTable == attendanceTable.getTable())
					attendanceTable.updateSearchField(searchField.getText());
				else if (activeTable == coursesTable.getTable())
					coursesTable.updateSearchField(searchField.getText());
				else if (activeTable == logTable.getTable())
					logTable.updateSearchField(searchField.getText());
				else if (activeTable == githubTable.getTable())
					githubTable.updateSearchField(searchField.getText());
				else if (activeTable == scheduleTable.getTable())
					scheduleTable.updateSearchField(searchField.getText());
				else if (activeTable == schedDetailsTable.getTable())
					schedDetailsTable.updateSearchField(searchField.getText());
			}
		});

		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(searchLabel);
		menuBar.add(searchField);

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
				refreshLogTable(true);
			}
		});
		clearLogDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.clearDbLogData();
				refreshLogTable(true);
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
					JOptionPane.showMessageDialog(frame, e1.getMessage(), "Printer Failure", JOptionPane.ERROR_MESSAGE,
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
		// JMenuItem studentRemoveInactiveMenu = new JMenuItem("Remove inactive students ");
		JMenuItem studentNoRecentGitItem = new JMenuItem("View students without recent Github ");
		JMenuItem studentViewEmailMenu = new JMenuItem("View Student Email ");
		JMenuItem studentViewPhoneMenu = new JMenuItem("View Student Phone ");
		JMenuItem studentViewTAMenu = new JMenuItem("View Student TA's ");
		JMenuItem studentViewAllMenu = new JMenuItem("View all active students ");

		// Add these sub-menus to the Student menu
		studentMenu.add(studentNotInMasterMenu);
		studentMenu.add(studentNoRecentGitItem);
		studentMenu.add(studentViewEmailMenu);
		studentMenu.add(studentViewPhoneMenu);
		studentMenu.add(studentViewTAMenu);
		studentMenu.add(studentViewAllMenu);

		// Set up listeners for the Student menu
		studentNotInMasterMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Show all students not in master database
				refreshStudentTable(STUDENT_TABLE_NOT_IN_MASTER_DB, 0, true);
			}
		});
		studentNoRecentGitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshGithubTable(true);
			}
		});
		studentViewEmailMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshStudentTable(STUDENT_TABLE_EMAIL_ALL, 0, true);
			}
		});
		studentViewPhoneMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshStudentTable(STUDENT_TABLE_PHONE_ALL, 0, true);
			}
		});
		studentViewTAMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TableHeaderBox.clearFilters();
				refreshStudentTable(STUDENT_TABLE_FOR_TA, 0, true);
			}
		});
		studentViewAllMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshStudentTable(STUDENT_TABLE_ALL, 0, true);
			}
		});
	}

	private void createAttendanceMenu(JMenu attendanceMenu) {
		// Create sub-menus for the Attendance menu
		JMenuItem attendanceViewAllItem = new JMenuItem("View All Attendance ");
		attendanceMenu.add(attendanceViewAllItem);

		// Set up listeners for Attendance menu
		attendanceViewAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshAttendanceTable(ATTEND_TABLE_ALL, "", "", "", "", false, true);
			}
		});
	}

	private void createScheduleMenu(JMenu scheduleMenu) {
		// Create sub-menu for the Schedule menu
		JMenuItem scheduleViewMenu = new JMenuItem("View Weekly Class Schedule ");
		JMenuItem schedDetailMenu = new JMenuItem("View Weekly Class Details ");
		JMenuItem courseViewMenu = new JMenuItem("View Workshop and Summer Slam Schedule ");
		scheduleMenu.add(scheduleViewMenu);
		scheduleMenu.add(schedDetailMenu);
		scheduleMenu.add(courseViewMenu);

		// Set up listeners for Schedule menu
		scheduleViewMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshScheduleTable(true);
			}
		});
		schedDetailMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TableHeaderBox.clearFilters();
				refreshSchedDetailsTable(TableHeaderBox.getDowSelectList(), true);
			}
		});
		courseViewMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshCoursesTable(true);
			}
		});
	}

	private void createHelpMenu(JMenu helpMenu) {
		// Create sub-menus for the Help menu
		JMenu menuDescription = new JMenu("Menu Descriptions ");
		JMenuItem exampleUsageItem = new JMenuItem("Example usage ");
		JMenuItem searchFilterCopyItem = new JMenuItem("Search, filter, copy ");
		JMenuItem locationCodesItem = new JMenuItem("Location Codes ");
		JMenuItem aboutItem = new JMenuItem("About League Student Tracker ");

		// Add these sub-menus to the Help menu
		helpMenu.add(menuDescription);
		helpMenu.add(exampleUsageItem);
		helpMenu.add(searchFilterCopyItem);
		helpMenu.add(locationCodesItem);
		helpMenu.addSeparator();
		helpMenu.add(aboutItem);

		// Set up listeners for Menu Description help menu
		for (int i = 0; i < menuDescripNames.length; i++) {
			int menuFilter = i;
			JMenuItem subMenu = new JMenuItem(menuDescripNames[i]);
			menuDescription.add(subMenu);

			subMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (menuFilter == fileMenuIdx)
						new NotesWindow(NotesWindow.FILE_MENU);
					else if (menuFilter == studentMenuIdx)
						new NotesWindow(NotesWindow.STUDENT_MENU);
					else if (menuFilter == attendMenuIdx)
						new NotesWindow(NotesWindow.ATTENDANCE_MENU);
					else if (menuFilter == schedMenuIdx)
						new NotesWindow(NotesWindow.SCHEDULE_MENU);
				}
			});
		}

		// Set up listeners for each of the sub-menus
		exampleUsageItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NotesWindow(NotesWindow.EXAMPLES);
			}
		});
		searchFilterCopyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NotesWindow(NotesWindow.SEARCH_COPY);
			}
		});
		locationCodesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NotesWindow(NotesWindow.LOCATION_CODES);
			}
		});
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NotesWindow(NotesWindow.ABOUT);
			}
		});
	}

	private void createTableListeners() {
		TableListeners listener = new TableListeners() {
			@Override
			public void viewStudentTableByStudent(int clientID) {
				refreshStudentTable(STUDENT_TABLE_BY_STUDENT, clientID, true);
			}

			@Override
			public void viewAttendanceByStudent(String clientID, String studentName) {
				refreshAttendanceTable(ATTEND_TABLE_BY_CLIENT_ID, clientID, "", "", " for " + studentName, true, true);
			}

			@Override
			public void viewAttendanceByClass(String className, String classDate, boolean sinceDateEna) {
				// Display class by class name
				if (className.toLowerCase().contains("make-up") || className.startsWith("L@")) {
					refreshAttendanceTable(ATTEND_TABLE_BY_CLASS_AND_DATE, "", className, classDate,
							" for '" + className + "' on " + classDate, false, true);
				} else if (className.contains("Intro to Java") || className.toLowerCase().contains("slam")) {
					refreshAttendanceTable(ATTEND_TABLE_BY_COURSE_AND_DATE, "", className, classDate,
							" for '" + className + "' on " + classDate, false, true);
				} else
					refreshAttendanceTable(ATTEND_TABLE_BY_CLASS_NAME, "", className, "", " for '" + className + "'",
							sinceDateEna, true);
			}

			@Override
			public void viewAttendanceByCourse(String courseName) {
				// Get attendance by course name
				refreshAttendanceTable(ATTEND_TABLE_BY_COURSE_NAME, "", courseName, "", " for '" + courseName + "'",
						false, true);
			}

			@Override
			public void updateGithubUser(String clientID, String name) {
				// Get Github user name
				new GithubUserDialog(clientID, name);
			}

			@Override
			public void viewEmailByStudent(int clientID) {
				// Get student's email info
				refreshStudentTable(STUDENT_TABLE_EMAIL_BY_STUDENT, clientID, true);
			}

			@Override
			public void viewPhoneByStudent(int clientID) {
				// Get student's phone numbers
				refreshStudentTable(STUDENT_TABLE_PHONE_BY_STUDENT, clientID, true);
			}

			@Override
			public void viewActiveTAs() {
				// View active TA's using TA table filters
				refreshStudentTable(STUDENT_TABLE_FOR_TA, 0, false);
			}

			@Override
			public void viewClassDetails(boolean[] dowSelectList) {
				// View class details using DOW filters
				refreshSchedDetailsTable(dowSelectList, false);
			}
			
			@Override
			public void deleteLogEntry(int logID) {
				// Delete entry from Log Table
				controller.deleteDbLogEntry(logID);
				refreshLogTable(false);
			}

			@Override
			public void viewPreviousPage() {
				int size = tableHistoryList.size();
				if (size > 0) {
					TableHistoryModel tbl = tableHistoryList.get(size - 1);
					tableHistoryList.remove(size - 1);
					searchField.setText(tbl.getSearchText());
					switch (tbl.getTableType()) {
						case TableHistoryModel.STUDENT_TABLE:
							refreshStudentTable(tbl.getTblSubType(), tbl.getClientID(), false);
							break;
						case TableHistoryModel.ATTENDANCE_TABLE:
							refreshAttendanceTable(tbl.getTblSubType(), tbl.getClientID().toString(), tbl.getClassName(), 
									tbl.getClassDate(), tbl.getTableHeader(), tbl.isByStudentOrSinceDate(), false);
							break;
						case TableHistoryModel.COURSE_TABLE:
							refreshCoursesTable(false);
							break;
						case TableHistoryModel.LOG_TABLE:
							refreshLogTable(false);
							break;
						case TableHistoryModel.SCHEDULE_TABLE:
							refreshScheduleTable(false);
							break;
						case TableHistoryModel.SCHED_DETAILS_TABLE:
							refreshSchedDetailsTable(TableHeaderBox.getDowSelectList(), false);
							break;
						case TableHistoryModel.GITHUB_TABLE:
							refreshGithubTable(false);
							break;
					}
				}
			}
		};

		// Now provide this listener to each table
		scheduleTable.setTableListener(listener);
		attendanceTable.setTableListener(listener);
		studentTable.setTableListener(listener);
		logTable.setTableListener(listener);
		githubTable.setTableListener(listener);
		coursesTable.setTableListener(listener);
		schedDetailsTable.setTableListener(listener);
		TableHeaderBox.setTableListener(listener);
	}

	private void refreshStudentTable(int tableType, int clientID, boolean clearSearch) {
		// Remove data being displayed
		removeDataFromTables();
		int state = TableHeaderBox.HDR_EMPTY;

		// Add student table and header
		if (tableType == STUDENT_TABLE_ALL) {
			studentTable.setData(tablePanel, controller.getActiveStudents(), StudentTable.STANDARD_STUDENT_TABLE_TYPE,
					clearSearch);
			headerLabel.setText(STUDENT_TITLE + " (" + studentTable.getTableRowCount() + " Students)");

		} else if (tableType == STUDENT_TABLE_NOT_IN_MASTER_DB) {
			headerLabel.setText(STUDENTS_NOT_IN_MASTER_TITLE);
			studentTable.setData(tablePanel, controller.getStudentsNotInMasterDB(),
					StudentTable.STANDARD_STUDENT_TABLE_TYPE, clearSearch);

		} else if (tableType == STUDENT_TABLE_BY_STUDENT) {
			ArrayList<StudentModel> students = controller.getStudentByClientID(clientID);
			headerLabel.setText(STUDENT_TITLE + " for " + students.get(0).getNameModel().toString());
			studentTable.setData(tablePanel, students, StudentTable.STANDARD_STUDENT_TABLE_TYPE, clearSearch);

		} else if (tableType == STUDENT_TABLE_EMAIL_ALL) {
			headerLabel.setText(STUDENT_EMAIL_TITLE);
			studentTable.setData(tablePanel, controller.getActiveStudents(), StudentTable.EMAIL_STUDENT_TABLE_TYPE,
					clearSearch);

		} else if (tableType == STUDENT_TABLE_PHONE_ALL) {
			headerLabel.setText(STUDENT_PHONE_TITLE);
			studentTable.setData(tablePanel, controller.getActiveStudents(), StudentTable.PHONE_STUDENT_TABLE_TYPE,
					clearSearch);
		}

		else if (tableType == STUDENT_TABLE_EMAIL_BY_STUDENT) {
			ArrayList<StudentModel> students = controller.getStudentByClientID(clientID);
			headerLabel.setText(STUDENT_EMAIL_TITLE + " for " + students.get(0).getNameModel().toString());
			studentTable.setData(tablePanel, students, StudentTable.EMAIL_STUDENT_TABLE_TYPE, clearSearch);
		}

		else if (tableType == STUDENT_TABLE_PHONE_BY_STUDENT) {
			ArrayList<StudentModel> students = controller.getStudentByClientID(clientID);
			headerLabel.setText(STUDENT_PHONE_TITLE + " for " + students.get(0).getNameModel().toString());
			studentTable.setData(tablePanel, students, StudentTable.PHONE_STUDENT_TABLE_TYPE, clearSearch);
		}

		else { // STUDENT_TABLE_FOR_TA
			headerLabel.setText(STUDENT_TA_TITLE);
			state = TableHeaderBox.HDR_STUDENT_TA;
			TableHeaderBox.refreshHeader(state, (tableHistoryList.size() > 0));
			studentTable.setData(tablePanel, controller.getActiveTAs(TableHeaderBox.getMinClasses(),
					TableHeaderBox.getMinAge(), TableHeaderBox.getMinLevel()), StudentTable.TA_STUDENT_TABLE_TYPE, clearSearch);
		}

		if (clearSearch) {
			if (currTable.getTableType() != TableHistoryModel.STUDENT_TABLE || currTable.getTblSubType() != tableType) {
				// Save last table type
				currTable.setSearchText(searchField.getText());
				addTableToHistory(currTable);
			}
			searchField.setText("");
			studentTable.updateSearchField("");
		}

		// Update current table type
		activeTable = studentTable.getTable();
		activeTableHeader = headerLabel.getText();
		currTable = new TableHistoryModel(TableHistoryModel.STUDENT_TABLE, activeTableHeader, clientID, tableType, searchField.getText());
		TableHeaderBox.refreshHeader(state, (tableHistoryList.size() > 0));
	}

	private void refreshAttendanceTable(int attendType, String clientID, String className, String classDate,
			String titleExtension, boolean byStudentOrSinceDate, boolean newTable) {
		
		// Only refresh if something has changed, otherwise the table won't detect "fireTableDataChanged"
		if (currTable.getTableType() == TableHistoryModel.ATTENDANCE_TABLE 
			  && ((currTable.getTblSubType() == ATTEND_TABLE_BY_CLASS_NAME && currTable.getClassName().equals(className)) 
				  || (currTable.getTblSubType() == ATTEND_TABLE_BY_CLASS_AND_DATE && currTable.getClassName().equals(className) && currTable.getClassDate().equals(classDate))
				  || (currTable.getTblSubType() == ATTEND_TABLE_BY_COURSE_AND_DATE && currTable.getClassName().equals(className) && currTable.getClassDate().equals(classDate))))
		{
			attendanceTable.clearSelectedRows();
			return;
		}
		
		// Remove data being displayed
		removeDataFromTables();

		// Get appropriate list based on attendance type
		ArrayList<AttendanceModel> list = null;
		switch (attendType) {
			case ATTEND_TABLE_ALL:
				list = controller.getAllAttendance();
				break;
			case ATTEND_TABLE_BY_CLIENT_ID:
				list = controller.getAttendanceByClientID(clientID);
				break;
			case ATTEND_TABLE_BY_CLASS_NAME:
				list = controller.getAttendanceByClassName(className, byStudentOrSinceDate);
				break;
			case ATTEND_TABLE_BY_CLASS_AND_DATE:
				list = controller.getAttendanceByClassByDate(className, classDate);
				break;
			case ATTEND_TABLE_BY_COURSE_NAME:
				list = controller.getAttendanceByCourseName(className);
				break;
			case ATTEND_TABLE_BY_COURSE_AND_DATE:
				list = controller.getAttendanceByCourseByDate(className, classDate);
				break;
		}

		// Add attendance table and header
		if (byStudentOrSinceDate && attendType != ATTEND_TABLE_BY_CLASS_NAME)
			attendanceTable.setAttendDataByStudent(tablePanel, list);
		else
			attendanceTable.setAllAttendanceData(tablePanel, list);
		headerLabel.setText(ATTENDANCE_TITLE + titleExtension);

		if (newTable) {
			if (currTable.getTableType() != TableHistoryModel.ATTENDANCE_TABLE || currTable.getTblSubType() != attendType 
					|| !currTable.getClassName().equals(className) || !currTable.getClassDate().equals(classDate)) {
				// Save last table type
				currTable.setSearchText(searchField.getText());
				addTableToHistory(currTable);
			}
			searchField.setText("");
			attendanceTable.updateSearchField("");
		}

		// Update current table type
		activeTable = attendanceTable.getTable();
		activeTableHeader = headerLabel.getText();
		currTable = new TableHistoryModel(TableHistoryModel.ATTENDANCE_TABLE, titleExtension, clientID, attendType, 
				searchField.getText(), className, classDate, byStudentOrSinceDate);
		TableHeaderBox.refreshHeader(TableHeaderBox.HDR_EMPTY, (tableHistoryList.size() > 0));
	}

	private void refreshLogTable(boolean newTable) {
		// Remove data being displayed
		removeDataFromTables();

		// Add log data table and header
		logTable.setData(tablePanel, controller.getDbLogData());
		headerLabel.setText(LOGGING_TITLE);
		if (newTable) {
			if (currTable.getTableType() != TableHistoryModel.LOG_TABLE) {
				// Save last table type
				currTable.setSearchText(searchField.getText());
				addTableToHistory(currTable);
			}
			searchField.setText("");
			logTable.updateSearchField("");
		}
				
		// Update current table type
		activeTable = logTable.getTable();
		activeTableHeader = headerLabel.getText();
		currTable = new TableHistoryModel(TableHistoryModel.LOG_TABLE, activeTableHeader, 0, 0, searchField.getText());
		TableHeaderBox.refreshHeader(TableHeaderBox.HDR_EMPTY, (tableHistoryList.size() > 0));
	}

	private void refreshScheduleTable(boolean newTable) {
		// Remove data being displayed
		removeDataFromTables();

		// Add log data table and header
		scheduleTable.setData(tablePanel, controller.getClassSchedule());
		headerLabel.setText(SCHEDULE_TITLE);
		if (newTable) {
			if (currTable.getTableType() != TableHistoryModel.SCHEDULE_TABLE) {
				// Save last table type
				currTable.setSearchText(searchField.getText());
				addTableToHistory(currTable);
			}
			searchField.setText("");
			scheduleTable.updateSearchField("");
		}
						
		// Update current table type
		activeTable = scheduleTable.getTable();
		activeTableHeader = headerLabel.getText();
		currTable = new TableHistoryModel(TableHistoryModel.SCHEDULE_TABLE, activeTableHeader, 0, 0, searchField.getText());
		TableHeaderBox.refreshHeader(TableHeaderBox.HDR_EMPTY, (tableHistoryList.size() > 0));
	}

	private void refreshSchedDetailsTable(boolean[] dowSelectList, boolean clearSearch) {
		// Remove data being displayed
		removeDataFromTables();

		// Add schedule data table and header
		schedDetailsTable.setData(tablePanel, controller.getWeeklyClassDetails(dowSelectList), clearSearch);
		headerLabel.setText(SCHED_DETAILS_TITLE);
		if (clearSearch) {
			if (currTable.getTableType() != TableHistoryModel.SCHED_DETAILS_TABLE) {
				// Save last table type
				currTable.setSearchText(searchField.getText());
				addTableToHistory(currTable);
			}
			searchField.setText("");
			schedDetailsTable.updateSearchField("");
		}

		// Update current table type
		activeTable = schedDetailsTable.getTable();
		activeTableHeader = headerLabel.getText();
		currTable = new TableHistoryModel(TableHistoryModel.SCHED_DETAILS_TABLE, activeTableHeader, 0, 0, searchField.getText());
		TableHeaderBox.refreshHeader(TableHeaderBox.HDR_CLASS_DETAILS, (tableHistoryList.size() > 0));
	}

	private void refreshCoursesTable(boolean newTable) {
		// Remove data being displayed
		removeDataFromTables();

		// Add log data table and header
		coursesTable.setData(tablePanel, controller.getCourseSchedule());
		headerLabel.setText(COURSE_TITLE);
		if (newTable) {
			if (currTable.getTableType() != TableHistoryModel.COURSE_TABLE) {
				// Save last table type
				currTable.setSearchText(searchField.getText());
				addTableToHistory(currTable);
			}
			searchField.setText("");
			coursesTable.updateSearchField("");
		}
										
		// Update current table type
		activeTable = coursesTable.getTable();
		activeTableHeader = headerLabel.getText();
		currTable = new TableHistoryModel(TableHistoryModel.COURSE_TABLE, activeTableHeader, 0, 0, searchField.getText());
		TableHeaderBox.refreshHeader(TableHeaderBox.HDR_EMPTY, (tableHistoryList.size() > 0));
	}
	
	private void refreshGithubTable(boolean newTable) {
		// Remove data being displayed
		removeDataFromTables();

		// Add github data and header
		String sinceDate = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"))
				.minusDays(NO_RECENT_GITHUB_SINCE_DAYS).toString("yyyy-MM-dd");
		headerLabel.setText(GITHUB_TITLE + sinceDate + " (Levels 0 - 5)");
		githubTable.setData(tablePanel,
				controller.getStudentsWithNoRecentGithub(sinceDate, MIN_CLASSES_WITH_NO_GITHUB));

		if (newTable) {
			if (currTable.getTableType() != TableHistoryModel.GITHUB_TABLE) {
				// Save last table type
				currTable.setSearchText(searchField.getText());
				addTableToHistory(currTable);
			}
			searchField.setText("");
			coursesTable.updateSearchField("");
		}
												
		// Update current table type
		activeTable = githubTable.getTable();
		activeTableHeader = headerLabel.getText();
		currTable = new TableHistoryModel(TableHistoryModel.GITHUB_TABLE, activeTableHeader, 0, 0, searchField.getText());
		TableHeaderBox.refreshHeader(TableHeaderBox.HDR_EMPTY, (tableHistoryList.size() > 0));
	}

	private void addTableToHistory (TableHistoryModel table)
	{
		tableHistoryList.add(table);
		while (tableHistoryList.size() > MAX_TABLE_HISTORY_DEPTH)
			tableHistoryList.remove(0);
	}

	private void removeDataFromTables() {
		// Remove data from Student table and Attendance table
		studentTable.removeData();
		attendanceTable.removeData();
		logTable.removeData();
		scheduleTable.removeData();
		githubTable.removeData();
		coursesTable.removeData();
		schedDetailsTable.removeData();
	}

	public static void shutdown() {
		// Disconnect database and dispose of frame before exiting
		if (controller != null)
			controller.disconnectDatabase();
		frame.dispose();
		System.exit(0);
	}
}

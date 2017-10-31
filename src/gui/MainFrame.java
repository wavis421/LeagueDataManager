package gui;

/**
 * File: MainFrame.java
 * -----------------------
 * This class creates the GUI for the League App.
 **/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controller.Controller;
import model.ActivityModel;
import model.LogDataModel;
import model.StudentNameModel;

public class MainFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 1100;
	private static final int PREF_FRAME_HEIGHT = 700;

	private static final int PREF_TABLE_PANEL_WIDTH = PREF_FRAME_WIDTH;
	private static final int PREF_TABLE_PANEL_HEIGHT = PREF_FRAME_HEIGHT - 58;

	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT_1ROW = 30;
	private static final int POPUP_HEIGHT_2ROWS = 50;

	private static final String STUDENT_TITLE = "League Student Info";
	private static final String STUDENTS_NOT_IN_MASTER_TITLE = "League Students not in Master DB";
	private static final String ACTIVITY_TITLE = "League Attendance";

	private static final int STUDENT_TABLE_ALL = 0;
	private static final int STUDENT_TABLE_NOT_IN_MASTER_DB = 1;
	private static final int STUDENT_TABLE_BY_STUDENT = 2;

	private static final int ACTIVITY_TABLE_ALL = 0;
	private static final int ACTIVITY_TABLE_BY_CLASS = 1;
	private static final int ACTIVITY_TABLE_BY_STUDENT = 2;

	/* Private instance variables */
	private static Controller controller;
	private JPanel mainPanel;
	private JPanel tablePanel = new JPanel();
	private JLabel headerLabel = new JLabel();
	private StudentTable studentTable;
	private ActivityTable activityTable;
	private LogTable logTable;
	private int currentStudentTable;
	private int currentActivityTable;
	private JFileChooser fileChooser;
	private FileFilterCsv fileFilter;
	private String selectedClassName;
	private static JFrame frame = new JFrame();

	public MainFrame() {
		frame.setTitle("League Data Manager");
		frame.setLayout(new BorderLayout());
		frame.setBackground(Color.WHITE);

		ImageIcon icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		frame.setIconImage(icon.getImage());

		// Create components
		mainPanel = new JPanel(new BorderLayout());
		frame.add(mainPanel);
		controller = new Controller(frame);

		// Configure header
		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		headerLabel.setFont(CustomFonts.TITLE_FONT);
		headerLabel.setForeground(CustomFonts.TITLE_COLOR);
		mainPanel.add(headerLabel, BorderLayout.NORTH);

		// Default to student table with all students
		currentStudentTable = STUDENT_TABLE_ALL;
		headerLabel.setText(STUDENT_TITLE);

		// Configure panel and each tables
		tablePanel.setPreferredSize(new Dimension(PREF_TABLE_PANEL_WIDTH, PREF_TABLE_PANEL_HEIGHT));
		activityTable = new ActivityTable(tablePanel, new ArrayList<ActivityModel>());
		logTable = new LogTable(tablePanel, new ArrayList<LogDataModel>());
		studentTable = new StudentTable(tablePanel, controller.getAllStudents());

		createStudentTablePopups();
		createActivityTablePopups();

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

		// Set up top level menus and add to menu bar
		JMenu fileMenu = new JMenu("File");
		JMenu studentMenu = new JMenu("Students");
		JMenu activitiesMenu = new JMenu("Attendance");

		menuBar.add(fileMenu);
		menuBar.add(studentMenu);
		menuBar.add(activitiesMenu);

		// Add file sub-menus
		JMenuItem importStudentFileItem = new JMenuItem("Import Students from CSV File...  ");
		JMenuItem importStudentPike13Item = new JMenuItem("Import Students from Pike13...  ");
		JMenuItem importActivityLogFileItem = new JMenuItem("Import Attendance Log from CSV File...  ");
		JMenuItem importActivityLogPike13Item = new JMenuItem("Import Attendance Log from Pike13...  ");
		JMenuItem importGithubItem = new JMenuItem("Import Github comments...  ");
		JMenuItem importGithubLevel0Item = new JMenuItem("Import Github comments for Level 0...  ");
		JMenuItem viewLogDataItem = new JMenuItem("View Log Data ");
		JMenuItem clearLogDataItem = new JMenuItem("Clear Log Data ");
		JMenuItem exitItem = new JMenuItem("Exit ");
		fileMenu.add(importStudentFileItem);
		fileMenu.add(importStudentPike13Item);
		fileMenu.add(importActivityLogFileItem);
		fileMenu.add(importActivityLogPike13Item);
		fileMenu.add(importGithubItem);
		fileMenu.add(importGithubLevel0Item);
		fileMenu.addSeparator();
		fileMenu.add(viewLogDataItem);
		fileMenu.add(clearLogDataItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Add Students sub-menus
		JMenuItem studentNotInMasterMenu = new JMenuItem("View students not in master DB ");
		JMenuItem studentRemoveInactiveMenu = new JMenuItem("Remove inactive students ");
		JMenuItem studentViewAllMenu = new JMenuItem("View all students ");
		studentMenu.add(studentNotInMasterMenu);
		studentMenu.add(studentRemoveInactiveMenu);
		studentMenu.add(studentViewAllMenu);

		// Add activities sub-menus
		JMenu activitiesViewByClassMenu = new JMenu("View by Class ");
		JMenuItem activitiesViewAllItem = new JMenuItem("View all ");
		activitiesMenu.add(activitiesViewByClassMenu);
		activitiesMenu.add(activitiesViewAllItem);

		// Create listeners
		createFileMenuListeners(importStudentFileItem, importStudentPike13Item, importActivityLogFileItem,
				importActivityLogPike13Item, importGithubItem, importGithubLevel0Item, viewLogDataItem,
				clearLogDataItem, exitItem);
		createStudentMenuListeners(studentNotInMasterMenu, studentRemoveInactiveMenu, studentViewAllMenu);
		createActivityMenuListeners(activitiesViewByClassMenu, activitiesViewAllItem);

		return menuBar;
	}

	private void createFileMenuListeners(JMenuItem importStudentFile, JMenuItem importStudentsPike13,
			JMenuItem importActivitesFile, JMenuItem importActivitiesPike13, JMenuItem importGithub,
			JMenuItem importGithubLevel0, JMenuItem viewLogData, JMenuItem clearLogData, JMenuItem exitItem) {
		// Set up listeners for FILE menu
		importStudentFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					controller.importStudentsFromFile(fileChooser.getSelectedFile());
					refreshStudentTable(STUDENT_TABLE_ALL, 0);
				}
			}
		});
		importStudentsPike13.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.importStudentsFromPike13();
				refreshStudentTable(STUDENT_TABLE_ALL, 0);
			}
		});
		importActivitesFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					controller.importActivitiesFromFile(fileChooser.getSelectedFile());
					refreshActivityTable(ACTIVITY_TABLE_ALL, controller.getAllActivities(), "");
				}
			}
		});
		importActivitiesPike13.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Get start date for import
				DatePickerUtility datePicker = new DatePickerUtility();
				String startDate = datePicker.getDialogResponse();
				if (startDate != null) {
					controller.importActivitiesFromPike13(startDate);
					refreshActivityTable(ACTIVITY_TABLE_ALL, controller.getAllActivities(), "");
				}
			}
		});
		importGithub.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Get start date for import
				DatePickerUtility datePicker = new DatePickerUtility();
				String startDate = datePicker.getDialogResponse();
				if (startDate != null) {
					controller.importGithubComments(startDate);
					refreshActivityTable(ACTIVITY_TABLE_ALL, controller.getAllActivities(), "");
				}
			}
		});
		importGithubLevel0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Get start date for import
				DatePickerUtility datePicker = new DatePickerUtility();
				String startDate = datePicker.getDialogResponse();
				if (startDate != null) {
					controller.importGithubCommentsByLevel(0, startDate);
					refreshActivityTable(ACTIVITY_TABLE_ALL, controller.getAllActivities(), "");
				}
			}
		});
		viewLogData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshLogTable();
			}
		});
		clearLogData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.clearDbLogData();
				refreshLogTable();
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shutdown();
			}
		});
	}

	private void createStudentMenuListeners(JMenuItem studentNotInMaster, JMenuItem studentRemoveInvactive,
			JMenuItem studentViewAll) {
		// Set up listeners for STUDENT menu
		studentNotInMaster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Show all students not in master database
				refreshStudentTable(STUDENT_TABLE_NOT_IN_MASTER_DB, 0);
			}
		});
		studentRemoveInvactive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.removeInactiveStudents();
				refreshStudentTable(STUDENT_TABLE_NOT_IN_MASTER_DB, 0);
			}
		});
		studentViewAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshStudentTable(STUDENT_TABLE_ALL, 0);
			}
		});
	}

	private void createActivityMenuListeners(JMenu activitiesViewByClass, JMenuItem activitiesViewAll) {
		// Set up listeners for Activities menu
		activitiesViewByClass.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				activitiesViewByClass.removeAll();
				activitiesViewByClass.getPopupMenu().setLayout(new GridLayout(20, 1));
				ArrayList<String> classList = controller.getAllClassNames();

				for (int i = 0; i < classList.size(); i++) {
					JMenuItem classItem = new JMenuItem(classList.get(i).toString());
					activitiesViewByClass.add(classItem);

					classItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							classList.clear();
							activitiesViewByClass.removeAll();

							// Add activity table and header
							refreshActivityTable(ACTIVITY_TABLE_BY_CLASS,
									controller.getActivitiesByClassName(classItem.getText()),
									"  for  \"" + classItem.getText() + "\"");
						}
					});
				}
			}
		});
		activitiesViewAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshActivityTable(ACTIVITY_TABLE_ALL, controller.getAllActivities(), "");
			}
		});
	}

	private void createStudentTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem removeStudentItem = new JMenuItem("Remove student ");
		JMenuItem showStudentActivityItem = new JMenuItem("Show attendance ");
		tablePopup.add(showStudentActivityItem);
		tablePopup.add(removeStudentItem);

		// POP UP action listeners
		removeStudentItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get row, model, and clientID for the row
				int row = studentTable.getTable().convertRowIndexToModel(studentTable.getTable().getSelectedRow());
				StudentTableModel model = (StudentTableModel) studentTable.getTable().getModel();
				int clientID = Integer.parseInt((String) model.getValueAt(row, StudentTableModel.CLIENT_ID_COLUMN));

				// Remove student from database
				controller.removeStudentByClientID(clientID);
				studentTable.getTable().clearSelection();

				// Refresh current table
				refreshStudentTable(currentStudentTable, 0);
			}
		});
		showStudentActivityItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student name for selected row/column
				int modelRow = studentTable.getTable().convertRowIndexToModel(studentTable.getTable().getSelectedRow());
				StudentTableModel model = (StudentTableModel) studentTable.getTable().getModel();
				String clientID = (String) model.getValueAt(modelRow, StudentTableModel.CLIENT_ID_COLUMN);
				StudentNameModel studentName = (StudentNameModel) model.getValueAt(modelRow,
						StudentTableModel.STUDENT_NAME_COLUMN);

				// Display activity table for selected student
				activityTable.showActivitiesByPerson(studentName.toString(),
						controller.getActivitiesByClientID(clientID));
				studentTable.getTable().clearSelection();
			}
		});
		studentTable.getTable().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				JTable table = studentTable.getTable();
				int row = table.getSelectedRow();
				if (e.getButton() == MouseEvent.BUTTON3 && row != -1) {
					row = table.convertRowIndexToModel(row);
					StudentTableModel model = (StudentTableModel) table.getModel();

					// Either add or remove the "remove student" item
					if (((StudentNameModel) model.getValueAt(row, StudentTableModel.STUDENT_NAME_COLUMN))
							.getIsInMasterDb()) {
						tablePopup.remove(removeStudentItem);
						tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_1ROW));
					} else {
						tablePopup.add(removeStudentItem);
						tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_2ROWS));
					}
					tablePopup.show(table, e.getX(), e.getY());
				}
			}
		});
	}

	private void createActivityTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem showStudentClassItem = new JMenuItem("Show class ");
		JMenuItem showStudentInfoItem = new JMenuItem("Show student info ");
		tablePopup.add(showStudentInfoItem);
		tablePopup.add(showStudentClassItem);
		tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_1ROW));

		// POP UP action listeners
		showStudentClassItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Add activity table and header for selected class
				refreshActivityTable(ACTIVITY_TABLE_BY_CLASS, controller.getActivitiesByClassName(selectedClassName),
						"  for  \"" + selectedClassName + "\"");
				studentTable.getTable().clearSelection();
			}
		});
		showStudentInfoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get Client ID for selected row/column
				int row = activityTable.getTable().convertRowIndexToModel(activityTable.getTable().getSelectedRow());
				ActivityTableModel model = (ActivityTableModel) activityTable.getTable().getModel();
				int clientID = Integer.parseInt((String) model.getValueAt(row, ActivityTableModel.CLIENT_ID_COLUMN));

				refreshStudentTable(STUDENT_TABLE_BY_STUDENT, clientID);
				studentTable.getTable().clearSelection();
			}
		});
		activityTable.getTable().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				JTable table = activityTable.getTable();
				int row = table.getSelectedRow();

				if (e.getButton() == MouseEvent.BUTTON1 && row > -1
						&& table.getSelectedColumn() == ActivityTableModel.GITHUB_COMMENTS_COLUMN) {
					// Highlight selected row in github event table
					activityTable.setSelectedEventRow(row, e.getY());

				} else if (e.getButton() == MouseEvent.BUTTON3 && row > -1) {
					if (table.getSelectedColumn() == ActivityTableModel.STUDENT_NAME_COLUMN) {
						// Show student's info
						tablePopup.remove(showStudentClassItem);
						tablePopup.add(showStudentInfoItem);
						tablePopup.show(table, e.getX(), e.getY());

					} else if (table.getSelectedColumn() == ActivityTableModel.GITHUB_COMMENTS_COLUMN
							&& currentActivityTable != ACTIVITY_TABLE_BY_CLASS) {
						// Show students by class name
						selectedClassName = activityTable.getClassNameByRow(row, table.convertRowIndexToModel(row),
								e.getY());
						if (selectedClassName != null) {
							tablePopup.remove(showStudentInfoItem);
							tablePopup.add(showStudentClassItem);
							tablePopup.show(table, e.getX(), e.getY());
						}
					}
				}
			}

			public void mouseClicked(MouseEvent e) {
				JTable table = activityTable.getTable();
				if (e.getClickCount() == 2 && table.getSelectedColumn() == ActivityTableModel.GITHUB_COMMENTS_COLUMN) {
					int row = table.getSelectedRow();
					if (row > -1) {
						String clientID = (String) table.getValueAt(row, ActivityTableModel.CLIENT_ID_COLUMN);
						activityTable.showActivitiesByPerson(
								table.getValueAt(row, ActivityTableModel.STUDENT_NAME_COLUMN).toString(),
								controller.getActivitiesByClientID(clientID));
					}
				}
			}
		});
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
	}

	private void refreshActivityTable(int tableType, ArrayList<ActivityModel> list, String titleExtension) {
		// Remove data being displayed
		removeDataFromTables();

		// Add activity table and header
		activityTable.setData(tablePanel, list);
		headerLabel.setText(ACTIVITY_TITLE + titleExtension);

		currentActivityTable = tableType;
	}

	private void refreshLogTable() {
		// Remove data being displayed
		removeDataFromTables();

		// Add log data table and header
		logTable.setData(tablePanel, controller.getDbLogData());
		headerLabel.setText(controller.getLogDataTitle());
	}

	private void removeDataFromTables() {
		// Remove data from Student table and Activities table
		studentTable.removeData();
		activityTable.removeData();
		logTable.removeData();
	}

	public static void shutdown() {
		// Disconnect database and dispose of frame before exiting
		if (controller != null)
			controller.disconnectDatabase();
		frame.dispose();
		System.exit(0);
	}
}

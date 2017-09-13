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
import model.ActivityTableModel;
import model.StudentNameModel;
import model.StudentTableModel;

public class MainFrame extends JFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 975;
	private static final int PREF_FRAME_HEIGHT = 700;

	private static final int PREF_TABLE_PANEL_WIDTH = PREF_FRAME_WIDTH;
	private static final int PREF_TABLE_PANEL_HEIGHT = PREF_FRAME_HEIGHT - 58;

	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT_1ROW = 30;
	private static final int POPUP_HEIGHT_2ROWS = 50;

	private static final String STUDENT_TITLE = "League Student Info";
	private static final String STUDENTS_NOT_IN_MASTER_TITLE = "League Students not in Master DB";
	private static final String ACTIVITY_TITLE = "League Attendance";
	private static final String LOG_DATA_TITLE = "Database Import Logging Data";

	private static final int CURR_TABLE_ALL_STUDENTS = 0;
	private static final int CURR_TABLE_STUDENTS_NOT_IN_MASTER_DB = 1;
	private static final int CURR_TABLE_BY_STUDENT = 2;

	/* Private instance variables */
	private static Controller controller;
	private JPanel mainPanel;
	private JPanel tablePanel = new JPanel();
	private JLabel headerLabel = new JLabel();
	private StudentTable studentTable;
	private ActivityTable activityTable;
	private LogTable logTable;
	private int currentStudentTable;
	private JFileChooser fileChooser;
	private FileFilterCsv fileFilter;

	public MainFrame() {
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		ImageIcon icon = new ImageIcon(getClass().getResource("PPicon24.png"));
		setIconImage(icon.getImage());

		// Create components
		mainPanel = new JPanel(new BorderLayout());
		add(mainPanel);

		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		headerLabel.setFont(CustomFonts.TITLE_FONT);
		headerLabel.setForeground(CustomFonts.TITLE_COLOR);
		mainPanel.add(headerLabel, BorderLayout.NORTH);

		controller = new Controller((JFrame) MainFrame.this);
		tablePanel.setPreferredSize(new Dimension(PREF_TABLE_PANEL_WIDTH, PREF_TABLE_PANEL_HEIGHT));
		headerLabel.setText(STUDENT_TITLE);
		studentTable = new StudentTable(tablePanel, controller.getAllStudents());
		currentStudentTable = CURR_TABLE_ALL_STUDENTS;
		createStudentTablePopups();

		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(5, 1, 1, 1);
		tablePanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		mainPanel.add(tablePanel, BorderLayout.CENTER);

		fileChooser = new JFileChooser();
		fileFilter = new FileFilterCsv();

		setJMenuBar(createMenuBar());
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setFileFilter(fileFilter);

		// Make form visible
		pack();
		setSize(PREF_FRAME_WIDTH, PREF_FRAME_HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
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
		JMenuItem importStudentsItem = new JMenuItem("Import Students...  ");
		JMenuItem importActivityLogItem = new JMenuItem("Import Attendance Log...  ");
		JMenuItem viewLogDataItem = new JMenuItem("View Log Data ");
		JMenuItem exitItem = new JMenuItem("Exit ");
		fileMenu.add(importStudentsItem);
		fileMenu.add(importActivityLogItem);
		fileMenu.addSeparator();
		fileMenu.add(viewLogDataItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Add Students sub-menus
		JMenuItem studentNotInMasterMenu = new JMenuItem("View students not in master DB ");
		JMenuItem studentViewAllMenu = new JMenuItem("View all students ");
		studentMenu.add(studentNotInMasterMenu);
		studentMenu.add(studentViewAllMenu);

		// Add activities sub-menus
		JMenu activitiesViewByClassMenu = new JMenu("View by Class ");
		JMenuItem activitiesViewAllItem = new JMenuItem("View all ");
		activitiesMenu.add(activitiesViewByClassMenu);
		activitiesMenu.add(activitiesViewAllItem);

		// Create listeners
		createFileMenuListeners(importStudentsItem, importActivityLogItem, viewLogDataItem, exitItem);
		createStudentMenuListeners(studentNotInMasterMenu, studentViewAllMenu);
		createActivityMenuListeners(activitiesViewByClassMenu, activitiesViewAllItem);

		return menuBar;
	}

	private void createFileMenuListeners(JMenuItem importStudents, JMenuItem importActivites, JMenuItem viewLogData,
			JMenuItem exitItem) {
		// Set up listeners for FILE menu
		importStudents.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					controller.importStudentsFromFile(fileChooser.getSelectedFile());
					refreshStudentTable(CURR_TABLE_ALL_STUDENTS, 0);
				}
			}
		});
		importActivites.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					controller.importActivitiesFromFile(fileChooser.getSelectedFile());
					refreshActivityTable();
				}
			}
		});
		viewLogData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshLogTable();
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.disconnectDatabase();
				dispose();
				System.gc();
			}
		});
	}

	private void createStudentMenuListeners(JMenuItem studentNotInMaster, JMenuItem studentViewAll) {
		// Set up listeners for STUDENT menu
		studentNotInMaster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Show all students not in master database
				refreshStudentTable(CURR_TABLE_STUDENTS_NOT_IN_MASTER_DB, 0);
			}
		});
		studentViewAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshStudentTable(CURR_TABLE_ALL_STUDENTS, 0);
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

							// Remove data being displayed
							removeDataFromTables();

							// Add activity table and header
							if (activityTable == null) {
								activityTable = new ActivityTable(tablePanel,
										controller.getActivitiesByClassName(classItem.getText()), false);
								createActivityTablePopups();
							} else
								activityTable.setData(tablePanel,
										controller.getActivitiesByClassName(classItem.getText()), false);
							headerLabel.setText(ACTIVITY_TITLE + "  for  \"" + classItem.getText() + "\"");
						}
					});
				}
			}
		});
		activitiesViewAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshActivityTable();
			}
		});
	}

	private void createStudentTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem removeStudentItem = new JMenuItem("Remove student ");
		JMenuItem showStudentActivityItem = new JMenuItem("Show activities ");
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
				StudentNameModel studentName = (StudentNameModel) model.getValueAt(modelRow,
						StudentTableModel.STUDENT_NAME_COLUMN);

				// Remove data being displayed
				removeDataFromTables();

				// Display activity table for selected student
				if (activityTable == null) {
					activityTable = new ActivityTable(tablePanel, controller.getActivitiesByStudentName(studentName),
							true);
					createActivityTablePopups();
				} else {
					activityTable.setData(tablePanel, controller.getActivitiesByStudentName(studentName), true);
				}
				headerLabel.setText(ACTIVITY_TITLE + "  for  " + studentName);
				studentTable.getTable().clearSelection();
			}
		});
		studentTable.getTable().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				JTable table = studentTable.getTable();
				if (e.getButton() == MouseEvent.BUTTON3 && table.getSelectedRow() != -1) {
					int row = table.convertRowIndexToModel(table.getSelectedRow());
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
		tablePopup.add(showStudentClassItem);
		tablePopup.add(showStudentInfoItem);
		tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_2ROWS));

		// POP UP action listeners
		showStudentClassItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get class name for selected row/column
				int row = activityTable.getTable().convertRowIndexToModel(activityTable.getTable().getSelectedRow());
				ActivityTableModel model = (ActivityTableModel) activityTable.getTable().getModel();
				String className = (String) model.getValueAt(row, ActivityTableModel.CLASS_NAME_COLUMN);

				System.out.println("Show class info not yet implemented: " + className);
				studentTable.getTable().clearSelection();
			}
		});
		showStudentInfoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get Client ID for selected row/column
				int row = activityTable.getTable().convertRowIndexToModel(activityTable.getTable().getSelectedRow());
				ActivityTableModel model = (ActivityTableModel) activityTable.getTable().getModel();
				int clientID = Integer.parseInt((String) model.getValueAt(row, ActivityTableModel.CLIENT_ID_COLUMN));

				refreshStudentTable(CURR_TABLE_BY_STUDENT, clientID);
				studentTable.getTable().clearSelection();
			}
		});
		activityTable.getTable().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				JTable table = activityTable.getTable();
				if (e.getButton() == MouseEvent.BUTTON3 && table.getSelectedRow() != -1) {
					tablePopup.show(table, e.getX(), e.getY());
				}
			}
		});
	}

	private void refreshStudentTable(int tableType, int clientID) {
		// Remove data being displayed
		removeDataFromTables();

		// Add student table and header
		if (tableType == CURR_TABLE_ALL_STUDENTS) {
			headerLabel.setText(STUDENT_TITLE);
			studentTable.setData(tablePanel, controller.getAllStudents());
		} else if (tableType == CURR_TABLE_STUDENTS_NOT_IN_MASTER_DB) {
			headerLabel.setText(STUDENTS_NOT_IN_MASTER_TITLE);
			studentTable.setData(tablePanel, controller.getStudentsNotInMasterDB());
		} else { // CURR_TABLE_BY_STUDENT
			headerLabel.setText(STUDENT_TITLE);
			studentTable.setData(tablePanel,  controller.getStudentByClientID(clientID));
		}

		// Update current table type
		currentStudentTable = tableType;
	}

	private void refreshActivityTable() {
		// Remove data being displayed
		removeDataFromTables();

		// Add activity table and header
		if (activityTable == null) {
			activityTable = new ActivityTable(tablePanel, controller.getAllActivities(), true);
			createActivityTablePopups();
		} else
			activityTable.setData(tablePanel, controller.getAllActivities(), true);
		headerLabel.setText(ACTIVITY_TITLE);
	}

	private void refreshLogTable() {
		// Remove data being displayed
		removeDataFromTables();

		// Add log data table and header
		if (logTable == null)
			logTable = new LogTable(tablePanel, controller.getDbLogData());
		else
			logTable.setData(tablePanel, controller.getDbLogData());
		headerLabel.setText(LOG_DATA_TITLE);
	}

	private void removeDataFromTables() {
		// Remove data from Student table and Activities table
		studentTable.removeData();
		if (activityTable != null)
			activityTable.removeData();
		if (logTable != null)
			logTable.removeData();
	}
}

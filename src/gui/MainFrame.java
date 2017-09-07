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
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controller.Controller;

public class MainFrame extends JFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 975;
	private static final int PREF_FRAME_HEIGHT = 700;

	private static final int PREF_TABLE_PANEL_WIDTH = PREF_FRAME_WIDTH;
	private static final int PREF_TABLE_PANEL_HEIGHT = PREF_FRAME_HEIGHT - 58;

	private static final String STUDENT_TITLE = "League Student Info";
	private static final String ACTIVITY_TITLE = "League Attendance Data";

	/* Private instance variables */
	private static Controller controller;
	private JPanel mainPanel;
	private JPanel tablePanel = new JPanel();
	private JLabel headerLabel = new JLabel();
	private StudentTable studentTable;
	private ActivityTable activityTable;
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
		JMenuItem exitItem = new JMenuItem("Exit ");
		fileMenu.add(importStudentsItem);
		fileMenu.add(importActivityLogItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Add Students sub-menus
		JMenu studentRemoveMenu = new JMenu("Remove student ");
		JMenuItem studentViewAllMenu = new JMenuItem("View all students ");
		studentMenu.add(studentRemoveMenu);
		studentMenu.add(studentViewAllMenu);

		// Add activities sub-menus
		JMenu activitiesViewByClassMenu = new JMenu("View by Class ");
		JMenuItem activitiesViewAllItem = new JMenuItem("View all ");
		activitiesMenu.add(activitiesViewByClassMenu);
		activitiesMenu.add(activitiesViewAllItem);

		// Create listeners
		createFileMenuListeners(importStudentsItem, importActivityLogItem, exitItem);
		createStudentMenuListeners(studentRemoveMenu, studentViewAllMenu);
		createActivityMenuListeners(activitiesViewByClassMenu, activitiesViewAllItem);

		return menuBar;
	}

	private void createFileMenuListeners(JMenuItem importStudents, JMenuItem importActivites, JMenuItem exitItem) {
		// Set up listeners for FILE menu
		importStudents.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					controller.importStudentsFromFile(fileChooser.getSelectedFile());
					refreshStudentTable();
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
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.disconnectDatabase();
				dispose();
				System.gc();
			}
		});
	}

	private void createStudentMenuListeners(JMenu studentRemove, JMenuItem studentViewAll) {
		// Set up listeners for STUDENT menu
		studentRemove.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

			}
		});
		studentViewAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshStudentTable();
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
							if (activityTable == null)
								activityTable = new ActivityTable(tablePanel,
										controller.getActivitiesByClassName(classItem.getText()));
							else
								activityTable.setData(tablePanel, controller.getActivitiesByClassName(classItem.getText()));
							headerLabel.setText(ACTIVITY_TITLE);
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

	private void refreshStudentTable() {
		// Remove data being displayed
		removeDataFromTables();

		// Add student table and header
		studentTable.setData(tablePanel, controller.getAllStudents());
		headerLabel.setText(STUDENT_TITLE);
	}

	private void refreshActivityTable() {
		// Remove data being displayed
		removeDataFromTables();

		// Add activity table and header
		if (activityTable == null)
			activityTable = new ActivityTable(tablePanel, controller.getAllActivities());
		else
			activityTable.setData(tablePanel, controller.getAllActivities());
		headerLabel.setText(ACTIVITY_TITLE);
	}

	private void removeDataFromTables() {
		// Remove data from Student table and Activities table
		studentTable.removeData();
		if (activityTable != null)
			activityTable.removeData();
	}
}

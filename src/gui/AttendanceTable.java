package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import model.AttendanceEventModel;
import model.StudentNameModel;
import model_for_gui.AttendanceModel;

public class AttendanceTable extends JPanel {
	private static final int TEXT_HEIGHT = 16;
	private static final int ROW_HEIGHT = (TEXT_HEIGHT * 4);
	private static final int ROW_GAP = 4;

	private static final int POPUP_MENU_WIDTH = 240;
	private static final int POPUP_MENU_HEIGHT_1ROW  = 30;
	private static final int POPUP_MENU_HEIGHT_2ROWS = 50;
	private static final int POPUP_MENU_HEIGHT_6ROWS = 130;

	// Columns for embedded event table
	private static final int EVENT_TABLE_DATE_COLUMN = 0;
	private static final int EVENT_TABLE_CLASS_NAME_COLUMN = 1;
	private static final int EVENT_TABLE_TEACHER_NAME_COLUMN = 2;
	private static final int EVENT_TABLE_REPO_NAME_COLUMN = 3;
	private static final int EVENT_TABLE_COMMENTS_COLUMN = 4;
	private static final int EVENT_TABLE_NUM_COLUMNS = 5;

	private JPanel parentTablePanel;
	private JTable mainTable, studentTable, activeTable;
	private ArrayList<JTable> githubEventTableList = new ArrayList<JTable>();
	private AttendanceTableModel attendanceTableModel;
	private AttendEventTableModel attendEventTableModel;
	private JScrollPane tableScrollPane;
	private TableListeners attendanceListener;
	private int eventTableSelectedRow = -1; // table row
	private int eventSelectedRow = -1; // row within table row
	private String selectedClassName, selectedClassDate;

	private TableRowSorter<AttendanceTableModel> rowSorter;

	public AttendanceTable(JPanel tablePanel, ArrayList<AttendanceModel> attendanceList) {
		this.parentTablePanel = tablePanel;

		// Create main & student table-model and table
		attendanceTableModel  = new AttendanceTableModel(attendanceList);
		attendEventTableModel = new AttendEventTableModel(new ArrayList<AttendanceEventModel>());
		mainTable = new JTable(attendanceTableModel);
		studentTable = new JTable(attendEventTableModel);

		// Create event sub-table with github comments by date
		createAttendSubTable(attendanceList, githubEventTableList);

		// Configure table panel and pop-ups
		createTablePanel(mainTable, tablePanel, githubEventTableList);
		createAttendanceTablePopups();
		createStudAttendTablePopups();

		// Configure row sorter for main table only
		rowSorter = new TableRowSorter<AttendanceTableModel>((AttendanceTableModel) mainTable.getModel());
		rowSorter.setSortable(AttendanceTableModel.GITHUB_COMMENTS_COLUMN, false);
	}

	public void setTableListener(TableListeners listener) {
		this.attendanceListener = listener;
	}

	public JTable getTable() {
		return activeTable;
	}

	public void setAllAttendanceData(JPanel tablePanel, ArrayList<AttendanceModel> attendanceList) {
		// Clear event row selections
		eventTableSelectedRow = -1;
		eventSelectedRow = -1;

		// Set data for main table		
		activeTable = mainTable;
		attendanceTableModel.setData(attendanceList);

		// Create github sub-table
		createAttendSubTable(attendanceList, githubEventTableList);
		
		// Add table to scroll pane
		addTableToScrollPane(mainTable);

		// Update table
		attendanceTableModel.fireTableDataChanged();
		tableScrollPane.setVisible(true);
		tablePanel.add(tableScrollPane, BorderLayout.NORTH);
	}
	
	public void setAttendDataByStudent(JPanel tablePanel, ArrayList<AttendanceModel> attendanceList) {
		// Clear event row selections
		eventTableSelectedRow = -1;
		eventSelectedRow = -1;

		// Set data for student table; check for empty attendance
		activeTable = studentTable;
		if (attendanceList.size() > 0)
			attendEventTableModel.setData (attendanceList.get(0).getAttendanceEventList());
		else 
			attendEventTableModel.setData (new ArrayList<AttendanceEventModel>());
		
		// Add table to scroll pane
		addTableToScrollPane (studentTable);
				
		// Update table
		attendEventTableModel.fireTableDataChanged();
		tableScrollPane.setVisible(true);
		tablePanel.add(tableScrollPane, BorderLayout.NORTH);
	}

	public void removeData() {
		// Remove data from tables and lists
		githubEventTableList.clear();
		if (attendanceTableModel.getRowCount() > 0)
			attendanceTableModel.removeAll();
		if (attendEventTableModel.getRowCount() > 0)
			attendEventTableModel.removeAll();
		
		tableScrollPane.setVisible(false);
	}

	public void setSelectedEventRow(int selectedRow, int yPos) {
		eventSelectedRow = getEventRow(selectedRow, yPos);
		activeTable.repaint();
	}

	private int getEventRow(int selectedRow, int yPos) {
		// Compute row based on Y-position in event table
		JTable table = githubEventTableList.get(selectedRow);
		int row = (yPos - (selectedRow * ROW_HEIGHT)) / (TEXT_HEIGHT - 1);
		if (row < table.getModel().getRowCount())
			return row;
		else
			return -1;
	}

	private void addTableToScrollPane (JTable table)
	{
		// Add table to scroll pane
		tableScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tableScrollPane.setPreferredSize(new Dimension(parentTablePanel.getPreferredSize().width, parentTablePanel.getPreferredSize().height - 70));
		tableScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));		
	}

	private void createTablePanel(JTable table, JPanel panel, ArrayList<JTable> eventList) {
		// >>> Set up main table parameters <<<
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		table.setShowGrid(true);
		table.getTableHeader().setDefaultRenderer(new AttendanceHeaderRenderer());

		// Configure column height and width for main table
		table.setRowHeight(ROW_HEIGHT);
		table.getColumnModel().getColumn(AttendanceTableModel.CLIENT_ID_COLUMN).setMaxWidth(66);
		table.getColumnModel().getColumn(AttendanceTableModel.STUDENT_NAME_COLUMN).setMaxWidth(240);
		table.getColumnModel().getColumn(AttendanceTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(170);
		table.getColumnModel().getColumn(AttendanceTableModel.STUDENT_AGE_COLUMN).setMaxWidth(48);

		// Set table properties
		table.setDefaultRenderer(Object.class, new AttendanceTableRenderer(eventList));
		table.setAutoCreateRowSorter(true);
		new TableKeystrokeHandler(table);
		
		// >>> Set up student table <<<
		studentTable.setFont(CustomFonts.TABLE_TEXT_FONT);
		studentTable.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		studentTable.setShowGrid(true);
		studentTable.getTableHeader().setDefaultRenderer(new AttendanceHeaderRenderer());

		// Configure column height and width for student table
		studentTable.setRowHeight(studentTable.getRowHeight() + ROW_GAP);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setPreferredWidth(100);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setMaxWidth(100);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setMaxWidth(250);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setPreferredWidth(200);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_TEACHER_NAME_COLUMN).setMaxWidth(550);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_TEACHER_NAME_COLUMN).setPreferredWidth(280);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_REPO_NAME_COLUMN).setMaxWidth(550);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_REPO_NAME_COLUMN).setPreferredWidth(280);
		studentTable.getColumnModel().getColumn(EVENT_TABLE_COMMENTS_COLUMN).setPreferredWidth(280);
				
		// Set student table properties
		studentTable.setDefaultRenderer(Object.class, new EventTableRenderer(new ArrayList<JTable>()));

		// Create panel containing main table with scroll
		activeTable = mainTable;
		addTableToScrollPane (table);
		panel.setLayout(new BorderLayout());
		panel.add(tableScrollPane, BorderLayout.NORTH);
	}

	private void createAttendanceTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem showStudentClassItem = new JMenuItem("Show class ");
		JMenuItem showStudentInfoItem = new JMenuItem("Show student info ");
		JMenuItem showStudentAttendanceItem = new JMenuItem("Show student attendance ");
		JMenuItem showStudentEmailItem = new JMenuItem("Show student email ");
		JMenuItem showStudentPhoneItem = new JMenuItem("Show student phone ");
		JMenuItem updateGithubUserItem = new JMenuItem("Update Github user name ");
		tablePopup.add(showStudentInfoItem);
		tablePopup.add(showStudentClassItem);
		tablePopup.add(showStudentAttendanceItem);
		tablePopup.add(showStudentEmailItem);
		tablePopup.add(showStudentPhoneItem);
		tablePopup.add(updateGithubUserItem);

		// POP UP action listeners
		showStudentClassItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Add attendance table and header for selected class
				mainTable.clearSelection();
				attendanceListener.viewAttendanceByClass(selectedClassName, selectedClassDate);
			}
		});
		showStudentInfoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get Client ID for selected row/column
				int row = mainTable.convertRowIndexToModel(mainTable.getSelectedRow());
				AttendanceTableModel model = (AttendanceTableModel) mainTable.getModel();
				int clientID = Integer.parseInt((String) model.getValueAt(row, AttendanceTableModel.CLIENT_ID_COLUMN));

				mainTable.clearSelection();
				attendanceListener.viewStudentTableByStudent(clientID);
			}
		});
		showStudentAttendanceItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student name & Client ID for selected row/column
				int row = mainTable.convertRowIndexToModel(mainTable.getSelectedRow());
				AttendanceTableModel model = (AttendanceTableModel) mainTable.getModel();
				String clientID = (String) model.getValueAt(row, AttendanceTableModel.CLIENT_ID_COLUMN);
				StudentNameModel studentName = (StudentNameModel) model.getValueAt(row,
						AttendanceTableModel.STUDENT_NAME_COLUMN);

				// Display attendance table for selected student
				mainTable.clearSelection();
				attendanceListener.viewAttendanceByStudent(clientID, studentName.toString());
			}
		});
		showStudentEmailItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student name & Client ID for selected row/column
				int row = mainTable.convertRowIndexToModel(mainTable.getSelectedRow());
				AttendanceTableModel model = (AttendanceTableModel) mainTable.getModel();
				String clientID = (String) model.getValueAt(row, AttendanceTableModel.CLIENT_ID_COLUMN);

				// Display email table for selected student
				mainTable.clearSelection();
				attendanceListener.viewEmailByStudent(Integer.parseInt(clientID));
			}
		});
		showStudentPhoneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student name & Client ID for selected row/column
				int row = mainTable.convertRowIndexToModel(mainTable.getSelectedRow());
				AttendanceTableModel model = (AttendanceTableModel) mainTable.getModel();
				String clientID = (String) model.getValueAt(row, AttendanceTableModel.CLIENT_ID_COLUMN);

				// Display phone number table for selected student
				mainTable.clearSelection();
				attendanceListener.viewPhoneByStudent(Integer.parseInt(clientID));
			}
		});
		updateGithubUserItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get Client ID for selected row/column
				int row = mainTable.convertRowIndexToModel(mainTable.getSelectedRow());
				AttendanceTableModel model = (AttendanceTableModel) mainTable.getModel();
				String clientID = (String) model.getValueAt(row, AttendanceTableModel.CLIENT_ID_COLUMN);
				StudentNameModel studentNameModel = (StudentNameModel) model.getValueAt(row,
						AttendanceTableModel.STUDENT_NAME_COLUMN);

				mainTable.clearSelection();
				attendanceListener.updateGithubUser(clientID, studentNameModel.toString());
			}
		});
		mainTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = mainTable.getSelectedRow();

				if (e.getButton() == MouseEvent.BUTTON1 && row > -1) {
					if (mainTable.getSelectedColumn() == AttendanceTableModel.GITHUB_COMMENTS_COLUMN) {
						// Highlight selected row in github event table
						setSelectedEventRow(row, e.getY());

					}

				} else if (e.getButton() == MouseEvent.BUTTON3 && row > -1) {
					if (mainTable.getSelectedColumn() == AttendanceTableModel.STUDENT_NAME_COLUMN) {
						// Show student's info
						tablePopup.remove(showStudentClassItem);
						tablePopup.add(showStudentInfoItem);
						tablePopup.add(showStudentAttendanceItem);
						tablePopup.add(showStudentEmailItem);
						tablePopup.add(showStudentPhoneItem);
						tablePopup.add(updateGithubUserItem);
						tablePopup.setPreferredSize(new Dimension(POPUP_MENU_WIDTH, POPUP_MENU_HEIGHT_6ROWS));
						tablePopup.show(mainTable, e.getX(), e.getY());

					} else if (mainTable.getSelectedColumn() == AttendanceTableModel.GITHUB_COMMENTS_COLUMN) {
						// Show students by class name
						if (eventSelectedRow > -1) {
							int modelRow = mainTable.convertRowIndexToModel(row);
							EventTableModel eventModel = (EventTableModel) githubEventTableList.get(modelRow)
									.getModel();
							selectedClassName = (String) eventModel.getValueAt(eventSelectedRow, EVENT_TABLE_CLASS_NAME_COLUMN);
							selectedClassDate = (String) eventModel.getValueAt(eventSelectedRow, EVENT_TABLE_DATE_COLUMN);

							if (selectedClassName != null && !selectedClassName.startsWith("Intro to Java") && !selectedClassName.contains("Make-Up")
									&& !selectedClassName.startsWith("EL@") && !selectedClassName.startsWith("EL @") 
									&& !selectedClassName.contains("Leave of Absence") && !selectedClassName.toLowerCase().contains("slam")) {
								tablePopup.remove(showStudentInfoItem);
								tablePopup.remove(showStudentAttendanceItem);
								tablePopup.remove(showStudentEmailItem);
								tablePopup.remove(showStudentPhoneItem);
								tablePopup.remove(updateGithubUserItem);
								tablePopup.add(showStudentClassItem);

								// Show pop-up menu
								tablePopup.setPreferredSize(new Dimension(POPUP_MENU_WIDTH, POPUP_MENU_HEIGHT_1ROW));
								tablePopup.show(mainTable, e.getX(), e.getY());
							}
						}
					}
				}
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1
						&& mainTable.getSelectedColumn() == AttendanceTableModel.GITHUB_COMMENTS_COLUMN) {
					// Expand attendance
					int row = mainTable.getSelectedRow();
					if (row > -1) {
						String clientID = (String) mainTable.getValueAt(row, AttendanceTableModel.CLIENT_ID_COLUMN);
						String studentName = mainTable.getValueAt(row, AttendanceTableModel.STUDENT_NAME_COLUMN)
								.toString();
						attendanceListener.viewAttendanceByStudent(clientID, studentName);
					}
				}
			}
		});
	}

	private void createStudAttendTablePopups() {
		// Student Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem showStudentInfoItem = new JMenuItem("Show student info ");
		JMenuItem showStudentClassItem = new JMenuItem("Show class ");
		tablePopup.add(showStudentInfoItem);

		// POP UP action listeners for student attendance table
		showStudentInfoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get Client ID for selected row/column
				int row = studentTable.getSelectedRow();
				int clientID = (int) attendEventTableModel.getValueAt(row, AttendEventTableModel.CLIENT_ID_COLUMN);

				studentTable.clearSelection();
				attendanceListener.viewStudentTableByStudent(clientID);
			}
		});
		showStudentClassItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Add attendance table and header for selected class
				studentTable.clearSelection();
				attendanceListener.viewAttendanceByClass(selectedClassName, selectedClassDate);
			}
		});
		
		studentTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = studentTable.getSelectedRow();

				if (e.getButton() == MouseEvent.BUTTON3 && row > -1) {
					// Show students by class name
					selectedClassName = (String) attendEventTableModel.getValueAt(row, EVENT_TABLE_CLASS_NAME_COLUMN);
					selectedClassDate = (String) attendEventTableModel.getValueAt(row, EVENT_TABLE_DATE_COLUMN);

					if (selectedClassName != null && !selectedClassName.startsWith("Intro to Java") && !selectedClassName.contains("Make-Up")
							&& !selectedClassName.startsWith("EL@") && !selectedClassName.startsWith("EL @") 
							&& !selectedClassName.contains("Leave of Absence") && !selectedClassName.toLowerCase().contains("slam")) {
						// Show pop-up menu, including show-by-class
						tablePopup.add(showStudentClassItem);
						tablePopup.setPreferredSize(new Dimension(POPUP_MENU_WIDTH, POPUP_MENU_HEIGHT_2ROWS));
						tablePopup.show(studentTable, e.getX(), e.getY());
					}
					else {
						// Show pop-up menu, remove show-by-class
						tablePopup.remove(showStudentClassItem);
						tablePopup.setPreferredSize(new Dimension(POPUP_MENU_WIDTH, POPUP_MENU_HEIGHT_1ROW));
						tablePopup.show(studentTable, e.getX(), e.getY());
					}
				}
			}
		});
	}

	private void createAttendSubTable(ArrayList<AttendanceModel> tableData, ArrayList<JTable> eventList) {
		for (int i = 0; i < tableData.size(); i++) {
			// Create github sub-table
			ArrayList<AttendanceEventModel> eventData = tableData.get(i).getAttendanceEventList();
			JTable eventTable = new JTable(new EventTableModel(eventData));

			// Set table properties
			eventTable.setFont(CustomFonts.TABLE_TEXT_FONT);
			eventTable.setGridColor(CustomFonts.TABLE_GRID_COLOR);
			eventTable.setShowHorizontalLines(false);
			eventTable.setShowVerticalLines(true);
			eventTable.setTableHeader(null);

			eventTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setMaxWidth(90);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setPreferredWidth(85);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setMaxWidth(220);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setPreferredWidth(170);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_REPO_NAME_COLUMN).setMaxWidth(500);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_REPO_NAME_COLUMN).setPreferredWidth(245);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_TEACHER_NAME_COLUMN).setMaxWidth(480);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_TEACHER_NAME_COLUMN).setPreferredWidth(245);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_COMMENTS_COLUMN).setPreferredWidth(245);

			// Add renderer
			eventTable.setDefaultRenderer(Object.class, new AttendanceTableRenderer(null));

			// Add table to event panel and array list
			eventList.add(eventTable);
		}
	}

	public void updateSearchField(String searchText) {
		if (searchText.equals("")) {
			rowSorter.setRowFilter(null);
		} else {
			try {
				// Filter only on the 1st 2 columns
				rowSorter.setRowFilter(RowFilter.regexFilter("(?i)\\b" + searchText,
						AttendanceTableModel.CLIENT_ID_COLUMN, AttendanceTableModel.STUDENT_NAME_COLUMN));

			} catch (java.util.regex.PatternSyntaxException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
		mainTable.setRowSorter(rowSorter);
	}

	// ===== NESTED Class: Renderer for main table ===== //
	public class AttendanceTableRenderer extends JLabel implements TableCellRenderer {

		ArrayList<JTable> eventTable;

		private AttendanceTableRenderer(ArrayList<JTable> eventTable) {
			super();
			super.setOpaque(true);
			this.eventTable = eventTable;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			// Set foreground/background colors
			int modelRow = table.convertRowIndexToModel(row);
			if (table == mainTable
					&& ((AttendanceTableModel) mainTable.getModel()).getGithubNameByRow(modelRow) == null)
				super.setForeground(CustomFonts.TITLE_COLOR);
			else
				super.setForeground(Color.black);

			if (isSelected)
				super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
			else
				super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

			// All columns centered except Github Comments
			super.setVerticalAlignment(TOP);
			if (table != mainTable && column == EVENT_TABLE_COMMENTS_COLUMN)
				super.setHorizontalAlignment(LEFT);
			else
				super.setHorizontalAlignment(CENTER);

			setFont(CustomFonts.TABLE_TEXT_FONT);
			if (value instanceof String) {
				// String columns
				if (table != mainTable && isSelected && eventSelectedRow == row)
					super.setForeground(CustomFonts.ICON_COLOR);

				super.setText(((String) value));
				return this;

			} else if (value instanceof StudentNameModel) {
				// Students not in master DB are in italics
				if (((StudentNameModel) value).getIsInMasterDb() == false)
					setFont(CustomFonts.TABLE_ITALIC_TEXT_FONT);

				StudentNameModel student = (StudentNameModel) value;
				super.setText(student.toString());
				return this;

			} else {
				// Github comments column
				if (isSelected && row != eventTableSelectedRow) {
					// Selecting a new row; remove previous selection
					for (int i = 0; i < eventTable.size(); i++)
						eventTable.get(i).clearSelection();

					// Add and track current row selection and update
					eventTable.get(modelRow).selectAll();
					eventTableSelectedRow = row;
					mainTable.repaint();
				}
				return eventTable.get(modelRow);
			}
		}
	}

	public class EventTableRenderer extends JLabel implements TableCellRenderer {

		ArrayList<JTable> eventTable;

		private EventTableRenderer(ArrayList<JTable> eventTable) {
			super();
			super.setOpaque(true);
			this.eventTable = eventTable;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			// Set foreground/background colors
			super.setForeground(Color.black);

			if (isSelected)
				super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
			else
				super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

			// All columns centered except Github Comments
			super.setVerticalAlignment(CENTER);
			if (column == EVENT_TABLE_COMMENTS_COLUMN)
				super.setHorizontalAlignment(LEFT);
			else
				super.setHorizontalAlignment(CENTER);
			
			setFont(CustomFonts.TABLE_TEXT_FONT);
			
			if (isSelected && eventSelectedRow == row)
				super.setForeground(CustomFonts.ICON_COLOR);

			// All columns are strings
			super.setText(((String) value));
			return this;
		}
	}

	// ===== NESTED Class: Header Renderer for main table ===== //
	public class AttendanceHeaderRenderer extends JLabel implements TableCellRenderer {
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TABLE_GRID_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

		private AttendanceHeaderRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// GITHUB column is left justified, all others are centered
			if (table == mainTable && column == AttendanceTableModel.GITHUB_COMMENTS_COLUMN)
				super.setHorizontalAlignment(LEFT);
			else
				super.setHorizontalAlignment(CENTER);
			super.setFont(CustomFonts.TABLE_HEADER_FONT);
			super.setForeground(Color.black);
			setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

			super.setText(((String) value));
			return (this);
		}
	}

	// ===== NESTED Class: Model for Github sub-table ===== //
	public class EventTableModel extends AbstractTableModel {
		ArrayList<AttendanceEventModel> inputData;

		public EventTableModel(ArrayList<AttendanceEventModel> tableData) {
			inputData = tableData;
		}

		@Override
		public int getRowCount() {
			return inputData.size();
		}

		@Override
		public int getColumnCount() {
			return EVENT_TABLE_NUM_COLUMNS;
		}

		@Override
		public Object getValueAt(int row, int col) {
			AttendanceEventModel attendance = (AttendanceEventModel) inputData.get(row);

			if (col == EVENT_TABLE_DATE_COLUMN)
				return attendance.getServiceDateString();
			else if (col == EVENT_TABLE_CLASS_NAME_COLUMN)
				return attendance.getEventName();
			else if (col == EVENT_TABLE_TEACHER_NAME_COLUMN)
				return attendance.getTeacherNames();
			else if (col == EVENT_TABLE_REPO_NAME_COLUMN) {
				if (attendance.getRepoName() == null)
					return "";
				else
					return attendance.getRepoName();
			} else
				return attendance.getGithubComments();
		}
	}
}

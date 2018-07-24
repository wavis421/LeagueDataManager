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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import model.AttendanceEventModel;
import model.AttendanceModel;
import model.StudentNameModel;

public class AttendanceTable extends JPanel {
	private static final int TEXT_HEIGHT = 17;
	private static final int ROW_HEIGHT = (TEXT_HEIGHT * 4);

	private static final int POPUP_MENU_WIDTH = 240;
	private static final int POPUP_MENU_HEIGHT_1ROW = 30;
	private static final int POPUP_MENU_HEIGHT_2ROWS = 50;
	private static final int POPUP_MENU_HEIGHT_4ROWS = 90;

	// Columns for embedded event table
	private static final int EVENT_TABLE_DATE_COLUMN = 0;
	private static final int EVENT_TABLE_CLASS_NAME_COLUMN = 1;
	private static final int EVENT_TABLE_REPO_NAME_COLUMN = 2;
	private static final int EVENT_TABLE_COMMENTS_COLUMN = 3;
	private static final int EVENT_TABLE_NUM_COLUMNS = 4;

	private JPanel parentTablePanel;
	private JTable mainTable;
	private ArrayList<JTable> githubEventTableList = new ArrayList<JTable>();
	private AttendanceTableModel attendanceTableModel;
	private JScrollPane tableScrollPane;
	private TableListeners attendanceListener;
	private int eventTableSelectedRow = -1; // table row
	private int eventSelectedRow = -1; // row within table row
	private String selectedClassName, selectedClassDate, selectedStudentName, selectedClientID;

	private TableRowSorter<AttendanceTableModel> rowSorter;

	public AttendanceTable(JPanel tablePanel, ArrayList<AttendanceModel> attendanceList) {
		this.parentTablePanel = tablePanel;

		// Create main table-model and table
		attendanceTableModel = new AttendanceTableModel(attendanceList);
		mainTable = new JTable(attendanceTableModel);

		// Create event sub-table with github comments by date
		createEventTable(attendanceList, githubEventTableList);

		// Configure table panel and pop-ups
		tableScrollPane = createTablePanel(mainTable, parentTablePanel, githubEventTableList,
				parentTablePanel.getPreferredSize().height - 70);
		createAttendanceTablePopups();

		rowSorter = new TableRowSorter<AttendanceTableModel>((AttendanceTableModel) mainTable.getModel());
		rowSorter.setSortable(AttendanceTableModel.GITHUB_COMMENTS_COLUMN, false);
	}

	public void setTableListener(TableListeners listener) {
		this.attendanceListener = listener;
	}

	public JTable getTable() {
		return mainTable;
	}

	public void setData(JPanel tablePanel, ArrayList<AttendanceModel> attendanceList, boolean attendanceByStudent) {
		this.parentTablePanel = tablePanel;

		// Clear event row selections
		eventTableSelectedRow = -1;
		eventSelectedRow = -1;

		// Set data for main table
		attendanceTableModel.setData(attendanceList);

		// Go full row height if attendance for single student
		if (attendanceByStudent)
			mainTable.setRowHeight(parentTablePanel.getHeight() - 45);
		else
			mainTable.setRowHeight(ROW_HEIGHT);

		// Create github sub-table
		createEventTable(attendanceList, githubEventTableList);

		// Update table
		attendanceTableModel.fireTableDataChanged();
		tableScrollPane.setVisible(true);
		tablePanel.add(tableScrollPane, BorderLayout.NORTH);
	}

	public void removeData() {
		// Remove data from tables and lists
		if (attendanceTableModel.getRowCount() > 0) {
			attendanceTableModel.removeAll();
			githubEventTableList.clear();
		}
		tableScrollPane.setVisible(false);
	}

	public void setSelectedEventRow(int selectedRow, int yPos) {
		eventSelectedRow = getEventRow(selectedRow, yPos);
		mainTable.repaint();
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

	private JScrollPane createTablePanel(JTable table, JPanel panel, ArrayList<JTable> eventList, int panelHeight) {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		table.setShowGrid(true);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);

		// Configure column height and width
		table.setRowHeight(ROW_HEIGHT);
		table.getColumnModel().getColumn(AttendanceTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(AttendanceTableModel.STUDENT_NAME_COLUMN).setMaxWidth(220);
		table.getColumnModel().getColumn(AttendanceTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(180);

		// Set table properties
		table.setDefaultRenderer(Object.class, new AttendanceTableRenderer(eventList));
		table.setAutoCreateRowSorter(true);
		new TableKeystrokeHandler(table);

		panel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(panel.getPreferredSize().width, panelHeight));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		panel.add(scrollPane, BorderLayout.NORTH);

		return scrollPane;
	}

	private void createAttendanceTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem showStudentClassItem = new JMenuItem("Show class ");
		JMenuItem showStudentInfoItem = new JMenuItem("Show student info ");
		JMenuItem showStudentAttendanceItem = new JMenuItem("Show student attendance ");
		JMenuItem updateGithubUserItem = new JMenuItem("Update Github user name ");
		JMenuItem graduateClassItem = new JMenuItem("Graduate class ");
		JMenuItem graduateStudentItem = new JMenuItem("Graduate student ");
		tablePopup.add(showStudentInfoItem);
		tablePopup.add(showStudentClassItem);
		tablePopup.add(showStudentAttendanceItem);
		tablePopup.add(updateGithubUserItem);
		tablePopup.add(graduateClassItem);
		tablePopup.add(graduateStudentItem);

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
		graduateClassItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				mainTable.clearSelection();
				attendanceListener.graduateClass(selectedClassName);
			}
		});
		graduateStudentItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				mainTable.clearSelection();
				attendanceListener.graduateStudent(selectedClientID, selectedStudentName);
			}
		});
		mainTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = mainTable.getSelectedRow();

				if (e.getButton() == MouseEvent.BUTTON1 && row > -1) {
					if (mainTable.getSelectedColumn() == AttendanceTableModel.GITHUB_COMMENTS_COLUMN) {
						// Highlight selected row in github event table
						setSelectedEventRow(row, e.getY());

					} else {
						selectedClientID = (String) mainTable.getValueAt(row, AttendanceTableModel.CLIENT_ID_COLUMN);
						selectedStudentName = mainTable.getValueAt(row, AttendanceTableModel.STUDENT_NAME_COLUMN)
								.toString();
					}

				} else if (e.getButton() == MouseEvent.BUTTON3 && row > -1) {
					if (mainTable.getSelectedColumn() == AttendanceTableModel.STUDENT_NAME_COLUMN) {
						// Show student's info
						tablePopup.remove(showStudentClassItem);
						tablePopup.remove(graduateClassItem);
						tablePopup.add(showStudentInfoItem);
						tablePopup.add(showStudentAttendanceItem);
						tablePopup.add(updateGithubUserItem);
						tablePopup.add(graduateStudentItem);
						tablePopup.setPreferredSize(new Dimension(POPUP_MENU_WIDTH, POPUP_MENU_HEIGHT_4ROWS));
						tablePopup.show(mainTable, e.getX(), e.getY());

					} else if (mainTable.getSelectedColumn() == AttendanceTableModel.GITHUB_COMMENTS_COLUMN) {
						// Show students by class name
						if (eventSelectedRow > -1) {
							int modelRow = mainTable.convertRowIndexToModel(row);
							EventTableModel eventModel = (EventTableModel) githubEventTableList.get(modelRow)
									.getModel();
							selectedClassName = (String) eventModel.getValueAt(eventSelectedRow,
									EVENT_TABLE_CLASS_NAME_COLUMN);
							selectedClassDate = (String) eventModel.getValueAt(eventSelectedRow,
									EVENT_TABLE_DATE_COLUMN);

							if (selectedClassName != null) {
								tablePopup.remove(showStudentInfoItem);
								tablePopup.remove(showStudentAttendanceItem);
								tablePopup.remove(updateGithubUserItem);
								tablePopup.remove(graduateStudentItem);
								tablePopup.add(showStudentClassItem);

								// Check if this is a valid class to graduate
								if (GraduationDialog.isValidClassName(selectedClassName)) { // OK
									tablePopup.add(graduateClassItem);
									tablePopup
											.setPreferredSize(new Dimension(POPUP_MENU_WIDTH, POPUP_MENU_HEIGHT_2ROWS));
								} else { // Not a valid graduation class
									tablePopup.remove(graduateClassItem);
									tablePopup
											.setPreferredSize(new Dimension(POPUP_MENU_WIDTH, POPUP_MENU_HEIGHT_1ROW));
								}

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

	private void createEventTable(ArrayList<AttendanceModel> tableData, ArrayList<JTable> eventList) {
		for (int i = 0; i < tableData.size(); i++) {
			// Create github sub-table
			ArrayList<AttendanceEventModel> eventData = tableData.get(i).getAttendanceEventList();
			JTable eventTable = new JTable(new EventTableModel(eventData));

			// Set table properties
			eventTable.setFont(CustomFonts.TABLE_TEXT_FONT);
			eventTable.setGridColor(Color.white);
			eventTable.setShowGrid(true);
			eventTable.setTableHeader(null);

			eventTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setMaxWidth(90);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setPreferredWidth(90);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setMaxWidth(204);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setPreferredWidth(204);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_REPO_NAME_COLUMN).setMaxWidth(300);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_REPO_NAME_COLUMN).setPreferredWidth(275);

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

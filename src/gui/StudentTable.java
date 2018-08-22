package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import model.StudentModel;
import model.StudentNameModel;

public class StudentTable extends JPanel {
	private static final int ROW_GAP = 5;

	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT_3ROWS = 70;

	private JPanel tablePanel;
	private JTable table;
	private StudentTableModel studentTableModel;
	private TableListeners studentListener;
	private JScrollPane scrollPane;

	private TableRowSorter<StudentTableModel> rowSorter;

	public StudentTable(JPanel tablePanel, ArrayList<StudentModel> studentList) {
		this.tablePanel = tablePanel;

		studentTableModel = new StudentTableModel(studentList);
		table = new JTable(studentTableModel);

		createTablePanel();
		createStudentTablePopups();

		rowSorter = new TableRowSorter<StudentTableModel>((StudentTableModel) table.getModel());
	}

	public void setTableListener(TableListeners listener) {
		this.studentListener = listener;
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<StudentModel> studentList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		studentTableModel.setData(studentList);
		studentTableModel.fireTableDataChanged();
	}

	public void removeData() {
		if (studentTableModel.getRowCount() > 0) {
			studentTableModel.removeAll();
		}

		scrollPane.setVisible(false);
	}

	private void createTablePanel() {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		table.setShowGrid(true);
		table.getTableHeader().setDefaultRenderer(new StudentTableHeaderRenderer());
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		// Configure column widths
		table.getColumnModel().getColumn(StudentTableModel.GENDER_COLUMN).setMaxWidth(35);
		table.getColumnModel().getColumn(StudentTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(StudentTableModel.START_DATE_COLUMN).setMaxWidth(105);
		table.getColumnModel().getColumn(StudentTableModel.GRAD_YEAR_COLUMN).setMaxWidth(95);
		table.getColumnModel().getColumn(StudentTableModel.HOME_LOCATION_COLUMN).setMaxWidth(165);
		table.getColumnModel().getColumn(StudentTableModel.CURR_CLASS_COLUMN).setMaxWidth(280);

		table.getColumnModel().getColumn(StudentTableModel.START_DATE_COLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(StudentTableModel.GRAD_YEAR_COLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(StudentTableModel.HOME_LOCATION_COLUMN).setPreferredWidth(160);
		table.getColumnModel().getColumn(StudentTableModel.CURR_CLASS_COLUMN).setPreferredWidth(240);

		// Table renderer, sorter and key handler
		table.setDefaultRenderer(Object.class, new StudentTableRenderer());
		table.setCellSelectionEnabled(true);
		table.setAutoCreateRowSorter(true);
		new TableKeystrokeHandler(table);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	private void createStudentTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem showStudentAttendanceItem = new JMenuItem("Show attendance ");
		JMenuItem updateGithubUserItem = new JMenuItem("Update Github user name ");
		JMenuItem graduateStudentItem = new JMenuItem("Graduate student ");
		tablePopup.add(showStudentAttendanceItem);
		tablePopup.add(updateGithubUserItem);
		tablePopup.add(graduateStudentItem);
		tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_3ROWS));

		// POP UP action listeners
		updateGithubUserItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get row, model, and clientID for the row
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				StudentTableModel model = (StudentTableModel) table.getModel();
				String clientID = (String) model.getValueAt(row, StudentTableModel.CLIENT_ID_COLUMN);
				StudentNameModel nameModel = (StudentNameModel) model.getValueAt(row,
						StudentTableModel.STUDENT_NAME_COLUMN);

				// Send email to LeagueBot with new github user name
				studentListener.updateGithubUser(clientID, nameModel.getFirstName() + " " + nameModel.getLastName());
				table.clearSelection();
			}
		});
		showStudentAttendanceItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student name for selected row/column
				int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
				StudentTableModel model = (StudentTableModel) table.getModel();
				String clientID = (String) model.getValueAt(modelRow, StudentTableModel.CLIENT_ID_COLUMN);
				StudentNameModel studentName = (StudentNameModel) model.getValueAt(modelRow,
						StudentTableModel.STUDENT_NAME_COLUMN);

				// Display attendance table for selected student
				table.clearSelection();
				studentListener.viewAttendanceByStudent(clientID, studentName.toString());
			}
		});
		graduateStudentItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student name for selected row/column
				int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
				StudentTableModel model = (StudentTableModel) table.getModel();
				String clientID = (String) model.getValueAt(modelRow, StudentTableModel.CLIENT_ID_COLUMN);
				StudentNameModel studentName = (StudentNameModel) model.getValueAt(modelRow,
						StudentTableModel.STUDENT_NAME_COLUMN);
				String className = (String) model.getValueAt(modelRow, StudentTableModel.CURR_CLASS_COLUMN);

				table.clearSelection();
				studentListener.graduateStudent(clientID, studentName.toString(), className);
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = table.getSelectedRow();
				if (e.getButton() == MouseEvent.BUTTON3 && row != -1) {
					// Show popup menu
					tablePopup.show(table, e.getX(), e.getY());
				}
			}

			public void mouseClicked(MouseEvent e) {
				// Single row selects one cell (default), double click to get entire row
				if (e.getClickCount() == 2) {
					int col = table.getSelectedColumn();
					int row = table.getSelectedRow();
					if (row > -1 && col > -1) {
						table.setRowSelectionInterval(row, row);
						table.setColumnSelectionInterval(0, table.getColumnCount() - 1);
					}
				}
			}
		});
	}

	public void updateSearchField(String searchText) {
		if (searchText.equals("")) {
			rowSorter.setRowFilter(null);
		} else {
			try {
				rowSorter.setRowFilter(RowFilter.regexFilter("(?i)\\b" + searchText));

			} catch (java.util.regex.PatternSyntaxException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
		table.setRowSorter(rowSorter);
	}

	// ===== NESTED Class: Renderer for table ===== //
	public class StudentTableRenderer extends JLabel implements TableCellRenderer {
		private StudentTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			String text;
			if (value instanceof Integer)
				text = (String.valueOf(value));
			else if (value instanceof Date)
				text = (((Date) value).toString());
			else if (value instanceof StudentNameModel)
				text = (((StudentNameModel) value).toString());
			else
				text = ((String) value);
			setText(text);

			if (column != -1) {
				// Italics for inactive students
				if (value instanceof StudentNameModel && ((StudentNameModel) value).getIsInMasterDb() == false)
					setFont(CustomFonts.TABLE_ITALIC_TEXT_FONT);
				else
					setFont(CustomFonts.TABLE_TEXT_FONT);

				super.setForeground(Color.black);
				super.setBorder(BorderFactory.createEmptyBorder());

				int modelRow = table.convertRowIndexToModel(row);
				if (((StudentTableModel) table.getModel()).getValueByRow(modelRow).isMissingData()) {
					// Text ORANGE for students missing data
					if (column == StudentTableModel.STUDENT_NAME_COLUMN)
						super.setForeground(CustomFonts.TITLE_COLOR);

					// Border ORANGE for cells with missing data
					if (text == null || text.equals("") && column != StudentTableModel.CURR_CLASS_COLUMN)
						super.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, CustomFonts.TITLE_COLOR));
				}

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}

	// ===== NESTED Class: Renderer for table header ===== //
	public class StudentTableHeaderRenderer extends JLabel implements TableCellRenderer {
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TABLE_GRID_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		
		private StudentTableHeaderRenderer() {
			super();
			super.setOpaque(true);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.setHorizontalAlignment(CENTER);
			super.setFont(CustomFonts.TABLE_HEADER_FONT);
			super.setForeground(Color.black);
			setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

			super.setText(((String) value));
			return (this);
		}
	}
}

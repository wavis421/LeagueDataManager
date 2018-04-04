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
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;

import model.StudentModel;
import model.StudentNameModel;

public class StudentTable extends JPanel {
	private static final int ROW_GAP = 5;

	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT_1ROW = 30;
	private static final int POPUP_HEIGHT_2ROWS = 50;

	private JPanel tablePanel;
	private JTable table;
	private StudentTableModel studentTableModel;
	private TableListeners studentListener;
	private JScrollPane scrollPane;

	public StudentTable(JPanel tablePanel, ArrayList<StudentModel> studentList) {
		this.tablePanel = tablePanel;

		studentTableModel = new StudentTableModel(studentList);
		table = new JTable(studentTableModel);

		createTablePanel();
		createStudentTablePopups();
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
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		// Configure column widths
		table.getColumnModel().getColumn(StudentTableModel.GENDER_COLUMN).setMaxWidth(35);
		table.getColumnModel().getColumn(StudentTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(StudentTableModel.START_DATE_COLUMN).setMaxWidth(105);
		table.getColumnModel().getColumn(StudentTableModel.GRAD_YEAR_COLUMN).setMaxWidth(95);
		table.getColumnModel().getColumn(StudentTableModel.HOME_LOCATION_COLUMN).setMaxWidth(165);

		table.getColumnModel().getColumn(StudentTableModel.START_DATE_COLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(StudentTableModel.GRAD_YEAR_COLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(StudentTableModel.HOME_LOCATION_COLUMN).setPreferredWidth(160);

		table.setDefaultRenderer(Object.class, new StudentTableRenderer());
		table.setAutoCreateRowSorter(true);

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
		JMenuItem removeStudentItem = new JMenuItem("Remove student ");
		JMenuItem showStudentAttendanceItem = new JMenuItem("Show attendance ");
		tablePopup.add(showStudentAttendanceItem);
		tablePopup.add(removeStudentItem);

		// POP UP action listeners
		removeStudentItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get row, model, and clientID for the row
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				StudentTableModel model = (StudentTableModel) table.getModel();
				int clientID = Integer.parseInt((String) model.getValueAt(row, StudentTableModel.CLIENT_ID_COLUMN));

				// Remove student from database
				studentListener.removeStudent(clientID);
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
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
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
				
				if (((StudentTableModel) table.getModel()).getValueByRow(row).isMissingData()) {
					// Text RED for students missing data
					if (column == StudentTableModel.STUDENT_NAME_COLUMN)
						super.setForeground(Color.red);

					// Border RED for cells with missing data
					if (text == null || text.equals(""))
						super.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.red));
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
}

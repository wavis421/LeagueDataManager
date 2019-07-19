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

import model.LogDataModel;
import model.StudentNameModel;

public class LogTable extends JPanel {
	private static final int ROW_GAP = 5;
	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT_3ROWS = 70;

	private JPanel tablePanel;
	private JTable table;
	private LogTableModel logTableModel;
	private JScrollPane scrollPane;
	private TableListeners tableListener;

	private TableRowSorter<LogTableModel> rowSorter;

	public LogTable(JPanel tablePanel, ArrayList<LogDataModel> logList) {
		this.tablePanel = tablePanel;

		logTableModel = new LogTableModel(logList);
		table = new JTable(logTableModel);

		createTablePanel();
		createLogTablePopups();

		rowSorter = new TableRowSorter<LogTableModel>((LogTableModel) table.getModel());
	}

	public void setTableListener(TableListeners listener) {
		this.tableListener = listener;
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<LogDataModel> logList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		logTableModel.setData(logList);
		logTableModel.fireTableDataChanged();
	}

	public void removeData() {
		if (logTableModel.getRowCount() > 0) {
			logTableModel.removeAll();
		}

		scrollPane.setVisible(false);
	}

	private void createTablePanel() {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		table.setShowGrid(true);
		table.getTableHeader().setDefaultRenderer(new LogTableHeaderRenderer());
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		configureColumnWidths();

		// Set table properties
		table.setDefaultRenderer(Object.class, new LogTableRenderer());
		table.setAutoCreateRowSorter(true);
		table.setCellSelectionEnabled(true);
		new TableKeystrokeHandler(table);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(
				new Dimension(tablePanel.getPreferredSize().width, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	private void createLogTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem showStudentInfoItem = new JMenuItem("Show student info ");
		JMenuItem showStudentAttendanceItem = new JMenuItem("Show attendance ");
		JMenuItem deleteLogEntryItem = new JMenuItem("Delete Log Entry ");
		tablePopup.add(showStudentInfoItem);
		tablePopup.add(showStudentAttendanceItem);
		tablePopup.add(deleteLogEntryItem);
		tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_3ROWS));

		// POP UP action listeners
		showStudentInfoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get Client ID for selected row/column
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				LogTableModel model = (LogTableModel) table.getModel();
				String clientIdAsString = (String) model.getValueAt(row, LogTableModel.CLIENT_ID_COLUMN);

				table.clearSelection();
				if (!clientIdAsString.equals("")) {
					int clientID = Integer.parseInt(clientIdAsString);
					tableListener.viewStudentTableByStudent(clientID);
				}
			}
		});
		showStudentAttendanceItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student name for selected row/column
				int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
				LogTableModel model = (LogTableModel) table.getModel();
				String clientID = (String) model.getValueAt(modelRow, LogTableModel.CLIENT_ID_COLUMN);

				table.clearSelection();
				if (!clientID.equals("")) {
					StudentNameModel studentName = (StudentNameModel) model.getValueAt(modelRow,
							LogTableModel.STUDENT_NAME_COLUMN);

					// Display attendance table for selected student
					tableListener.viewAttendanceByStudent(clientID, studentName.toString());
				}
			}
		});
		deleteLogEntryItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get Log ID for selected row/column
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				LogTableModel model = (LogTableModel) table.getModel();
				int logID = (int) model.getValueAt(row, LogTableModel.LOG_ID_COLUMN);
						
				table.clearSelection();
				tableListener.deleteLogEntry(logID);
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = table.getSelectedRow();
				if (e.getButton() == MouseEvent.BUTTON3 && row != -1) {
					// Show the popup menu
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

	private void configureColumnWidths() {
		// Configure column widths
		table.getColumnModel().getColumn(LogTableModel.DATE_COLUMN).setMaxWidth(150);
		table.getColumnModel().getColumn(LogTableModel.DATE_COLUMN).setPreferredWidth(150);
		table.getColumnModel().getColumn(LogTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(LogTableModel.STUDENT_NAME_COLUMN).setMaxWidth(220);
		table.getColumnModel().getColumn(LogTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(180);
		
		// Log ID column is invisible
		table.getColumnModel().getColumn(LogTableModel.LOG_ID_COLUMN).setMinWidth(0);
		table.getColumnModel().getColumn(LogTableModel.LOG_ID_COLUMN).setMaxWidth(0);
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
	public class LogTableRenderer extends JLabel implements TableCellRenderer {
		private LogTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (value instanceof Integer)
				setText(String.valueOf(value));
			else if (value instanceof Date)
				setText(((Date) value).toString());
			else if (value instanceof StudentNameModel)
				setText(((StudentNameModel) value).toString());
			else if (value == null)
				setText("");
			else
				setText((String) value);

			if (column != -1) {
				if (value instanceof StudentNameModel && ((StudentNameModel) value).getIsInMasterDb() == false)
					setFont(CustomFonts.TABLE_ITALIC_TEXT_FONT);
				else
					setFont(CustomFonts.TABLE_TEXT_FONT);
				super.setForeground(Color.black);

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				if (column == LogTableModel.STATUS_COLUMN) {
					super.setHorizontalAlignment(LEFT);
					super.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0)); // left pad
				} else
					super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}

	// ===== NESTED Class: Renderer for table header ===== //
	public class LogTableHeaderRenderer extends JLabel implements TableCellRenderer {
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TABLE_GRID_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

		private LogTableHeaderRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (column == LogTableModel.STATUS_COLUMN)
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
}

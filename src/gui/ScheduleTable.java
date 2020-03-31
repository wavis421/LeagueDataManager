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
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import model.ScheduleModel;

public class ScheduleTable extends JPanel {
	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT_1ROW = 30;
	private static final int DAYS_IN_WEEK = 7;
	private static final int ROW_GAP = 5;

	private final String[] dayOfWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
	private JPanel parentTablePanel;
	private TableListeners scheduleListener;
	private JTable lastSelectedTable;

	// Panel variables: 2 row panels, 1 panel for each day
	private JPanel mainPanel = new JPanel();
	private JPanel row1Panel = new JPanel(), row2Panel = new JPanel();
	private ArrayList<JPanel> dowPanelList = new ArrayList<JPanel>();

	// Scroll pane with table
	private ArrayList<JScrollPane> dowScrollList = new ArrayList<JScrollPane>();
	private ArrayList<JTable> dowTableList = new ArrayList<JTable>();

	// Schedule data, breaks down into table model by day
	private ArrayList<ScheduleTableModel> dowScheduleTableModel = new ArrayList<ScheduleTableModel>();
	private ArrayList<ArrayList<ScheduleModel>> dowScheduleModelList = new ArrayList<ArrayList<ScheduleModel>>();

	private ArrayList<TableRowSorter<ScheduleTableModel>> rowSorterList = new ArrayList<TableRowSorter<ScheduleTableModel>>();

	public ScheduleTable(JPanel tablePanel) {
		this.parentTablePanel = tablePanel;

		Border border = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);

		for (int i = 0; i < DAYS_IN_WEEK; i++) {
			dowScheduleModelList.add(new ArrayList<ScheduleModel>());
			dowScheduleTableModel.add(new ScheduleTableModel(dowScheduleModelList.get(i), i));

			JTable table = new JTable(dowScheduleTableModel.get(i));
			dowTableList.add(table);
			dowScrollList.add(createTablePanel(dowTableList.get(i), dowScheduleTableModel.get(i)));
			dowPanelList.add(new JPanel());
			dowPanelList.get(i).add(dowScrollList.get(i));

			createScheduleTablePopups(dowTableList.get(i));
			dowTableList.get(i).setName(dayOfWeek[i]);

			dowPanelList.get(i).setBorder(BorderFactory.createTitledBorder(border, dayOfWeek[i], TitledBorder.CENTER,
					TitledBorder.DEFAULT_POSITION, CustomFonts.PANEL_HEADER_FONT, CustomFonts.TITLE_COLOR));

			rowSorterList.add(new TableRowSorter<ScheduleTableModel>((ScheduleTableModel) table.getModel()));
			table.setCellSelectionEnabled(true);
			new TableKeystrokeHandler(table);
		}

		// Add first 4 days in week to row1, the rest in row 2
		row1Panel.add(dowPanelList.get(0));
		row1Panel.add(dowPanelList.get(1));
		row1Panel.add(dowPanelList.get(2));
		row1Panel.add(dowPanelList.get(3));

		row2Panel.add(dowPanelList.get(4));
		row2Panel.add(dowPanelList.get(5));
		row2Panel.add(dowPanelList.get(6));

		// Add 2 rows to main panel and make visible
		mainPanel.add(row1Panel, BorderLayout.NORTH);
		mainPanel.add(row2Panel, BorderLayout.CENTER);
		mainPanel.setVisible(true);

		parentTablePanel.add(mainPanel);
		lastSelectedTable = dowTableList.get(0);
	}

	public void setTableListener(TableListeners listener) {
		this.scheduleListener = listener;
	}

	public JTable getTable() {
		return lastSelectedTable;
	}

	public void setData(JPanel tablePanel, ArrayList<ScheduleModel> scheduleList) {
		this.parentTablePanel = tablePanel;

		// Split schedule list by day of week
		for (int i = 0; i < scheduleList.size(); i++) {
			int dowIdx = scheduleList.get(i).getDayOfWeek();
			dowScheduleModelList.get(dowIdx).add(scheduleList.get(i));
		}

		// Add daily schedule data to table model
		for (int i = 0; i < 7; i++) {
			dowScheduleTableModel.get(i).setData(dowScheduleModelList.get(i));
			dowScheduleTableModel.get(i).fireTableDataChanged();
		}

		// Add main panel to parent and make visible
		parentTablePanel.add(mainPanel);
		mainPanel.setVisible(true);
	}

	public void removeData() {
		for (int i = 0; i < 7; i++) {
			// Empty each day of week list
			if (dowScheduleModelList.get(i).size() > 0) {
				dowScheduleModelList.get(i).clear();
			}
		}
		mainPanel.setVisible(false);
	}

	private JScrollPane createTablePanel(JTable dowTable, ScheduleTableModel sourceList) {
		// Set up table parameters
		dowTable.setFont(CustomFonts.TABLE_TEXT_FONT);
		dowTable.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		dowTable.setShowGrid(true);
		dowTable.getTableHeader().setDefaultRenderer(new SchedTableHeaderRenderer());
		int origRowHeight = dowTable.getRowHeight();
		dowTable.setRowHeight(origRowHeight + ROW_GAP);

		// Set table properties
		dowTable.getColumnModel().getColumn(ScheduleTableModel.START_TIME_COLUMN).setMaxWidth(75);
		dowTable.getColumnModel().getColumn(ScheduleTableModel.CLASS_NAME_COLUMN).setMaxWidth(200);

		dowTable.setDefaultRenderer(Object.class, new ScheduleTableRenderer());
		dowTable.setAutoCreateRowSorter(false);

		JScrollPane dowScrollPane = new JScrollPane(dowTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		dowScrollPane.setPreferredSize(new Dimension(280, (parentTablePanel.getPreferredSize().height - 200) / 2));

		Border innerBorder = BorderFactory.createLineBorder(Color.GRAY, 1, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		dowScrollPane.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

		return dowScrollPane;
	}

	private void createScheduleTablePopups(JTable table) {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem scheduleViewByClassItem = new JMenuItem("View by Class ");
		tablePopup.add(scheduleViewByClassItem);
		tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_1ROW));

		// POP UP action listeners
		scheduleViewByClassItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Retrieve class name from selected row
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				ScheduleTableModel model = (ScheduleTableModel) table.getModel();
				String className = (String) model.getValueAt(row, ScheduleTableModel.CLASS_NAME_COLUMN);

				// Display attendance table for selected class
				table.clearSelection();

				// Translate DOW 0-6 to 1-7 and view class by date for current week
				DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));
				int dow = model.getDow();
				if (dow == 0)
					dow = 7;
				DateTime date = today.withDayOfWeek(dow);
				if (date.toString("yyyy-MM-dd").compareTo(today.toString("yyyy-MM-dd")) < 0)
					date = date.plusWeeks(1);
				scheduleListener.viewAttendanceByClass(className, date.toString("yyyy-MM-dd"), true);
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

	private void clearTableSelections(JTable selectedTable) {
		JTable table;
		for (int i = 0; i < dowTableList.size(); i++) {
			// Clear all table selections except for the current selection
			table = dowTableList.get(i);
			if (table != selectedTable)
				dowTableList.get(i).clearSelection();
		}
	}

	public void updateSearchField(String searchText) {
		JTable table;
		RowFilter<ScheduleTableModel, Integer> filter;

		if (searchText.equals(""))
			filter = null;
		else {
			try {
				filter = RowFilter.regexFilter("(?i)\\b" + searchText);
			} catch (java.util.regex.PatternSyntaxException e) {
				System.out.println(e.getMessage());
				return;
			}
		}

		// Update filter for all DOW tables
		for (int i = 0; i < dowTableList.size(); i++) {
			table = dowTableList.get(i);

			rowSorterList.get(i).setRowFilter(filter);
			table.setRowSorter(rowSorterList.get(i));
		}
	}

	// ===== NESTED Class: Renderer for table ===== //
	public class ScheduleTableRenderer extends JLabel implements TableCellRenderer {
		private ScheduleTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (isSelected) {
				clearTableSelections(table);
				lastSelectedTable = table;
			}
			if (value instanceof Integer)
				setText(String.valueOf(value));
			else
				setText((String) value);

			if (column != -1) {
				super.setFont(CustomFonts.TABLE_TEXT_FONT);
				super.setForeground(Color.black);
				super.setHorizontalAlignment(CENTER);

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);
			}
			return this;
		}
	}

	// ===== NESTED Class: Renderer for table header ===== //
	public class SchedTableHeaderRenderer extends JLabel implements TableCellRenderer {
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TABLE_GRID_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

		private SchedTableHeaderRenderer() {
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

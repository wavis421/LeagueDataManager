package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import model.ScheduleModel;

public class ScheduleTable extends JPanel {
	private static final int DAYS_IN_WEEK = 7;
	private static final int ROW_GAP = 5;
	private final String[] dayOfWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

	private JPanel parentTablePanel;

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

	public ScheduleTable(JPanel tablePanel) {
		this.parentTablePanel = tablePanel;

		Border border = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);

		for (int i = 0; i < DAYS_IN_WEEK; i++) {
			dowScheduleModelList.add(new ArrayList<ScheduleModel>());
			dowScheduleTableModel.add(new ScheduleTableModel(dowScheduleModelList.get(i)));
			dowTableList.add(new JTable(dowScheduleTableModel.get(i)));
			dowScrollList.add(createTablePanel(dowTableList.get(i), dowScheduleTableModel.get(i)));
			dowPanelList.add(new JPanel());
			dowPanelList.get(i).add(dowScrollList.get(i));

			dowPanelList.get(i).setBorder(BorderFactory.createTitledBorder(border, dayOfWeek[i], TitledBorder.CENTER,
					TitledBorder.DEFAULT_POSITION, CustomFonts.TITLE_FONT, CustomFonts.TITLE_COLOR));
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
	}

	private JScrollPane createTablePanel(JTable dowTable, ScheduleTableModel sourceList) {
		// Set up table parameters
		dowTable.setFont(CustomFonts.TABLE_TEXT_FONT);
		dowTable.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		dowTable.setShowGrid(true);
		dowTable.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
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

	public JTable getTable() {
		// TODO: Get selected table
		return dowTableList.get(0);
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

	public class ScheduleTableRenderer extends JLabel implements TableCellRenderer {
		private ScheduleTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
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
}

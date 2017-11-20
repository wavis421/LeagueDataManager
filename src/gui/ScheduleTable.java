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
import javax.swing.table.TableCellRenderer;

import model.ScheduleModel;

public class ScheduleTable extends JPanel {
	private static final int ROW_GAP = 5;

	private JPanel tablePanel;
	private JTable table;
	private ScheduleTableModel scheduleTableModel;
	private JScrollPane scrollPane;

	public ScheduleTable(JPanel tablePanel, ArrayList<ScheduleModel> arrayList) {
		this.tablePanel = tablePanel;

		scheduleTableModel = new ScheduleTableModel(arrayList);
		table = new JTable(scheduleTableModel);

		createTablePanel();
	}

	private void createTablePanel() {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		table.setShowGrid(true);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		configureColumnWidths();

		// Set table properties
		table.setDefaultRenderer(Object.class, new ScheduleTableRenderer());
		table.setAutoCreateRowSorter(false);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(
				new Dimension(tablePanel.getPreferredSize().width, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<ScheduleModel> scheduleList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		scheduleTableModel.setData(scheduleList);
		scheduleTableModel.fireTableDataChanged();
	}

	public void removeData() {
		if (scheduleTableModel.getRowCount() > 0) {
			scheduleTableModel.removeAll();
		}

		scrollPane.setVisible(false);
	}

	private void configureColumnWidths() {
		// Configure column widths
		table.getColumnModel().getColumn(ScheduleTableModel.DAY_OF_WEEK_COLUMN).setMaxWidth(100);
		table.getColumnModel().getColumn(ScheduleTableModel.START_TIME_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(ScheduleTableModel.CLASS_NAME_COLUMN).setMaxWidth(200);
	}

	// TODO: share/merge this table renderer
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

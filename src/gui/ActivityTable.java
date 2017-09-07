package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.Date;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;

import model.ActivityModel;
import model.ActivityTableModel;

public class ActivityTable extends JPanel {
	private static final int ROW_GAP = 5;

	private JPanel tablePanel;
	private JTable table;
	private ActivityTableModel activityTableModel;
	private JScrollPane scrollPane;

	public ActivityTable(JPanel tablePanel, ArrayList<ActivityModel> activitiesList) {
		this.tablePanel = tablePanel;

		activityTableModel = new ActivityTableModel(activitiesList);
		table = new JTable(activityTableModel);

		createTablePanel();
	}

	private void createTablePanel() {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		// Configure column widths
		table.getColumnModel().getColumn(activityTableModel.getColumnForClientID()).setMaxWidth(75);
		table.getColumnModel().getColumn(activityTableModel.getColumnForServiceDate()).setMaxWidth(120);

		// Set table properties
		table.setDefaultRenderer(Object.class, new ActivityTableRenderer());
		table.setAutoCreateRowSorter(true);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(
				new Dimension(tablePanel.getPreferredSize().width, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	public void setData(JPanel tablePanel, ArrayList<ActivityModel> activityList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		activityTableModel.setData(activityList);
		activityTableModel.fireTableDataChanged();
	}

	public void removeData() {
		System.out.println("Remove data, activity table rows = " + activityTableModel.getRowCount());

		if (activityTableModel.getRowCount() > 0) {
			activityTableModel.removeAll();
			activityTableModel.fireTableDataChanged();
		}

		scrollPane.setVisible(false);
	}

	// TODO: share this table renderer
	public class ActivityTableRenderer extends JLabel implements TableCellRenderer {
		private ActivityTableRenderer() {
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
			else
				setText((String) value);

			if (column != -1) {
				setFont(CustomFonts.TABLE_TEXT_FONT);
				super.setForeground(Color.black);

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				super.setVerticalAlignment(TOP);
				super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}
}

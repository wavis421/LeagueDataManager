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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import model.ActivityEventModel;
import model.ActivityModel;
import model.ActivityTableModel;
import model.StudentNameModel;

public class ActivityTable extends JPanel {
	private static final int ROW_GAP = 5;

	// Columns for embedded event table
	private static final int EVENT_TABLE_DATE_COLUMN = 0;
	private static final int EVENT_TABLE_CLASS_NAME_COLUMN = 1;
	private static final int EVENT_TABLE_COMMENTS_COLUMN = 2;
	private static final int EVENT_TABLE_NUM_COLUMNS = 3;

	private JPanel tablePanel;
	private JTable table;
	private ActivityTableModel activityTableModel;
	private JScrollPane tableScrollPane;

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

		configureColumnWidths();

		// Set table properties
		table.setDefaultRenderer(Object.class, new ActivityTableRenderer());
		table.setAutoCreateRowSorter(true);

		tablePanel.setLayout(new BorderLayout());
		tableScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tableScrollPane.setPreferredSize(
				new Dimension(tablePanel.getPreferredSize().width, tablePanel.getPreferredSize().height - 70));
		tableScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(tableScrollPane, BorderLayout.NORTH);
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<ActivityModel> activityList) {
		activityTableModel.setData(activityList);
		activityTableModel.fireTableDataChanged();

		tableScrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(tableScrollPane, BorderLayout.NORTH);
	}

	public void removeData() {
		if (activityTableModel.getRowCount() > 0) {
			activityTableModel.removeAll();
			activityTableModel.fireTableDataChanged();
		}
		tableScrollPane.setVisible(false);
	}

	private void configureColumnWidths() {
		// Configure column widths
		table.getColumnModel().getColumn(ActivityTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(ActivityTableModel.STUDENT_NAME_COLUMN).setMaxWidth(220);
		table.getColumnModel().getColumn(ActivityTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(180);

		// Configure row height
		table.setRowHeight(table.getRowHeight() * 3);
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

			if (value.getClass().isArray()) {
				final Object[] inputData = (Object[]) value;

				// Create table that will hold the multi-value field embedded in the main table
				JTable embeddedTable = new JTable(new AbstractTableModel() {
					public int getColumnCount() {
						return EVENT_TABLE_NUM_COLUMNS;
					}

					public int getRowCount() {
						return inputData.length;
					}

					public Object getValueAt(int row, int col) {
						ActivityEventModel activity = (ActivityEventModel) inputData[row];
						if (col == EVENT_TABLE_DATE_COLUMN)
							return activity.getServiceDate().toString();
						else if (col == EVENT_TABLE_CLASS_NAME_COLUMN)
							return activity.getEventName();
						else
							return activity.getGithubComments();
					}

					public boolean isCellEditable(int row, int col) {
						return true;
					}
				});
				embeddedTable.setFont(CustomFonts.TABLE_TEXT_FONT);
				embeddedTable.setShowVerticalLines(false);
				embeddedTable.setShowHorizontalLines(false);
				embeddedTable.setForeground(Color.black);
				if (isSelected)
					embeddedTable.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					embeddedTable.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				embeddedTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setMaxWidth(90);
				embeddedTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setPreferredWidth(90);
				embeddedTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setMaxWidth(180);
				embeddedTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setPreferredWidth(180);

				return embeddedTable;

			} else {
				super.setHorizontalAlignment(CENTER);
				if (value instanceof StudentNameModel && ((StudentNameModel) value).getIsInMasterDb() == false)
					setFont(CustomFonts.TABLE_ITALIC_TEXT_FONT);
				else
					setFont(CustomFonts.TABLE_TEXT_FONT);
				super.setForeground(Color.black);

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				if (value instanceof StudentNameModel) {
					StudentNameModel student = (StudentNameModel) value;
					super.setText(student.toString());

				} else if (value instanceof String) {
					super.setText((String) value);
				}
				return this;
			}
		}
	}
}

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
	private static final int ROW_HEIGHT = 65;

	// Columns for embedded event table
	private static final int EVENT_TABLE_DATE_COLUMN = 0;
	private static final int EVENT_TABLE_CLASS_NAME_COLUMN = 1;
	private static final int EVENT_TABLE_COMMENTS_COLUMN = 2;
	private static final int EVENT_TABLE_NUM_COLUMNS = 3;

	private JPanel parentTablePanel;
	private JPanel githubEventPanel = new JPanel();
	private JTable mainTable;
	private ArrayList<JTable> githubEventTableList = new ArrayList<JTable>();
	private ActivityTableModel activityTableModel;
	private JScrollPane tableScrollPane;

	public ActivityTable(JPanel tablePanel, ArrayList<ActivityModel> activitiesList) {
		this.parentTablePanel = tablePanel;

		// Create main table-model and table
		activityTableModel = new ActivityTableModel(activitiesList);
		mainTable = new JTable(activityTableModel);
		createTablePanel();
		
		// Create event sub-table with github comments by date
		createEventTable(activitiesList);
	}

	private void createTablePanel() {
		// Set up table parameters
		mainTable.setFont(CustomFonts.TABLE_TEXT_FONT);
		mainTable.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);

		// Configure column height and width
		mainTable.setRowHeight(ROW_HEIGHT);
		mainTable.getColumnModel().getColumn(ActivityTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		mainTable.getColumnModel().getColumn(ActivityTableModel.STUDENT_NAME_COLUMN).setMaxWidth(220);
		mainTable.getColumnModel().getColumn(ActivityTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(180);

		// Set table properties
		mainTable.setDefaultRenderer(Object.class, new ActivityTableRenderer());
		mainTable.setAutoCreateRowSorter(true);

		parentTablePanel.setLayout(new BorderLayout());
		tableScrollPane = new JScrollPane(mainTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tableScrollPane.setPreferredSize(
				new Dimension(parentTablePanel.getPreferredSize().width, parentTablePanel.getPreferredSize().height - 70));
		tableScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		parentTablePanel.add(tableScrollPane, BorderLayout.NORTH);
	}

	public JTable getTable() {
		return mainTable;
	}

	public void setData(JPanel tablePanel, ArrayList<ActivityModel> activityList) {
		this.parentTablePanel = tablePanel;
		
		// Set data for main table
		activityTableModel.setData(activityList);
		
		// Create github sub-table
		createEventTable(activityList);
		
		// Update table
		activityTableModel.fireTableDataChanged();
		tableScrollPane.setVisible(true);
		tablePanel.add(tableScrollPane, BorderLayout.NORTH);
	}

	public void removeData() {
		// Remove data from tables and lists
		if (activityTableModel.getRowCount() > 0) {
			activityTableModel.removeAll();
			githubEventTableList.clear();
			githubEventPanel.removeAll();

			activityTableModel.fireTableDataChanged();
		}
		tableScrollPane.setVisible(false);
	}

	private void createEventTable(ArrayList<ActivityModel> tableData) {
		for (int i = 0; i < tableData.size(); i++) {
			// Create github sub-table
			ArrayList<ActivityEventModel> eventData = tableData.get(i).getActivityEventList();
			JTable eventTable = new JTable(new EventTableModel(eventData));

			// Set table properties
			eventTable.setFont(CustomFonts.TABLE_TEXT_FONT);
			eventTable.setShowVerticalLines(false);
			eventTable.setShowHorizontalLines(false);
			eventTable.setTableHeader(null);

			eventTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setMaxWidth(90);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_DATE_COLUMN).setPreferredWidth(90);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setMaxWidth(180);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setPreferredWidth(180);

			// Add table to event panel and array list
			githubEventTableList.add(eventTable);
			githubEventPanel.add(eventTable);
		}
	}

	// Renderer for main table
	public class ActivityTableRenderer extends JLabel implements TableCellRenderer {

		private ActivityTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			super.setForeground(Color.black);
			if (isSelected)
				super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
			else
				super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);
			super.setHorizontalAlignment(CENTER);

			if (value instanceof String) {
				// Used for class name column
				super.setText((String) value);
				return this;

			} else if (value instanceof StudentNameModel) {
				if (((StudentNameModel) value).getIsInMasterDb() == false)
					setFont(CustomFonts.TABLE_ITALIC_TEXT_FONT);
				else
					setFont(CustomFonts.TABLE_TEXT_FONT);

				StudentNameModel student = (StudentNameModel) value;
				super.setText(student.toString());
				return this;
				
			} else {
				// Github comments column
				return githubEventTableList.get(row);
			}
		}
	}

	// Model for Github sub-table
	public class EventTableModel extends AbstractTableModel {
		ArrayList<ActivityEventModel> inputData;

		public EventTableModel(ArrayList<ActivityEventModel> tableData) {
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
			ActivityEventModel activity = (ActivityEventModel) inputData.get(row);

			if (col == EVENT_TABLE_DATE_COLUMN)
				return activity.getServiceDate().toString();
			else if (col == EVENT_TABLE_CLASS_NAME_COLUMN)
				return activity.getEventName();
			else
				return activity.getGithubComments();
		}
	}
}

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import model.ActivityEventModel;
import model.ActivityModel;
import model.StudentNameModel;

public class ActivityTable extends JPanel {
	private static final int TEXT_HEIGHT = 16;
	private static final int ROW_HEIGHT = (TEXT_HEIGHT * 4);

	private static final int POPUP_WINDOW_WIDTH = 1200;
	private static final int POPUP_WINDOW_HEIGHT = 600;

	// Columns for embedded event table
	private static final int EVENT_TABLE_DATE_COLUMN = 0;
	private static final int EVENT_TABLE_CLASS_NAME_COLUMN = 1;
	private static final int EVENT_TABLE_REPO_NAME_COLUMN = 2;
	private static final int EVENT_TABLE_COMMENTS_COLUMN = 3;
	private static final int EVENT_TABLE_NUM_COLUMNS = 4;

	private JPanel parentTablePanel;
	private JTable mainTable;
	private ArrayList<JTable> githubEventTableList = new ArrayList<JTable>();
	private ActivityTableModel activityTableModel;
	private JScrollPane tableScrollPane;
	private int eventTableSelectedRow = -1; // table row
	private int eventSelectedRow = -1; // row within table row

	public ActivityTable(JPanel tablePanel, ArrayList<ActivityModel> activitiesList) {
		this.parentTablePanel = tablePanel;

		// Create main table-model and table
		activityTableModel = new ActivityTableModel(activitiesList);
		mainTable = new JTable(activityTableModel);

		// Create event sub-table with github comments by date
		createEventTable(activitiesList, githubEventTableList);

		// Configure table panel
		tableScrollPane = createTablePanel(mainTable, parentTablePanel, githubEventTableList, ROW_HEIGHT,
				parentTablePanel.getPreferredSize().height - 70);
	}

	private JScrollPane createTablePanel(JTable table, JPanel panel, ArrayList<JTable> eventList, int rowHeight,
			int panelHeight) {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);

		// Configure column height and width
		table.setRowHeight(rowHeight);
		table.getColumnModel().getColumn(ActivityTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(ActivityTableModel.STUDENT_NAME_COLUMN).setMaxWidth(220);
		table.getColumnModel().getColumn(ActivityTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(180);

		// Set table properties
		table.setDefaultRenderer(Object.class, new ActivityTableRenderer(eventList));
		table.setAutoCreateRowSorter(true);

		panel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(panel.getPreferredSize().width, panelHeight));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		panel.add(scrollPane, BorderLayout.NORTH);

		return scrollPane;
	}

	public JTable getTable() {
		return mainTable;
	}

	public void setSelectedEventRow(int selectedRow, int yPos) {
		eventSelectedRow = getEventRow(selectedRow, yPos);
		mainTable.repaint(); // TODO: This is probably overkill
	}

	public String getClassNameByRow(int selectedRow, int modelRow, int yPos) {
		JTable table = githubEventTableList.get(modelRow);
		int eventRow = getEventRow(selectedRow, yPos);

		if (eventRow > -1) {
			return (String) ((EventTableModel) table.getModel()).getValueAt(eventRow, EVENT_TABLE_CLASS_NAME_COLUMN);
		} else
			return null;
	}

	private int getEventRow(int selectedRow, int yPos) {
		// Compute row based on Y-position in event table
		JTable table = githubEventTableList.get(selectedRow);
		int row = (yPos - (selectedRow * ROW_HEIGHT)) / TEXT_HEIGHT;
		if (row < table.getModel().getRowCount())
			return row;
		else
			return -1;
	}

	public void setData(JPanel tablePanel, ArrayList<ActivityModel> activityList) {
		this.parentTablePanel = tablePanel;

		// Set data for main table
		activityTableModel.setData(activityList);

		// Create github sub-table
		createEventTable(activityList, githubEventTableList);

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
		}
		tableScrollPane.setVisible(false);
	}

	private void createEventTable(ArrayList<ActivityModel> tableData, ArrayList<JTable> eventList) {
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
			eventTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setMaxWidth(204);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_CLASS_NAME_COLUMN).setPreferredWidth(204);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_REPO_NAME_COLUMN).setMaxWidth(300);
			eventTable.getColumnModel().getColumn(EVENT_TABLE_REPO_NAME_COLUMN).setPreferredWidth(275);

			// Add renderer
			eventTable.setDefaultRenderer(Object.class, new ActivityTableRenderer(null));

			// Add table to event panel and array list
			eventList.add(eventTable);
		}
	}

	public void showActivitiesByPerson(String studentName, ArrayList<ActivityModel> arrayList) {
		JFrame frame = new JFrame("Attendance for " + studentName);
		ActivityTableModel model = new ActivityTableModel(arrayList);
		JTable table = new JTable(model);
		JPanel tablePanel = new JPanel();
		ArrayList<JTable> eventList = new ArrayList<JTable>();

		// Create table header
		JLabel headerLabel = new JLabel("Attendance for " + studentName);
		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		headerLabel.setFont(CustomFonts.TITLE_FONT);
		headerLabel.setForeground(CustomFonts.TITLE_COLOR);
		frame.add(headerLabel, BorderLayout.NORTH);

		// Set table panel size and borders, and disable selections
		tablePanel.setPreferredSize(new Dimension(POPUP_WINDOW_WIDTH, POPUP_WINDOW_HEIGHT));
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(5, 1, 1, 1);
		tablePanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		table.setCellSelectionEnabled(false);
		table.clearSelection();

		// Add data to a scroll pane inside the tablePanel
		createEventTable(arrayList, eventList);
		createTablePanel(table, tablePanel, eventList, POPUP_WINDOW_HEIGHT - 48, POPUP_WINDOW_HEIGHT - 18);
		frame.add(tablePanel, BorderLayout.CENTER);

		// Configure and show frame
		ImageIcon icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		frame.setIconImage(icon.getImage());
		frame.setLocation(parentTablePanel.getLocation().x + 50, parentTablePanel.getLocation().y + 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	// ===== NESTED Class: Renderer for main table ===== //
	public class ActivityTableRenderer extends JLabel implements TableCellRenderer {

		ArrayList<JTable> eventTable;

		private ActivityTableRenderer(ArrayList<JTable> eventTable) {
			super();
			super.setOpaque(true);
			this.eventTable = eventTable;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			// Set foreground/background colors
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

			if (value instanceof String) {
				// String columns
				if (table != mainTable && isSelected && eventSelectedRow == row)
					super.setForeground(CustomFonts.ICON_COLOR);

				super.setText((String) value);
				return this;

			} else if (value instanceof StudentNameModel) {
				// Students not in master DB are in italics
				if (((StudentNameModel) value).getIsInMasterDb() == false)
					setFont(CustomFonts.TABLE_ITALIC_TEXT_FONT);
				else
					setFont(CustomFonts.TABLE_TEXT_FONT);

				StudentNameModel student = (StudentNameModel) value;
				super.setText(student.toString());
				return this;

			} else {
				// Github comments column
				int modelRow = table.convertRowIndexToModel(row);
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
			else if (col == EVENT_TABLE_REPO_NAME_COLUMN) {
				if (activity.getRepoName() == null)
					return "";
				else
					return activity.getRepoName();
			} else
				return activity.getGithubComments();
		}
	}
}

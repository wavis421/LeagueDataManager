package model;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ActivityTableModel extends AbstractTableModel {
	public static final int CLIENT_ID_COLUMN = 0;
	public static final int STUDENT_NAME_COLUMN = 1;
	public static final int GITHUB_COMMENTS_COLUMN = 2;
	private static final int TABLE_NUM_COLUMNS = 3;

	private Object[][] tableObjects;
	private final String[] colNames = { " Client ID ", " Student Name ", " Github Comments " };

	public ActivityTableModel(ArrayList<ActivityModel> activities) {
		initializeTableData(activities);
	}

	public void setData(ArrayList<ActivityModel> db) {
		initializeTableData(db);
	}

	private void initializeTableData(ArrayList<ActivityModel> db) {
		tableObjects = new Object[db.size()][TABLE_NUM_COLUMNS];

		for (int row = 0; row < db.size(); row++) {
			tableObjects[row][CLIENT_ID_COLUMN] = String.valueOf(db.get(row).getClientID());
			tableObjects[row][STUDENT_NAME_COLUMN] = db.get(row).getStudentName();
			tableObjects[row][GITHUB_COMMENTS_COLUMN] = db.get(row).getActivityEventList().toArray();
		}
		System.out.println("Num Activities: " + db.size());
	}

	public void removeAll() {
		for (int i = 0; i < tableObjects.length; i++) {
			for (int j = 0; j < TABLE_NUM_COLUMNS; j++) {
				tableObjects[i][j] = null;
			}
		}
		tableObjects = null;
	}

	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return colNames[column];
	}

	@Override
	public int getRowCount() {
		if (tableObjects == null)
			return 0;
		else
			return tableObjects.length;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return tableObjects[0][columnIndex].getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return tableObjects[row][col];
	}
}

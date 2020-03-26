package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.AttendanceEventModel;

public class AttendEventTableModel extends AbstractTableModel {
	public static final int CLASS_DATE_COLUMN    = 0;
	public static final int CLASS_NAME_COLUMN    = 1;
	public static final int TEACHER_NAMES_COLUMN = 2;
	public static final int REPO_NAME_COLUMN     = 3;
	public static final int GIT_COMMENTS_COLUMN  = 4;
	public static final int CLIENT_ID_COLUMN     = 5;   // Not a visible column
	public static final int TABLE_NUM_COLUMNS    = 6;

	private Object[][] tableObjects;
	private final String[] colNames = { " Class Date ", " Class Name ", " Teacher Name(s) ",
			" Repository Name ", " Github Comments " };

	public AttendEventTableModel(ArrayList<AttendanceEventModel> attendance) {
		initializeTableData(attendance);
	}

	public void setData(ArrayList<AttendanceEventModel> db) {
		initializeTableData(db);
	}

	private void initializeTableData(ArrayList<AttendanceEventModel> db) {
		tableObjects = new Object[db.size()][TABLE_NUM_COLUMNS];

		for (int row = 0; row < db.size(); row++) {
			tableObjects[row][CLASS_DATE_COLUMN]    = db.get(row).getServiceDateString();
			tableObjects[row][CLASS_NAME_COLUMN]    = db.get(row).getEventName();
			tableObjects[row][TEACHER_NAMES_COLUMN] = db.get(row).getTeacherNames();
			tableObjects[row][REPO_NAME_COLUMN]     = db.get(row).getRepoName();
			tableObjects[row][GIT_COMMENTS_COLUMN]  = db.get(row).getGithubComments();
			tableObjects[row][CLIENT_ID_COLUMN]     = db.get(row).getClientID();
		}
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
		return String.class;
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

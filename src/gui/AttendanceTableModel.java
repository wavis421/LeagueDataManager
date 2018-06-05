package gui;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import gui.AttendanceTable.EventTableModel;
import model.AttendanceModel;

public class AttendanceTableModel extends AbstractTableModel {
	public static final int CLIENT_ID_COLUMN = 0;
	public static final int STUDENT_NAME_COLUMN = 1;
	public static final int GITHUB_COMMENTS_COLUMN = 2;
	public static final int GITHUB_NAME_COLUMN = 3; // Not actually a column
	private static final int TABLE_NUM_COLUMNS = 4;

	private Object[][] tableObjects;
	private final String[] colNames = { " ID ", " Student Name ",
			" Class Date / Class Name / Repository Name / Github Comments " };

	public AttendanceTableModel(ArrayList<AttendanceModel> attendance) {
		initializeTableData(attendance);
	}

	public void setData(ArrayList<AttendanceModel> db) {
		initializeTableData(db);
	}

	private void initializeTableData(ArrayList<AttendanceModel> db) {
		tableObjects = new Object[db.size()][TABLE_NUM_COLUMNS];

		for (int row = 0; row < db.size(); row++) {
			tableObjects[row][CLIENT_ID_COLUMN] = String.valueOf(db.get(row).getClientID());
			tableObjects[row][STUDENT_NAME_COLUMN] = db.get(row).getStudentName();
			tableObjects[row][GITHUB_COMMENTS_COLUMN] = db.get(row).getAttendanceEventList().toArray();

			// Github name is not actually a column in table, just a placeholder
			if (!db.get(row).getStudentName().getIsInMasterDb())
				tableObjects[row][GITHUB_NAME_COLUMN] = "";
			else if (db.get(row).getGithubName() == null || db.get(row).getGithubName().equals(""))
				tableObjects[row][GITHUB_NAME_COLUMN] = null;
			else
				tableObjects[row][GITHUB_NAME_COLUMN] = db.get(row).getGithubName();
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
		if (tableObjects == null || tableObjects.length == 0)
			return Object.class;
		else if (columnIndex == GITHUB_COMMENTS_COLUMN)
			return EventTableModel.class;
		else
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

	public String getGithubNameByRow(int row) {
		return (String) tableObjects[row][GITHUB_NAME_COLUMN];
	}
}
